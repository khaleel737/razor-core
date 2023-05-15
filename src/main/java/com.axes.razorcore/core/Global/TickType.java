package com.axes.razorcore.core.Global;

public enum TickType {
    /// Trade type tick object (0)
    TRADE(0),
    /// Quote type tick object (1)
    QUOTE(1),
    /// Open Interest type tick object (for options, futures) (2)
    OPENINTEREST(2)

    private final byte code;

    TickType(int code) {
        this.code = (byte) code;
    }

    public static TickType of(final byte code) {
        return switch (code) {
            case 0 -> TRADE;
            case 1 -> QUOTE;
            case 2 -> OPENINTEREST;
            default -> throw new IllegalArgumentException("Unknown TickType " + code);
        };
    }
}
