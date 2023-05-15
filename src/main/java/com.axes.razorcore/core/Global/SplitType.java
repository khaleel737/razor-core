package com.axes.razorcore.core.Global;

public enum SplitType {
        /// <summary>
        /// Specifies a warning of an imminent split event (0)
        /// </summary>
        WARNING(0),

        /// <summary>
        /// Specifies the symbol has been split (1)
        /// </summary>
        SPLITOCCURED(1);

    private final byte code;

    SplitType(final int code) {
        this.code = (byte) code;
    }

    public static SplitType of(final byte code) {
        return switch (code) {
            case 0 -> WARNING;
            case 1 -> SPLITOCCURED;
            default -> throw new IllegalArgumentException("Unknown SplitType " + code);
        };
    }
}
