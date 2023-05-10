package com.axes.razorcore.core;

public enum OrderTimeInForce {
MARKET(0),
    LIMIT(1),
    STOP(2),
    PEG(3),
    ICEBERG(4),
    HIDDEN(5),
    TRAILING_STOP(6),
    LIMIT_STOP(7);

private final byte code;

OrderTimeInForce(final int code) {
    this.code = (byte) code;
}

public static OrderTimeInForce of(final byte code) {
    return switch (code) {
        case 0 -> MARKET;
        case 1 -> LIMIT;
        case 2 -> STOP;
        case 3 -> PEG;
        case 4 -> ICEBERG;
        case 5 -> HIDDEN;
        case 6 -> TRAILING_STOP;
        case 7 -> LIMIT_STOP;
        default -> throw new IllegalArgumentException("Unknown OrderTimeInForce " + code);
    };
}
}
