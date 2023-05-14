package com.axes.razorcore.orderbook;

import com.axes.razorcore.config.LoggingConfiguration;
import com.axes.razorcore.core.*;
import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.data.L2MarketData;
import com.axes.razorcore.event.MatchTradeEventHandler;
import com.axes.razorcore.utils.SerializationUtils;
import exchange.core2.collections.objpool.ObjectsPool;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class OrderBookNaiveImpl implements IOrderBook {


    private final NavigableMap<Long, OrdersBucketNaive> askBuckets;
    private final NavigableMap<Long, OrdersBucketNaive> bidBuckets;

    private final SymbolSpecification symbolSpec;

    private final LongObjectHashMap<Order> idMap = new LongObjectHashMap<>();

    private final OrderBookEventsHelper eventsHelper;

    private final boolean logDebug;

    public OrderBookNaiveImpl(final SymbolSpecification symbolSpec,
                              final ObjectsPool pool,
                              final OrderBookEventsHelper eventsHelper,
                              final LoggingConfiguration loggingCfg) {

        this.symbolSpec = symbolSpec;
        this.askBuckets = new TreeMap<>();
        this.bidBuckets = new TreeMap<>(Collections.reverseOrder());
        this.eventsHelper = eventsHelper;
        this.logDebug = loggingCfg.getLoggingLevels().contains(LoggingConfiguration.LoggingLevel.LOGGING_MATCHING_DEBUG);
    }

    public OrderBookNaiveImpl(final SymbolSpecification symbolSpec,
                              final LoggingConfiguration loggingCfg) {

        this.symbolSpec = symbolSpec;
        this.askBuckets = new TreeMap<>();
        this.bidBuckets = new TreeMap<>(Collections.reverseOrder());
        this.eventsHelper = OrderBookEventsHelper.NON_POOLED_EVENTS_HELPER;
        this.logDebug = loggingCfg.getLoggingLevels().contains(LoggingConfiguration.LoggingLevel.LOGGING_MATCHING_DEBUG);
    }

    public OrderBookNaiveImpl(final BytesIn bytes, final LoggingConfiguration loggingCfg) {
        this.symbolSpec = new SymbolSpecification(bytes);
        this.askBuckets = SerializationUtils.readLongMap(bytes, TreeMap::new, OrdersBucketNaive::new);
        this.bidBuckets = SerializationUtils.readLongMap(bytes, () -> new TreeMap<>(Collections.reverseOrder()), OrdersBucketNaive::new);

        this.eventsHelper = OrderBookEventsHelper.NON_POOLED_EVENTS_HELPER;
        // reconstruct ordersId-> Order cache
        // TODO check resulting performance
        askBuckets.values().forEach(bucket -> bucket.forEachOrder(order -> idMap.put(order.orderId, order)));
        bidBuckets.values().forEach(bucket -> bucket.forEachOrder(order -> idMap.put(order.orderId, order)));

        this.logDebug = loggingCfg.getLoggingLevels().contains(LoggingConfiguration.LoggingLevel.LOGGING_MATCHING_DEBUG);
        //validateInternalState();
    }

    @Override
    public void newOrder(final OrderCommand command) {

        switch (command.orderType) {
            case GTC:
                newOrderPlaceGtc(command);
                break;
            case IOC:
                newOrderMatchIoc(command);
                break;
            case FOK_BUDGET:
                newOrderMatchFokBudget(command);
                break;
            // TODO IOC_BUDGET and FOK support
            default:
                log.warn("Unsupported order type: {}", command);
                eventsHelper.attachRejectEvent(command, command.size);
        }
    }

    private void newOrderPlaceGtc(final OrderCommand command) {

        final OrderAction action = command.action;
        final long price = command.price;
        final long size = command.size;

        // check if order is marketable (if there are opposite matching orders)
        final long filledSize = tryMatchInstantly(command, subtreeForMatching(action, price), 0, command);
        if (filledSize == size) {
            // order was matched completely - nothing to place - can just return
            return;
        }

        long newOrderId = command.orderId;
        if (idMap.containsKey(newOrderId)) {
            // duplicate order id - can match, but can not place
            eventsHelper.attachRejectEvent(command, command.size - filledSize);
            log.warn("duplicate order id: {}", command);
            return;
        }

        // normally placing regular GTC limit order
        Order orderRecord = new Order(
                command.uuid,
                newOrderId, price,
                filledSize, command.
                timestamp, size,
                filledSize, command.reserveBidPrice,
                action);

        getBucketsByAction(action)
                .computeIfAbsent(price, OrdersBucketNaive::new)
                .put(orderRecord);

        idMap.put(newOrderId, orderRecord);
    }

    private void newOrderMatchIoc(final OrderCommand command) {

        final long filledSize = tryMatchInstantly(command, subtreeForMatching(command.action, command.price), 0, command);

        final long rejectedSize = command.size - filledSize;

        if (rejectedSize != 0) {
            // was not matched completely - send reject for not-completed IoC order
            eventsHelper.attachRejectEvent(command, rejectedSize);
        }
    }

    private void newOrderMatchFokBudget(final OrderCommand command) {

        final long size = command.size;

        final SortedMap<Long, OrdersBucketNaive> subtreeForMatching =
                command.action == OrderAction.ASK ? bidBuckets : askBuckets;

        final Optional<Long> budget = checkBudgetToFill(size, subtreeForMatching);

        if (logDebug) log.debug("Budget calc: {} requested: {}", budget, command.price);

        if (budget.isPresent() && isBudgetLimitSatisfied(command.action, budget.get(), command.price)) {
            tryMatchInstantly(command, subtreeForMatching, 0, command);
        } else {
            eventsHelper.attachRejectEvent(command, size);
        }
    }

    private boolean isBudgetLimitSatisfied(final OrderAction orderAction, final long calculated, final long limit) {
        return calculated == limit || (orderAction == OrderAction.BID ^ calculated > limit);
    }


    private Optional<Long> checkBudgetToFill(
            long size,
            final SortedMap<Long, OrdersBucketNaive> matchingBuckets) {

        long budget = 0;

        for (final OrdersBucketNaive bucket : matchingBuckets.values()) {

            final long availableSize = bucket.getTotalVolume();
            final long price = bucket.getPrice();

            if (size > availableSize) {
                size -= availableSize;
                budget += availableSize * price;
                if (logDebug) log.debug("add    {} * {} -> {}", price, availableSize, budget);
            } else {
                final long result = budget + size * price;
                if (logDebug) log.debug("return {} * {} -> {}", price, size, result);
                return Optional.of(result);
            }
        }
        if (logDebug) log.debug("not enough liquuidity to fill size={}", size);
        return Optional.empty();
    }

    private SortedMap<Long, OrdersBucketNaive> subtreeForMatching(final OrderAction action, final long price) {
        return (action == OrderAction.ASK ? bidBuckets : askBuckets)
                .headMap(price, true);
    }

    /**
     * Match the order instantly to specified sorted buckets map
     * Fully matching orders are removed from orderId index
     * Should any trades occur - they sent to tradesConsumer
     *
     * @param activeOrder     - GTC or IOC order to match
     * @param matchingBuckets - sorted buckets map
     * @param filled          - current 'filled' value for the order
     * @param triggerCmd      - triggered command (taker)
     * @return new filled size
     */
    private long tryMatchInstantly(
            final IOrder activeOrder,
            final SortedMap<Long, OrdersBucketNaive> matchingBuckets,
            long filled,
            final OrderCommand triggerCommand) {

//        log.info("matchInstantly: {} {}", order, matchingBuckets);

        if (matchingBuckets.size() == 0) {
            return filled;
        }

        final long orderSize = activeOrder.getSize();

        MatchTradeEventHandler eventsTail = null;

        List<Long> emptyBuckets = new ArrayList<>();
        for (final OrdersBucketNaive bucket : matchingBuckets.values()) {

//            log.debug("Matching bucket: {} ...", bucket);
//            log.debug("... with order: {}", activeOrder);

            final long sizeLeft = orderSize - filled;

            final OrdersBucketNaive.MatcherResult bucketMatchings = bucket.match(sizeLeft, activeOrder, eventsHelper);

            bucketMatchings.ordersToRemove.forEach(idMap::remove);

            filled += bucketMatchings.volume;

            // attach chain received from bucket matcher
            if (eventsTail == null) {
                triggerCommand.matchTradeEventHandler = bucketMatchings.eventsChainHead;
            } else {
                eventsTail.matchTradeNextEvent = bucketMatchings.eventsChainHead;
            }
            eventsTail = bucketMatchings.eventsChainTail;


            long price = bucket.getPrice();

            // remove empty buckets
            if (bucket.getTotalVolume() == 0) {
                emptyBuckets.add(price);
            }

            if (filled == orderSize) {
                // enough matched
                break;
            }
        }

        // remove empty buckets (is it necessary?)
        // TODO can remove through iterator ??
        emptyBuckets.forEach(matchingBuckets::remove);

//        log.debug("emptyBuckets: {}", emptyBuckets);
//        log.debug("matchingRecords: {}", matchingRecords);

        return filled;
    }

    /**
     * Remove an order.<p>
     *
     * @param command cancel command (orderId - order to remove)
     * @return true if order removed, false if not found (can be removed/matched earlier)
     */
    public CommandResultCode cancelOrder(OrderCommand command) {
        final long orderId = command.orderId;

        final Order order = idMap.get(orderId);
        if (order == null || order.uuid != command.uuid) {
            // order already matched and removed from order book previously
            return CommandResultCode.MATCHING_UNKNOWN_ORDER_ID;
        }

        // now can remove it
        idMap.remove(orderId);

        final NavigableMap<Long, OrdersBucketNaive> buckets = getBucketsByAction(order.action);
        final long price = order.price;
        final OrdersBucketNaive ordersBucket = buckets.get(price);
        if (ordersBucket == null) {
            // not possible state
            throw new IllegalStateException("Can not find bucket for order price=" + price + " for order " + order);
        }

        // remove order and whole bucket if its empty
        ordersBucket.remove(orderId, command.uuid);
        if (ordersBucket.getTotalVolume() == 0) {
            buckets.remove(price);
        }

        // send reduce event
        command.matchTradeEventHandler = eventsHelper.sendReduceEvent(order, order.getSize() - order.getFilled(), true);

        // fill action fields (for events handling)
        command.action = order.getAction();

        return CommandResultCode.SUCCESS;
    }

    @Override
    public CommandResultCode reduceOrder(OrderCommand command) {
        final long orderId = command.orderId;
        final long requestedReduceSize = command.size;

        if (requestedReduceSize <= 0) {
            return CommandResultCode.MATCHING_REDUCE_FAILED_WRONG_SIZE;
        }

        final Order order = idMap.get(orderId);
        if (order == null || order.uuid != command.uuid) {
            // already matched, moved or cancelled
            return CommandResultCode.MATCHING_UNKNOWN_ORDER_ID;
        }

        final long remainingSize = order.size - order.filled;
        final long reduceBy = Math.min(remainingSize, requestedReduceSize);

        final NavigableMap<Long, OrdersBucketNaive> buckets = getBucketsByAction(order.action);
        final OrdersBucketNaive ordersBucket = buckets.get(order.price);
        if (ordersBucket == null) {
            // not possible state
            throw new IllegalStateException("Can not find bucket for order price=" + order.price + " for order " + order);
        }

        final boolean canRemove = (reduceBy == remainingSize);

        if (canRemove) {

            // now can remove order
            idMap.remove(orderId);

            // canRemove order and whole bucket if it is empty
            ordersBucket.remove(orderId, command.uuid);
            if (ordersBucket.getTotalVolume() == 0) {
                buckets.remove(order.price);
            }

        } else {

            order.size -= reduceBy;
            ordersBucket.reduceSize(reduceBy);
        }

        // send reduce event
        command.matchTradeEventHandler = eventsHelper.sendReduceEvent(order, reduceBy, canRemove);

        // fill action fields (for events handling)
        command.action = order.getAction();

        return CommandResultCode.SUCCESS;
    }

    @Override
    public CommandResultCode moveOrder(OrderCommand command) {

        final long orderId = command.orderId;
        final long newPrice = command.price;

        final Order order = idMap.get(orderId);
        if (order == null || order.uuid != command.uuid) {
            // already matched, moved or cancelled
            return CommandResultCode.MATCHING_UNKNOWN_ORDER_ID;
        }

        final long price = order.price;
        final NavigableMap<Long, OrdersBucketNaive> buckets = getBucketsByAction(order.action);
        final OrdersBucketNaive bucket = buckets.get(price);

        // fill action fields (for events handling)
        command.action = order.getAction();

        // reserved price risk check for exchange bids
        if (symbolSpec.type == SymbolType.CURRENCY_EXCHANGE_PAIRS && order.action == OrderAction.BID && command.price > order.reserveBidPrice) {
            return CommandResultCode.MATCHING_MOVE_FAILED_PRICE_OVER_RISK_LIMIT;
        }

        // take order out of the original bucket and clean bucket if its empty
        bucket.remove(orderId, command.uuid);

        if (bucket.getTotalVolume() == 0) {
            buckets.remove(price);
        }

        order.price = newPrice;

        // try match with new price
        final SortedMap<Long, OrdersBucketNaive> matchingArea = subtreeForMatching(order.action, newPrice);
        final long filled = tryMatchInstantly(order, matchingArea, order.filled, command);
        if (filled == order.size) {
            // order was fully matched (100% marketable) - removing from order book
            idMap.remove(orderId);
            return CommandResultCode.SUCCESS;
        }
        order.filled = filled;

        // if not filled completely - put it into corresponding bucket
        final OrdersBucketNaive anotherBucket = buckets.computeIfAbsent(newPrice, p -> {
            OrdersBucketNaive b = new OrdersBucketNaive(p);
            return b;
        });
        anotherBucket.put(order);

        return CommandResultCode.SUCCESS;
    }

    /**
     * Get bucket by order action
     *
     * @param action - action
     * @return bucket - navigable map
     */
    private NavigableMap<Long, OrdersBucketNaive> getBucketsByAction(OrderAction action) {
        return action == OrderAction.ASK ? askBuckets : bidBuckets;
    }


    /**
     * Get order from internal map
     *
     * @param orderId - order Id
     * @return order from map
     */
    @Override
    public IOrder getOrderById(long orderId) {
        return idMap.get(orderId);
    }

    @Override
    public void fillAsks(final int size, L2MarketData data) {
        if (size == 0) {
            data.askSize = 0;
            return;
        }

        int i = 0;
        for (OrdersBucketNaive bucket : askBuckets.values()) {
            data.askPrices[i] = bucket.getPrice();
            data.askVolumes[i] = bucket.getTotalVolume();
            data.askOrders[i] = bucket.getNumOrders();
            if (++i == size) {
                break;
            }
        }
        data.askSize = i;
    }

    @Override
    public void fillBids(final int size, L2MarketData data) {
        if (size == 0) {
            data.bidSize = 0;
            return;
        }

        int i = 0;
        for (OrdersBucketNaive bucket : bidBuckets.values()) {
            data.bidPrices[i] = bucket.getPrice();
            data.bidVolumes[i] = bucket.getTotalVolume();
            data.bidOrders[i] = bucket.getNumOrders();
            if (++i == size) {
                break;
            }
        }
        data.bidSize = i;
    }

    @Override
    public int getTotalAskBucket(final int limit) {
        return Math.min(limit, askBuckets.size());
    }

    @Override
    public int getTotalBidBucket(final int limit) {
        return Math.min(limit, bidBuckets.size());
    }

    @Override
    public void validateInternalState() {
        askBuckets.values().forEach(OrdersBucketNaive::validate);
        bidBuckets.values().forEach(OrdersBucketNaive::validate);
    }

    @Override
    public OrderBookImplType getImplementationType() {
        return OrderBookImplType.NAIVE;
    }

    @Override
    public List<Order> findUserOrders(final long uuid) {
        List<Order> list = new ArrayList<>();
        Consumer<OrdersBucketNaive> bucketConsumer = bucket -> bucket.forEachOrder(order -> {
            if (order.uuid == uuid) {
                list.add(order);
            }
        });
        askBuckets.values().forEach(bucketConsumer);
        bidBuckets.values().forEach(bucketConsumer);
        return list;
    }

    @Override
    public SymbolSpecification getSymbolSpec() {
        return symbolSpec;
    }

    @Override
    public Stream<IOrder> askOrdersStream(final boolean sorted) {
        return askBuckets.values().stream().flatMap(bucket -> bucket.getAllOrders().stream());
    }

    @Override
    public Stream<IOrder> bidOrdersStream(final boolean sorted) {
        return bidBuckets.values().stream().flatMap(bucket -> bucket.getAllOrders().stream());
    }

    // for testing only
    @Override
    public int getOrdersNum(OrderAction action) {
        final NavigableMap<Long, OrdersBucketNaive> buckets = action == OrderAction.ASK ? askBuckets : bidBuckets;
        return buckets.values().stream().mapToInt(OrdersBucketNaive::getNumOrders).sum();
//        int askOrders = askBuckets.values().stream().mapToInt(OrdersBucketNaive::getNumOrders).sum();
//        int bidOrders = bidBuckets.values().stream().mapToInt(OrdersBucketNaive::getNumOrders).sum();
        //log.debug("idMap:{} askOrders:{} bidOrders:{}", idMap.size(), askOrders, bidOrders);
//        int knownOrders = idMap.size();
//        assert knownOrders == askOrders + bidOrders : "inconsistent known orders";
    }

    @Override
    public long getTotalOrdersVolume(OrderAction action) {
        final NavigableMap<Long, OrdersBucketNaive> buckets = action == OrderAction.ASK ? askBuckets : bidBuckets;
        return buckets.values().stream().mapToLong(OrdersBucketNaive::getTotalVolume).sum();
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeByte(getImplementationType().getCode());
        symbolSpec.writeMarshallable(bytes);
        SerializationUtils.marshallLongMap(askBuckets, bytes);
        SerializationUtils.marshallLongMap(bidBuckets, bytes);
    }

}
