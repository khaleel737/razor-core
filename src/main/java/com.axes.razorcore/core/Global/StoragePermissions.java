package com.axes.razorcore.core.Global;


public enum StoragePermissions {
        /// Public Storage Permissions (0)
        PUBLIC(0),

        /// Authenticated Read Storage Permissions (1)
        AUTHENTICATED(1);

    private final byte code;

    StoragePermissions(final int code) {
        this.code = (byte) code;
    }

    public static StoragePermissions of(final byte code) {
        return switch (code) {
            case 0 -> PUBLIC;
            case 1 -> AUTHENTICATED;
            default -> throw new IllegalArgumentException("Unknown StoragePermissions " + code);
        };
    }
}
