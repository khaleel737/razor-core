package com.axes.razorcore.core.Global;

public enum DataFeedEndpointType {
    /// Backtesting Datafeed Endpoint (0)
    BACKTESTING(0),
    /// Loading files off the local system (1)
    FILESYSTEM(1),
    /// Getting datafeed from a QC-Live-Cloud (2)
    LIVETRADING(2),
    /// Database (3)
    DATABASE(3);

    private final byte code;

    DataFeedEndpointType(final int code) {
        this.code = (byte) code;
    }

    private static DataFeedEndpointType of(final byte code) {
        return switch (code) {
            case 0 -> BACKTESTING;
            case 1 -> FILESYSTEM;
            case 2 -> LIVETRADING;
            case 3 -> DATABASE;
            default -> throw new IllegalArgumentException("Unknown DataFeedEndPoint Type " + code);
        };
    }
}
