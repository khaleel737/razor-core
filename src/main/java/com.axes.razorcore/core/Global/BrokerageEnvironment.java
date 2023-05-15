package com.axes.razorcore.core.Global;

import lombok.Getter;

@Getter
public enum BrokerageEnvironment  {
    /// <summary>
    /// Live trading (0)
    /// </summary>
    LIVE(0),

        /// <summary>
        /// Paper trading (1)
        /// </summary>
    PAPER(1);

    private final byte code;

    BrokerageEnvironment(final int code) {
        this.code = (byte) code;
    }

private static BrokerageEnvironment of(final byte code) {
        return switch (code) {
            case 1 -> LIVE;
            case 2 -> PAPER;
            default -> throw new IllegalArgumentException("Unknown BrokerageEnvironment Type " + code);
        };
}
}
