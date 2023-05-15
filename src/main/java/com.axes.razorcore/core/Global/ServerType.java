package com.axes.razorcore.core.Global;

public enum ServerType {
    /// <summary>
    /// Additional server (0)
    /// </summary>
    SERVER512(0),

    /// <summary>
    /// Upgraded server (1)
    /// </summary>
    SERVER1024(1),

    /// <summary>
    /// Server with 2048 MB Ram (2)
    /// </summary>
    SERVER2048(2);

    private final byte code;

    ServerType(final int code) {
        this.code = (byte) code;
    }

    public static ServerType of(final byte code) {
        return switch (code) {
            case 0 -> SERVER512;
            case 1 -> SERVER1024;
            case 2 -> SERVER2048;
            default -> throw new IllegalArgumentException("Unknown ServerType " + code);
        };
    }
}
