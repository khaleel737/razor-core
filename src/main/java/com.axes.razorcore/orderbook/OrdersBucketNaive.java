package com.axes.razorcore.orderbook;

import com.axes.razorcore.core.IOrder;
import com.axes.razorcore.core.Order;
import com.axes.razorcore.core.OrderAction;
import com.axes.razorcore.event.MatchTradeEventHandler;
import com.axes.razorcore.utils.SerializationUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OrdersBucketNaive implements Comparable<OrdersBucketNaive>, WriteBytesMarshallable {

    @Getter
    private final long price;

    private final LinkedHashMap<Long, Order> entries;

    @Getter
    private long totalVolume;

    public OrdersBucketNaive(final long price) {
        this.price = price;
        this.entries = new LinkedHashMap<>();
        this.totalVolume = 0;
    }

    public OrdersBucketNaive(BytesIn bytes) {
        this.price = bytes.readLong();
        this.entries = SerializationUtils.readLongMap(bytes, LinkedHashMap::new, Order::new);
        this.totalVolume = bytes.readLong();
    }

    /**
     * Put a new order into bucket
     *
     * @param order - order
     */
    public void put(Order order) {
        entries.put(order.orderId, order);
        totalVolume += order.size - order.filled;
    }

    /**
     * Remove order from the bucket
     *
     * @param orderId - order id
     * @param uid     - order uid
     * @return order if removed, or null if not found
     */
    public Order remove(long orderId, long uid) {
        Order order = entries.get(orderId);
//        log.debug("removing order: {}", order);
        if (order == null || order.uuid != uid) {
            return null;
        }

        entries.remove(orderId);

        totalVolume -= order.size - order.filled;
        return order;
    }

    /**
     * Collect a list of matching orders starting from eldest records
     * Completely matching orders will be removed, partially matched order kept in the bucked.
     *
     * @param volumeToCollect - volume to collect
     * @param activeOrder     - for getReserveBidPrice
     * @param helper          - events helper
     * @return - total matched volume, events, completed orders to remove
     */
    public MatcherResult match(long volumeToCollect, IOrder activeOrder, OrderBookEventsHelper helper) {

//        log.debug("---- match: {}", volumeToCollect);

        final Iterator<Map.Entry<Long, Order>> iterator = entries.entrySet().iterator();

        long totalMatchingVolume = 0;

        final List<Long> ordersToRemove = new ArrayList<>();

        MatchTradeEventHandler eventsHead = null;
        MatchTradeEventHandler eventsTail = null;

        // iterate through all orders
        while (iterator.hasNext() && volumeToCollect > 0) {
            final Map.Entry<Long, Order> next = iterator.next();
            final Order order = next.getValue();

            // calculate exact volume can fill for this order
//            log.debug("volumeToCollect={} order: s{} f{}", volumeToCollect, order.size, order.filled);
            final long v = Math.min(volumeToCollect, order.size - order.filled);
            totalMatchingVolume += v;
//            log.debug("totalMatchingVolume={} v={}", totalMatchingVolume, v);

            order.filled += v;
            volumeToCollect -= v;
            totalVolume -= v;

            // remove from order book filled orders
            final boolean fullMatch = order.size == order.filled;

            final long bidderHoldPrice = order.action == OrderAction.ASK ? activeOrder.getReserveBidPrice() : order.reserveBidPrice;
            final MatchTradeEventHandler tradeEvent = helper.sendTradeEvent(order, fullMatch, volumeToCollect == 0, v, bidderHoldPrice);

            if (eventsTail == null) {
                eventsHead = tradeEvent;
            } else {
                eventsTail.matchTradeNextEvent = tradeEvent;
            }
            eventsTail = tradeEvent;

            if (fullMatch) {
                ordersToRemove.add(order.orderId);
                iterator.remove();
            }
        }

        return new MatcherResult(eventsHead, eventsTail, totalMatchingVolume, ordersToRemove);
    }

    /**
     * Get number of orders in the bucket
     *
     * @return number of orders in the bucket
     */
    public int getNumOrders() {
        return entries.size();
    }

    /**
     * Reduce size of the order
     *
     * @param reduceSize - size to reduce (difference)
     */
    public void reduceSize(long reduceSize) {

        totalVolume -= reduceSize;
    }

    public void validate() {
        long sum = entries.values().stream().mapToLong(c -> c.size - c.filled).sum();
        if (sum != totalVolume) {
            String msg = String.format("totalVolume=%d calculated=%d", totalVolume, sum);
            throw new IllegalStateException(msg);
        }
    }

    public Order findOrder(long orderId) {
        return entries.get(orderId);
    }

    /**
     * Inefficient method - for testing only
     *
     * @return new array with references to orders, preserving execution queue order
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(entries.values());
    }


    /**
     * execute some action for each order (preserving execution queue order)
     *
     * @param consumer action consumer function
     */
    public void forEachOrder(Consumer<Order> consumer) {
        entries.values().forEach(consumer);
    }

    public String dumpToSingleLine() {
        String orders = getAllOrders().stream()
                .map(o -> String.format("id%d_L%d_F%d", o.orderId, o.size, o.filled))
                .collect(Collectors.joining(", "));

        return String.format("%d : vol:%d num:%d : %s", getPrice(), getTotalVolume(), getNumOrders(), orders);
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeLong(price);
        SerializationUtils.marshallLongMap(entries, bytes);
        bytes.writeLong(totalVolume);
    }

    @Override
    public int compareTo(OrdersBucketNaive other) {
        return Long.compare(this.getPrice(), other.getPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                price,
                Arrays.hashCode(entries.values().toArray(new Order[0])));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof OrdersBucketNaive)) return false;
        OrdersBucketNaive other = (OrdersBucketNaive) obj;
        return price == other.getPrice()
                && getAllOrders().equals(other.getAllOrders());
    }

    @AllArgsConstructor
    public final class MatcherResult {
        public MatchTradeEventHandler eventsChainHead;
        public MatchTradeEventHandler eventsChainTail;
        public long volume;
        public List<Long> ordersToRemove;
    }
}
