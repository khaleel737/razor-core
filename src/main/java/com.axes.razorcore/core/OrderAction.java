package com.axes.razorcore.core;

import lombok.Getter;

@Getter
public enum OrderAction {
    ASK(0),
    BID(1);

    private byte code;

    OrderAction(int code) {
        this.code = (byte) code;
    }

    public static OrderAction of(byte code) {
        return switch(code) {
            case 0 -> ASK;
            case 1 -> BID;
            default -> throw new IllegalArgumentException("Unknown Order Action " + code);
        };
    }

    public OrderAction opposite() {
        return this == ASK ? BID : ASK;
    }

}
