package com.axes.razorcore.data;

import lombok.Getter;

import java.util.Date;

@Getter
public final class Instant {
    private final long epochSecond;
    private final int nano;

    public static Instant now() {
        return ofEpochMilli(System.currentTimeMillis());
    }

    public static Instant ofEpochMilli(long epochMilli) {
        return new Instant(epochMilli / 1000, (int) ((epochMilli % 1000) * 1_000_000));
    }

    public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
        return new Instant(epochSecond, (int) nanoAdjustment);
    }

    public static Instant ofEpochSecond(long epochSecond) {
        return new Instant(epochSecond, 0);
    }

    public long toEpochMilli() {
        return epochSecond * 1000 + nano / 1_000_000;
    }

    public long getEpochSecond() {
        return epochSecond;
    }

    public int getNano() {
        return nano;
    }

    public Date toDate() {
        return new Date(toEpochMilli());
    }

    private Instant(long epochSecond, int nano) {
        this.epochSecond = epochSecond;
        this.nano = nano;
    }
}

