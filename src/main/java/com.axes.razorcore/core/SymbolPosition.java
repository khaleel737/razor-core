package com.axes.razorcore.core;

import com.axes.razorcore.service.RiskEngine;
import com.axes.razorcore.utils.StateHash;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class SymbolPosition implements WriteBytesMarshallable, StateHash {

public long uuid;
public int symbol;
public int currency;
public OrderDirection orderDirection;
public long openVolume;
public long openPriceSum;
public long profit;
public long pendingSellSize;
public long pendingBuySize;

public void initialize(long uuid, int symbol, int currency) {
    this.uuid = uuid;
    this.symbol = symbol;
    this.currency = currency;
    this.orderDirection = OrderDirection.EMPTY;
    this.openVolume = 0;
    this.openPriceSum = 0;
    this.profit = 0;
    this.pendingSellSize = 0;
    this.pendingBuySize = 0;
}

public SymbolPosition(long uuid, BytesIn bytes) {
    this.uuid = uuid;
//    Bytes
    this.symbol = bytes.readInt();
    this.currency = bytes.readInt();
    this.orderDirection = OrderDirection.of(bytes.readByte());
    this.openVolume = bytes.readLong();
    this.openPriceSum = bytes.readLong();
    this.profit = bytes.readLong();
    this.pendingSellSize = bytes.readLong();
    this.pendingBuySize = bytes.readLong();
}

    public boolean isEmpty() {
        return direction == PositionDirection.EMPTY
                && pendingSellSize == 0
                && pendingBuySize == 0;
    }

    public void pendingHold(OrderAction orderAction, long size) {
        if (orderAction == OrderAction.ASK) {
            pendingSellSize += size;
        } else {
            pendingBuySize += size;
        }
    }

    public void pendingRelease(OrderAction orderAction, long size) {
        if (orderAction == OrderAction.ASK) {
            pendingSellSize -= size;
        } else {
            pendingBuySize -= size;
        }

//        if (pendingSellSize < 0 || pendingBuySize < 0) {
//            log.error("uid {} : pendingSellSize:{} pendingBuySize:{}", uid, pendingSellSize, pendingBuySize);
//        }
    }

    public long estimateProfit(final SymbolSpecification spec, final RiskEngine.LastPriceCacheRecord lastPriceCacheRecord) {
        switch (orderDirection) {
            case EMPTY:
                return profit;
            case LONG:
                return profit + ((lastPriceCacheRecord != null && lastPriceCacheRecord.bidPrice != 0)
                        ? (openVolume * lastPriceCacheRecord.bidPrice - openPriceSum)
                        : spec.marginBuy * openVolume); // unknown price - no liquidity - require extra margin
            case SHORT:
                return profit + ((lastPriceCacheRecord != null && lastPriceCacheRecord.askPrice != Long.MAX_VALUE)
                        ? (openPriceSum - openVolume * lastPriceCacheRecord.askPrice)
                        : spec.marginSell * openVolume); // unknown price - no liquidity - require extra margin
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Calculate required margin based on specification and current position/orders
     *
     * @param spec core symbol specification
     * @return required margin
     */
    public long calculateRequiredMarginForFutures(SymbolSpecification spec) {
        final long specMarginBuy = spec.marginBuy;
        final long specMarginSell = spec.marginSell;


        final long signedPosition = openVolume * orderDirection.getMultiplier();
        final long currentRiskBuySize = pendingBuySize + signedPosition;
        final long currentRiskSellSize = pendingSellSize - signedPosition;

        final long marginBuy = specMarginBuy * currentRiskBuySize;
        final long marginSell = specMarginSell * currentRiskSellSize;
        // marginBuy or marginSell can be negative, but not both of them
        return Math.max(marginBuy, marginSell);
    }

    /**
     * Calculate required margin based on specification and current position/orders
     * considering extra size added to current position (or outstanding orders)
     *
     * @param spec   symbols specification
     * @param action order action
     * @param size   order size
     * @return -1 if order will reduce current exposure (no additional margin required), otherwise full margin for symbol position if order placed/executed
     */
    public long calculateRequiredMarginForOrder(final SymbolSpecification spec, final OrderAction action, final long size) {
        final long specMarginBuy = spec.marginBuy;
        final long specMarginSell = spec.marginSell;

        final long signedPosition = openVolume * orderDirection.getMultiplier();
        final long currentRiskBuySize = pendingBuySize + signedPosition;
        final long currentRiskSellSize = pendingSellSize - signedPosition;

        long marginBuy = specMarginBuy * currentRiskBuySize;
        long marginSell = specMarginSell * currentRiskSellSize;
        // either marginBuy or marginSell can be negative (because of signedPosition), but not both of them
        final long currentMargin = Math.max(marginBuy, marginSell);

        if (action == OrderAction.BID) {
            marginBuy += spec.marginBuy * size;
        } else {
            marginSell += spec.marginSell * size;
        }

        // marginBuy or marginSell can be negative, but not both of them
        final long newMargin = Math.max(marginBuy, marginSell);

        return (newMargin <= currentMargin) ? -1 : newMargin;
    }


    /**
     * Update position for one user
     * 1. Un-hold pending size
     * 2. Reduce opposite position accordingly (if exists)
     * 3. Increase forward position accordingly (if size left in the trading event)
     *
     * @param action order action
     * @param size   order size
     * @param price  order price
     * @return opened size
     */
    public long updatePositionForMarginTrade(OrderAction action, long size, long price) {

        // 1. Un-hold pending size
        pendingRelease(action, size);

        // 2. Reduce opposite position accordingly (if exists)
        final long sizeToOpen = closeCurrentPositionFutures(action, size, price);

        // 3. Increase forward position accordingly (if size left in the trading event)
        if (sizeToOpen > 0) {
            openPositionMargin(action, sizeToOpen, price);
        }
        return sizeToOpen;
    }

    private long closeCurrentPositionFutures(final OrderAction action, final long tradeSize, final long tradePrice) {

        // log.debug("{} {} {} {} cur:{}-{} profit={}", uid, action, tradeSize, tradePrice, position, totalSize, profit);

        if (orderDirection == OrderDirection.EMPTY || orderDirection == OrderDirection.of(action)) {
            // nothing to close
            return tradeSize;
        }

        if (openVolume > tradeSize) {
            // current position is bigger than trade size - just reduce position accordingly, don't fix profit
            openVolume -= tradeSize;
            openPriceSum -= tradeSize * tradePrice;
            return 0;
        }

        // current position smaller than trade size, can close completely and calculate profit
        profit += (openVolume * tradePrice - openPriceSum) * orderDirection.getMultiplier();
        openPriceSum = 0;
        orderDirection = OrderDirection.EMPTY;
        final long sizeToOpen = tradeSize - openVolume;
        openVolume = 0;

        // validateInternalState();

        return sizeToOpen;
    }

    private void openPositionMargin(OrderAction action, long sizeToOpen, long tradePrice) {
        openVolume += sizeToOpen;
        openPriceSum += tradePrice * sizeToOpen;
        orderDirection = OrderDirection.of(action);

        // validateInternalState();
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeInt(symbol);
        bytes.writeInt(currency);
        bytes.writeByte((byte) orderDirection.getMultiplier());
        bytes.writeLong(openVolume);
        bytes.writeLong(openPriceSum);
        bytes.writeLong(profit);
        bytes.writeLong(pendingSellSize);
        bytes.writeLong(pendingBuySize);
    }

    public void reset() {

        // log.debug("records: {}, Pending B{} S{} total size: {}", records.size(), pendingBuySize, pendingSellSize, totalSize);

        pendingBuySize = 0;
        pendingSellSize = 0;

        openVolume = 0;
        openPriceSum = 0;
        orderDirection = OrderDirection.EMPTY;
    }

    public void validateInternalState() {
        if (orderDirection == OrderDirection.EMPTY && (openVolume != 0 || openPriceSum != 0)) {
            log.error("uid {} : position:{} totalSize:{} openPriceSum:{}", uuid, orderDirection, openVolume, openPriceSum);
            throw new IllegalStateException();
        }
        if (orderDirection != OrderDirection.EMPTY && (openVolume <= 0 || openPriceSum <= 0)) {
            log.error("uid {} : position:{} totalSize:{} openPriceSum:{}", uuid, orderDirection, openVolume, openPriceSum);
            throw new IllegalStateException();
        }

        if (pendingSellSize < 0 || pendingBuySize < 0) {
            log.error("uid {} : pendingSellSize:{} pendingBuySize:{}", uuid, pendingSellSize, pendingBuySize);
            throw new IllegalStateException();
        }
    }

    @Override
    public int stateHash() {
        return Objects.hash(symbol, currency, orderDirection.getMultiplier(), openVolume, openPriceSum, profit, pendingSellSize, pendingBuySize);
    }

    @Override
    public String toString() {
        return "SPR{" +
                "u" + uuid +
                " sym" + symbol +
                " cur" + currency +
                " pos" + orderDirection +
                " Σv=" + openVolume +
                " Σp=" + openPriceSum +
                " pnl=" + profit +
                " pendingS=" + pendingSellSize +
                " pendingB=" + pendingBuySize +
                '}';
    }
}
