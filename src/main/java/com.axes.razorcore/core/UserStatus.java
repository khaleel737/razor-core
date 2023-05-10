package com.axes.razorcore.core;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE(0),
    SUSPENDED(1);

    private byte code;

    UserStatus(int code) {
        this.code = (byte) code;
    }

    public static UserStatus of(byte code) {
        return switch (code) {
            case 0 -> ACTIVE;
            case 1 -> SUSPENDED;
            default -> throw new IllegalArgumentException("Unknown UserStatus " + code);
        };
    }
}
