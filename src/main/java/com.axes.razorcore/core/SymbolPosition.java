package com.axes.razorcore.core;

import com.axes.razorcore.utils.StateHash;
import lombok.NoArgsConstructor;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;

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
    return orderDirection == OrderDirection.EMPTY && pendingSellSize == 0 && pendingBuySize == 0;
}

public void pendingHold(OrderAction orderAction, long size) {
    if(orderAction == OrderAction.ASK) {
        pendingSellSize += size;
    } else {
        pendingBuySize += size;
    }
}

// Equity Profit Overloaded method
public long estimatedProfit(final EquitySymbolSpecification symbol, final String lastPriceCacheRecord) {
    return (long) 0.00;
}
    // Forex Profit Overloaded method
    public long estimatedProfit(final ForexSymbolSpecification symbol, final String lastPriceCacheRecord) {
        return (long) 0.00;
    }
    // Futures Profit Overloaded method
    public long estimatedProfit(final FuturesSymbolSpecification symbol, final String lastPriceCacheRecord) {
        return (long) 0.00;
    }
    // Options Profit Overloaded method
    public long estimatedProfit(final OptionsSymbolSpecification symbol, final String lastPriceCacheRecord) {
        return (long) 0.00;
    }

//    Forex Margin Overloaded Method
    public long calculateRequiredMargin(EquitySymbolSpecification symbol) {
        return (long) 0.00;
    }
    //    Forex Margin Overloaded Method
    public long calculateRequiredMargin(ForexSymbolSpecification symbol) {
        return (long) 0.00;
    }
    //    Forex Margin Overloaded Method
    public long calculateRequiredMargin(FuturesSymbolSpecification symbol) {
        return (long) 0.00;
    }
    //    Forex Margin Overloaded Method
    public long calculateRequiredMargin(OptionsSymbolSpecification symbol) {
        return (long) 0.00;
    }

    public long updatePositionForMarginTrade(OrderAction orderAction, long size, long price) {
    return (long) 0.00;
    }

    private long closeCurrentPosition(final OrderAction positionDirection, final long tradeSize, final long tradePrice) {
    if(orderDirection == OrderDirection.EMPTY || orderDirection == OrderDirection.of(positionDirection)) {
        return tradeSize;
    }
    if(openVolume > tradeSize) {
        openVolume -= tradeSize;
        openPriceSum -= tradeSize * tradePrice;
        return 0;
    }
    profit += (openVolume * tradePrice - openPriceSum) * orderDirection.getMultiplier();
    openPriceSum = 0;
    orderDirection = OrderDirection.EMPTY;
    final long sizeToOpen = tradeSize - openVolume;
    openVolume = 0;

    return sizeToOpen;
    }

    private void openPositionMargin(OrderAction orderAction, long sizeToOpen, long tradePrice) {
    openVolume += sizeToOpen;
    openPriceSum += tradePrice * sizeToOpen;
    orderDirection = OrderDirection.of(orderAction);
    }

    public void reset() {
                orderDirection = OrderDirection.EMPTY;
                openVolume = 0;
                openPriceSum = 0;
                profit = 0;
                pendingSellSize = 0;
                pendingBuySize = 0;
    }

    public void validateInternalState() {
    if(orderDirection == OrderDirection.EMPTY && ((openVolume != 0 || openPriceSum != 0) && (openVolume <= 0 || openPriceSum <= 0))) {
//     Need to add loggin
//        Example logger.log(problem happened)
        throw new IllegalArgumentException();
    }
    if(pendingBuySize < 0 || pendingSellSize < 0) {
        //     Need to add loggin
//        Example logger.log(problem happened)
        throw new IllegalArgumentException();
    }
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
    bytes.writeLong(uuid);
    bytes.writeInt(symbol);
    bytes.writeInt(currency);
    bytes.writeByte((byte) orderDirection.getMultiplier());
    bytes.writeLong(openVolume);
    bytes.writeLong(openPriceSum);
    bytes.writeLong(profit);
    bytes.writeLong(pendingSellSize);
    bytes.writeLong(pendingBuySize);
    }

    @Override
    public int stateHash() {
        return Objects.hash(
                uuid,
                symbol,
                currency,
                orderDirection,
                openVolume,
                openPriceSum,
                profit,
                pendingSellSize,
                pendingBuySize
        );
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
