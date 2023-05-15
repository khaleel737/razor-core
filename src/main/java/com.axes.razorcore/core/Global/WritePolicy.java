package com.axes.razorcore.core.Global;

public enum WritePolicy {
    /// <summary>
    /// Will overwrite any existing file or zip entry with the new content (0)
    /// </summary>
    OVERWRITE(0),

    /// <summary>
    /// Will inject and merge new content with the existings file content (1)
    /// </summary>
    MERGE(1),

    /// <summary>
    /// Will append new data to the end of the file or zip entry (2)
    /// </summary>
    APPEND(2);

    private final byte code;

    WritePolicy(final int code) {
        this.code = (byte) code;
    }

    public static WritePolicy of(final byte code) {
        return switch (code) {
            case 0 -> OVERWRITE;
            case 1 -> MERGE;
            case 2 -> APPEND;
            default -> throw new IllegalArgumentException("Unknown WritePolicy " + code);
        };
    }
}
