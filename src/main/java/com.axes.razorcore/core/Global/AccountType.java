package com.axes.razorcore.core.Global;

import lombok.Getter;

@Getter
public enum AccountType {
    /// <summary>
        /// Margin account type (0)
        /// </summary>
        MARGIN(0),

        /// <summary>
        /// Cash account type (1)
        /// </summary>
        CASH(1);

        private final byte code;

    AccountType(final int code) {
        this.code = (byte) code;
    }
    public static AccountType of(final byte code) {
        return switch (code) {
            case 0 -> MARGIN;
            case 1 -> CASH;
            default -> throw new IllegalArgumentException("Unknown Account Type " + code);
        };
    }

}
