package com.axes.razorcore.core.Global;

public enum DelistingType {
    /// <summary>
    /// Specifies a warning of an imminent delisting (0)
    /// </summary>
    WARNING(0),

    /// <summary>
    /// Specifies the symbol has been delisted (1)
    /// </summary>
    DELISTED(1);

    private final byte code;

    DelistingType(final int code) {
        this.code = (byte) code;
    }

    public static DelistingType of(final byte code) {
        return switch (code) {
            case 0 -> WARNING;
            case 1 -> DELISTED;
            default -> throw new IllegalArgumentException("Unknown Delisting Type + " + code);
        };
    }
}
