package com.axes.razorcore.core;

import lombok.Getter;

@Getter
public enum BalanceAdjustedType {

    ADJUSTED(0),
    SUSPENDED(1);

    private byte code;

    BalanceAdjustedType(int code) {
        this.code = (byte) code;
    }

    public static BalanceAdjustedType of(byte code) {
        return switch (code) {
            case 0 -> ADJUSTED;
            case 1 -> SUSPENDED;
            default -> throw new IllegalArgumentException("Unknown BalanceAdjustedType " + code);
        };
    }
}
