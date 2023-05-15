package com.axes.razorcore.core.Global;

public enum OptionRight {
    /// <summary>
    /// A call option, the right to buy at the strike price (0)
    /// </summary>
    CALL(0),

    /// <summary>
    /// A put option, the right to sell at the strike price (1)
    /// </summary>
    PUT(1);

    private final byte code;

    OptionRight(final int code) {
        this.code = (byte) code;
    }

    public static OptionRight of(final byte code) {
        return switch (code) {
            case 0 -> CALL;
            case 1 -> PUT;
            default -> throw new IllegalArgumentException("Unknown OptionRight " + code);
        };
    }
}
