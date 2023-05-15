package com.axes.razorcore.core.Global;

public enum PeriodType {
    TenSeconds(10),
    ThirtySeconds(30),
    OneMinute(60),
    TwoMinutes(120),
    ThreeMinutes(180),
    FiveMinutes(300),
    TenMinutes(600),
    FifteenMinutes(900),
    TwentyMinutes(1200),
    ThirtyMinutes(1800),
    OneHour(3600),
    TwoHours(7200),
    FourHours(14400),
    SixHours(21600);

    private final int value;

    PeriodType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

