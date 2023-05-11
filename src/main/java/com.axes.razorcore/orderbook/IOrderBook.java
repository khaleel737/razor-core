package com.axes.razorcore.orderbook;

import com.axes.razorcore.core.*;
import com.axes.razorcore.data.CommandResultCode;
import com.axes.razorcore.data.L2MarketData;
import com.axes.razorcore.data.OrderCommand;
import com.axes.razorcore.utils.HashingUtils;
import com.axes.razorcore.utils.StateHash;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface IOrderBook extends WriteBytesMarshallable, StateHash {

    void newOrder(OrderCommand command);
    CommandResultCode cancelOrder(OrderCommand command);
    CommandResultCode reduceOrder(OrderCommand command);
    CommandResultCode moveOrder(OrderCommand command);

    int getOrdersNumber(OrderAction orderAction);

    long getTotalOrdersVolume(OrderAction orderAction);

    IOrder getOrderById(long orderId);

    void validateInternalState();

    OrderBookImplType getImplementationType();

    List<Order> findUserOrders(long uuid);

    SymbolSpecification getSymbolSpecification();

    Stream<? extends IOrder> bidOrdersStream(boolean sorted);
    Stream<? extends IOrder> askOrdersStream(boolean sorted);

    @Override
    default int stateHash() {
     return Objects.hash(
             HashingUtils.stateHashStream(bidOrdersStream(true)),
             HashingUtils.stateHashStream(askOrdersStream(true)),
             getSymbolSpecification().stateHash()
     );
    }

    default L2MarketData getL2MarketDataSnapShot(final int size) {
        final int bidSize = getTotalBidBucket(size);
        final int askSize = getTotalAskBucket(size);
        final L2MarketData l2MarketData = new L2MarketData(askSize, bidSize);
        fillBids(bidSize, l2MarketData);
        fillAsks(askSize, l2MarketData);
        return l2MarketData;
    }

    default L2MarketData getL2MarketDataSnapShot() {
        return getL2MarketDataSnapShot(Integer.MAX_VALUE);
    }

    default void publishL2MarketDataSnapShot(L2MarketData l2MarketData) {
        int size = L2MarketData.L2_SIZE;
        fillBids(size, l2MarketData);
        fillAsks(size, l2MarketData);
    }

    void fillBids(int size, L2MarketData l2MarketData);
    void fillAsks(int size, L2MarketData l2MarketData);

    int getTotalBidBucket(int limit);
    int getTotalAskBucket(int limit);

    static CommandResultCode processCommands(final IOrderBook orderBook, final OrderCommand orderCommand) {
        final OrderCommandType orderCommandType = orderCommand.
    }

}
