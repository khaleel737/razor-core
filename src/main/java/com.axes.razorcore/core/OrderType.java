package com.axes.razorcore.core;

public enum OrderType {
    GTC(0),
    DAY(1),
    GTT(2),
    GTD(3),
    GFD(4),
    IOC(5),
    IOC_BUDGET(6),
    FOK(7),
    FOK_BUDGET(8);

    private final byte code;

    OrderType(final int code) {
        this.code = (byte) code;
    }
    public static OrderType of(final byte code) {
        return switch (code) {
            case 0 -> GTC;
            case 1 -> DAY;
            case 2 -> GTT;
            case 3 -> GTD;
            case 4 -> GFD;
            case 5 -> IOC;
            case 6 -> IOC_BUDGET;
            case 7 -> FOK;
            case 8 -> FOK_BUDGET;
            default -> throw new IllegalArgumentException("Unknown OrderType " + code);
        };
    }



}
