package com.axes.razorcore.orderbook;

import com.axes.razorcore.core.IOrder;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.event.MatchEventType;
import com.axes.razorcore.event.MatchTradeEventHandler;
import com.axes.razorcore.utils.SerializationUtils;
import lombok.RequiredArgsConstructor;
import net.openhft.chronicle.bytes.NativeBytes;
import net.openhft.chronicle.wire.Wire;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.axes.razorcore.RazorCore.EVENTS_POOLING;


@RequiredArgsConstructor
public class OrderBookEventsHelper {

    public static final OrderBookEventsHelper NON_POOLED_EVENTS_HELPER = new OrderBookEventsHelper(MatchTradeEventHandler::new);

    private final Supplier<MatchTradeEventHandler> eventChainsSupplier;

    private MatchTradeEventHandler eventsChainHead;

    public MatchTradeEventHandler sendTradeEvent(final IOrder matchingOrder,
                                            final boolean makerCompleted,
                                            final boolean takerCompleted,
                                            final long size,
                                            final long bidderHoldPrice) {

        final MatchTradeEventHandler event = newMatchingEvent();

        event.matchEventType = MatchEventType.TRADE;
        event.section = 0;

        event.activeOrderCompleted = takerCompleted;

        event.matchedPositionsId = matchingOrder.getOrderId();
        event.matchedPositionsUuid = matchingOrder.getUuid();
        event.matchedPositionsCompleted = makerCompleted;

        event.price = matchingOrder.getPrice();
        event.size = size;

        // set order reserved price for correct released EBids
        event.bidderHoldPrice = bidderHoldPrice;

        return event;

    }

    public MatchTradeEventHandler sendReduceEvent(final IOrder order, final long reduceSize, final boolean completed) {
        final MatchTradeEventHandler event = newMatchingEvent();
        event.matchEventType = MatchEventType.REDUCE;
        event.section = 0;
        event.activeOrderCompleted = completed;
        event.matchedPositionsId = 0;
        event.matchedPositionsCompleted = false;
        event.price = order.getPrice();
        event.size = reduceSize;

        event.bidderHoldPrice = order.getReserveBidPrice(); // set order reserved price for correct released EBids

        return event;
    }


    public void attachRejectEvent(final OrderCommand command, final long rejectedSize) {

        final MatchTradeEventHandler event = newMatchingEvent();

        event.matchEventType = MatchEventType.REJECT;

        event.section = 0;

        event.activeOrderCompleted = true;
//        event.activeOrderSeq = cmd.seq;

        event.matchedPositionsId = 0;
        event.matchedPositionsCompleted = false;

        event.price = command.price;
        event.size = rejectedSize;

        event.bidderHoldPrice = command.reserveBidPrice; // set command reserved price for correct released EBids

        // insert event
        event.matchEventType = command.matchTradeEventHandler.matchEventType;
        command.matchTradeEventHandler = event;
    }

    public MatchTradeEventHandler createBinaryEventsChain(final long timestamp,
                                                     final int section,
                                                     final NativeBytes<Void> bytes) {

        long[] dataArray = SerializationUtils.bytesToLongArray(bytes, 5);

        MatchTradeEventHandler firstEvent = null;
        MatchTradeEventHandler lastEvent = null;
        for (int i = 0; i < dataArray.length; i += 5) {

            final MatchTradeEventHandler event = newMatchingEvent();

            event.matchEventType = MatchEventType.BINARY_EVENT;

            event.section = section;
            event.matchedPositionsId = dataArray[i];
            event.matchedPositionsUuid = dataArray[i + 1];
            event.price = dataArray[i + 2];
            event.size = dataArray[i + 3];
            event.bidderHoldPrice = dataArray[i + 4];

            event.matchTradeNextEvent = null;

            if (firstEvent == null) {
                firstEvent = event;
            } else {
                lastEvent.matchTradeNextEvent = event;
            }
            lastEvent = event;
        }

        return firstEvent;
    }


    public static NavigableMap<Integer, Wire> deserializeEvents(final OrderCommand command) {

        final Map<Integer, List<MatchTradeEventHandler>> sections = new HashMap<>();
        command.processMatchTradeEventHandler(evt -> sections.computeIfAbsent(evt.section, k -> new ArrayList<>()).add(evt));

        NavigableMap<Integer, Wire> result = new TreeMap<>();

        sections.forEach((section, events) -> {
            final long[] dataArray = events.stream()
                    .flatMap(evt -> Stream.of(
                            evt.matchedPositionsId,
                            evt.matchedPositionsUuid,
                            evt.price,
                            evt.size,
                            evt.bidderHoldPrice))
                    .mapToLong(s -> s)
                    .toArray();

            final Wire wire = SerializationUtils.longToWire(dataArray);

            result.put(section, wire);
        });


        return result;
    }

    private MatchTradeEventHandler newMatchingEvent() {

        if (EVENTS_POOLING) {
            if (eventsChainHead == null) {
                eventsChainHead = eventChainsSupplier.get();
            }
            final MatchTradeEventHandler res = eventsChainHead;
            eventsChainHead = eventsChainHead.matchTradeNextEvent;
            return res;
        } else {
            return new MatchTradeEventHandler();
        }
    }

}
