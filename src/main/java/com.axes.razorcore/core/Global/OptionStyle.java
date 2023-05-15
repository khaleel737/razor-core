package com.axes.razorcore.core.Global;

public enum OptionStyle {
    /// <summary>
    /// American style options are able to be exercised at any time on or before the expiration date (0)
    /// </summary>
    AMERICAN(0),

    /// <summary>
    /// European style options are able to be exercised on the expiration date only (1)
    /// </summary>
    EUROPEAN(1);

    private final byte code;

    OptionStyle(final int code) {
        this.code = (byte) code;
    }

    public static OptionStyle of(final byte code) {
        return switch (code) {
            case 0 -> AMERICAN;
            case 1 -> EUROPEAN;
            default -> throw new IllegalArgumentException("Unknown OptionStyle " + code);
        };
    }
}
