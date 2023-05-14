package com.axes.razorcore.orderbook;

import com.axes.razorcore.config.LoggingConfiguration;
import com.axes.razorcore.core.IOrder;
import com.axes.razorcore.core.Order;
import com.axes.razorcore.core.OrderAction;
import com.axes.razorcore.core.SymbolSpecification;
import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.cqrs.OrderCommandType;
import com.axes.razorcore.data.L2MarketData;
import com.axes.razorcore.utils.HashingUtils;
import com.axes.razorcore.utils.StateHash;
import exchange.core2.collections.objpool.ObjectsPool;
import lombok.Getter;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface IOrderBook extends WriteBytesMarshallable, StateHash {

    /**
     * Process new order.
     * Depending on price specified (whether the order is marketable),
     * order will be matched to existing opposite GTC orders from the order book.
     * In case of remaining volume (order was not matched completely):
     * IOC - reject it as partially filled.
     * GTC - place as a new limit order into th order book.
     * <p>
     * Rejection chain attached in case of error (to simplify risk handling)
     *
     * @param command - order to match/place
     */
    void newOrder(OrderCommand command);

    /**
     * Cancel order completely.
     * <p>
     * fills command.action  with original original order action
     *
     * @param command - order command
     * @return MATCHING_UNKNOWN_ORDER_ID if order was not found, otherwise SUCCESS
     */
    CommandResultCode cancelOrder(OrderCommand command);

    /**
     * Decrease the size of the order by specific number of lots
     * <p>
     * fills command.action  with original  order action
     *
     * @param command - order command
     * @return MATCHING_UNKNOWN_ORDER_ID if order was not found, otherwise SUCCESS
     */
    CommandResultCode reduceOrder(OrderCommand command);

    /**
     * Move order
     * <p>
     * newPrice - new price (if 0 or same - order will not moved)
     * fills command.action  with original original order action
     *
     * @param command - order command
     * @return MATCHING_UNKNOWN_ORDER_ID if order was not found, otherwise SUCCESS
     */
    CommandResultCode moveOrder(OrderCommand command);

    // testing only ?
    int getOrdersNum(OrderAction action);

    // testing only ?
    long getTotalOrdersVolume(OrderAction action);

    // testing only ?
    IOrder getOrderById(long orderId);

    // testing only - validateInternalState without changing state
    void validateInternalState();

    /**
     * @return actual implementation
     */
    OrderBookImplType getImplementationType();

    /**
     * Search for all orders for specified user.<p>
     * Slow, because order book do not maintain uid-to-order index.<p>
     * Produces garbage.<p>
     * Orders must be processed before doing any other mutable call.<p>
     *
     * @param uid user id
     * @return list of orders
     */
    List<Order> findUserOrders(long uid);

    SymbolSpecification getSymbolSpec();

    Stream<? extends IOrder> askOrdersStream(boolean sorted);

    Stream<? extends IOrder> bidOrdersStream(boolean sorted);

    /**
     * State hash for order books is implementation-agnostic
     * Look {@link IOrderBook#validateInternalState} for full internal state validation for de-serialized objects
     *
     * @return state hash code
     */
    @Override
    default int stateHash() {

        // log.debug("State hash of {}", orderBook.getClass().getSimpleName());
        // log.debug("  Ask orders stream: {}", orderBook.askOrdersStream(true).collect(Collectors.toList()));
        // log.debug("  Ask orders hash: {}", stateHashStream(orderBook.askOrdersStream(true)));
        // log.debug("  Bid orders stream: {}", orderBook.bidOrdersStream(true).collect(Collectors.toList()));
        // log.debug("  Bid orders hash: {}", stateHashStream(orderBook.bidOrdersStream(true)));
        // log.debug("  getSymbolSpec: {}", orderBook.getSymbolSpec());
        // log.debug("  getSymbolSpec hash: {}", orderBook.getSymbolSpec().stateHash());

        return Objects.hash(
                HashingUtils.stateHashStream(askOrdersStream(true)),
                HashingUtils.stateHashStream(bidOrdersStream(true)),
                getSymbolSpec().stateHash());
    }

    /**
     * Obtain current L2 Market Data snapshot
     *
     * @param size max size for each part (ask, bid)
     * @return L2 Market Data snapshot
     */
    default L2MarketData getL2MarketDataSnapshot(final int size) {
        final int asksSize = getTotalAskBucket(size);
        final int bidsSize = getTotalBidBucket(size);
        final L2MarketData data = new L2MarketData(asksSize, bidsSize);
        fillAsks(asksSize, data);
        fillBids(bidsSize, data);
        return data;
    }

    default L2MarketData getL2MarketDataSnapshot() {
        return getL2MarketDataSnapshot(Integer.MAX_VALUE);
    }

    /**
     * Request to publish L2 market data into outgoing disruptor message
     *
     * @param data - pre-allocated object from ring buffer
     */
    default void publishL2MarketDataSnapshot(L2MarketData data) {
        int size = L2MarketData.L2_SIZE;
        fillAsks(size, data);
        fillBids(size, data);
    }

    void fillAsks(int size, L2MarketData data);

    void fillBids(int size, L2MarketData data);

    int getTotalAskBucket(int limit);

    int getTotalBidBucket(int limit);


    static CommandResultCode processCommand(final IOrderBook orderBook, final OrderCommand command) {

        final OrderCommandType commandType = command.commandType;

        if (commandType == OrderCommandType.MOVE_ORDER) {

            return orderBook.moveOrder(command);

        } else if (commandType == OrderCommandType.CANCEL_ORDER) {

            return orderBook.cancelOrder(command);

        } else if (commandType == OrderCommandType.REDUCE_ORDER) {

            return orderBook.reduceOrder(command);

        } else if (commandType == OrderCommandType.PLACE_ORDER) {

            if (command.resultCode == CommandResultCode.VALID_FOR_MATCHING_ENGINE) {
                orderBook.newOrder(command);
                return CommandResultCode.SUCCESS;
            } else {
                return command.resultCode; // no change
            }

        } else if (commandType == OrderCommandType.ORDER_BOOK_REQUEST) {
            int size = (int) command.size;
            command.marketData = orderBook.getL2MarketDataSnapshot(size >= 0 ? size : Integer.MAX_VALUE);
            return CommandResultCode.SUCCESS;

        } else {
            return CommandResultCode.MATCHING_UNSUPPORTED_COMMAND;
        }

    }

    static IOrderBook create(BytesIn bytes, ObjectsPool objectsPool, OrderBookEventsHelper eventsHelper, LoggingConfiguration loggingCfg) {
        switch (OrderBookImplType.of(bytes.readByte())) {
            case NAIVE:
                return new OrderBookNaiveImpl(bytes, loggingCfg);
            case DIRECT:
                return new OrderBookDirectImpl(bytes, objectsPool, eventsHelper, loggingCfg);
            default:
                throw new IllegalArgumentException();
        }
    }

    @FunctionalInterface
    interface OrderBookFactory {

        IOrderBook create(SymbolSpecification spec, ObjectsPool pool, OrderBookEventsHelper eventsHelper, LoggingConfiguration loggingCfg);
    }

    @Getter
    enum OrderBookImplType {
        NAIVE(0),
        DIRECT(2);

        private byte code;

        OrderBookImplType(int code) {
            this.code = (byte) code;
        }

        public static OrderBookImplType of(byte code) {
            switch (code) {
                case 0:
                    return NAIVE;
                case 2:
                    return DIRECT;
                default:
                    throw new IllegalArgumentException("unknown OrderBookImplType:" + code);
            }
        }
    }

}
