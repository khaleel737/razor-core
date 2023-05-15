package com.axes.razorcore.core.Global;

public enum SettlementType {
    /// <summary>
    /// Physical delivery of the underlying security (0)
    /// </summary>
    PHYSICALDELIVERY(0),

    /// <summary>
    /// Cash is paid/received on settlement (1)
    /// </summary>
    CASH(1);

    private final byte code;

    SettlementType(final int code) {
        this.code = (byte) code;
    }

    public static SettlementType of(final byte code) {
        return switch (code) {
            case 0 -> PHYSICALDELIVERY;
            case 1 -> CASH;
            default -> throw new IllegalArgumentException("Unknown SettlementType " + code);
        };
    }
}
