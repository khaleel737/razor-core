package com.axes.razorcore.core.Global;

public enum CashBookUpdateType
{
    /// <summary>
    /// A new <see cref="Cash.Symbol"/> was added (0)
    /// </summary>
    ADDED(0),
    /// <summary>
    /// One or more <see cref="Cash"/> instances were removed (1)
    /// </summary>
    REMOVED(1),
    /// <summary>
    /// An existing <see cref="Cash.Symbol"/> was updated (2)
    /// </summary>
    UPDATED(2);

    private final byte code;

    CashBookUpdateType(final int code) {
        this.code = (byte) code;
    }

    public static CashBookUpdateType of(final byte code) {
        return switch (code) {
            case 0 -> ADDED;
            case 1 -> REMOVED;
            case 2 -> UPDATED;
            default -> throw new IllegalArgumentException("Unknow CashBookUpdate Type " + code);
        };
    }
}
