package com.axes.razorcore.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchTradeEventHandler {

    public MatchEventType matchEventType;
    public int section;
    public boolean activeOrderCompleted;

//    Matched Orders
    public long matchedPositionsId;
    public long matchedPositionsUuid;
    public boolean matchedPositionsCompleted;

    public long price;
    public long size;

    public long bidderHoldPrice;

    public MatchTradeEventHandler matchTradeNextEvent;

//    TESTING
public MatchTradeEventHandler copy() {
    MatchTradeEventHandler eventType = new MatchTradeEventHandler();
    eventType.matchEventType = this.matchEventType;
    eventType.section = this.section;
    eventType.activeOrderCompleted = this.activeOrderCompleted;
    eventType.matchedPositionsId = this.matchedPositionsId;
    eventType.matchedPositionsUuid = this.matchedPositionsUuid;
    eventType.matchedPositionsCompleted = this.matchedPositionsCompleted;
    eventType.price = this.price;
    eventType.size = this.size;
//        evt.timestamp = this.timestamp;
    eventType.bidderHoldPrice = this.bidderHoldPrice;
    return eventType;
}

    // testing only
    public MatchTradeEventHandler findTail() {
        MatchTradeEventHandler tail = this;
        while (tail.matchTradeNextEvent != null) {
            tail = tail.matchTradeNextEvent;
        }
        return tail;
    }

    public int getChainSize() {
        MatchTradeEventHandler tail = this;
        int count = 1;
        while(tail.matchTradeNextEvent != null) {
            tail = tail.matchTradeNextEvent;
            count++;
        }
        return count;
    }

    public static MatchTradeEventHandler createChainEvent(int chainLength) {
        final MatchTradeEventHandler head = new MatchTradeEventHandler();
        MatchTradeEventHandler prev = head;
        for(int i = 1; i < chainLength; i++) {
            MatchTradeEventHandler nextEvent = new MatchTradeEventHandler();
            prev.matchTradeNextEvent = nextEvent;
            prev = nextEvent;
        }
        return head;
    }

    public static List<MatchTradeEventHandler> asList(MatchTradeEventHandler nextEvent) {
        List<MatchTradeEventHandler> eventList = new ArrayList<>();
        while (nextEvent != null) {
            eventList.add(nextEvent);
            nextEvent = nextEvent.matchTradeNextEvent;
        }
        return eventList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj == null) return false;
        if(!(obj instanceof MatchTradeEventHandler otherEvent)) return false;
        return section == otherEvent.section &&
                activeOrderCompleted == otherEvent.activeOrderCompleted &&
                matchedPositionsId == otherEvent.matchedPositionsId &&
                matchedPositionsUuid == otherEvent.matchedPositionsUuid &&
                matchedPositionsCompleted == otherEvent.matchedPositionsCompleted &&
                price == otherEvent.price &&
                size == otherEvent.size &&
                bidderHoldPrice == otherEvent.bidderHoldPrice &&
                ((matchTradeNextEvent == null && otherEvent.matchTradeNextEvent == null) || (matchTradeNextEvent != null && matchTradeNextEvent.equals(otherEvent.matchTradeNextEvent)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                section,
                activeOrderCompleted,
                matchedPositionsId,
                matchedPositionsUuid,
                matchedPositionsCompleted,
                price,
                size,
                bidderHoldPrice,
                matchTradeNextEvent
        );
    }

    @Override
    public String toString() {
        return "MatchTradeEventHandler{" +
                "matchEventType=" + matchEventType +
                ", section=" + section +
                ", activeOrderCompleted=" + activeOrderCompleted +
                ", matchedPositionsId=" + matchedPositionsId +
                ", matchedPositionsUuid=" + matchedPositionsUuid +
                ", matchedPositionsCompleted=" + matchedPositionsCompleted +
                ", price=" + price +
                ", size=" + size +
                ", bidderHoldPrice=" + bidderHoldPrice +
                ", matchTradeNextEvent=" + matchTradeNextEvent +
                '}';
    }

}
