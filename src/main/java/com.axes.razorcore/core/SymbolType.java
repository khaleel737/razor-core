package com.axes.razorcore.core;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SymbolType {

    EQUITIES(0),
    OPTIONS(1),
    CURRENCY_EXCHANGE_PAIRS(2),
    FUTURES_CONTRACTS(3);

    private byte code;

    SymbolType(int code) {
        this.code = (byte) code;
    }

    public static SymbolType of(int code) {
        return Arrays.stream(values())
                .filter(c -> c.code == (byte) code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown SymbolType " + code));
    }
}
