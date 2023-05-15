package com.axes.razorcore.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderDirection {
    LONG(1),
    SHORT(-1),
    EMPTY(0);


    private int multiplier;

    public static OrderDirection of(OrderAction direction) {
        return direction == OrderAction.BID ? LONG : SHORT;
    }

    public static OrderDirection of(byte code) {
        return switch (code) {
            case 1 -> LONG;
            case -1 -> SHORT;
            case 0 -> EMPTY;
            default -> throw new IllegalArgumentException("Unknown Order Direction " + code);
        };
    }

    public boolean isOppositeOrderDirection(OrderAction direction) {
        return (this == OrderDirection.LONG && direction == OrderAction.ASK) || (this == OrderDirection.SHORT && direction == OrderAction.BID);
    }

    public boolean isSameOrderDirection(OrderDirection direction) {
        return false;
    }
}
