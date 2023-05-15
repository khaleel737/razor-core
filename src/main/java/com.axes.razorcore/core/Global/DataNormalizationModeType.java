package com.axes.razorcore.core.Global;

public enum DataNormalizationModeType {

        /// <summary>
        /// No modifications to the asset price at all. For Equities, dividends are paid in cash and splits are applied directly to your portfolio quantity. (0)
        /// </summary>
    RAW(0),
        /// <summary>
        /// Splits and dividends are backward-adjusted into the price of the asset. The price today is identical to the current market price. (1)
        /// </summary>
    ADJUSTED(1),
        /// <summary>
        /// Equity splits are applied to the price adjustment but dividends are paid in cash to your portfolio. This normalization mode allows you to manage dividend payments (e.g. reinvestment) while still giving a smooth time series of prices for indicators. (2)
        /// </summary>
    SPLITADJUSTED(2),
        /// <summary>
        /// Equity splits are applied to the price adjustment and the value of all future dividend payments is added to the initial asset price. (3)
        /// </summary>
    TOTALRETURN(3),
        /// <summary>
        /// Eliminates price jumps between two consecutive contracts, adding a factor based on the difference of their prices. The first contract has the true price. Factor 0. (4)
        /// </summary>
        /// <remarks>First contract is the true one, factor 0</remarks>
    FORWARDPANAMACANAL(4),
        /// <summary>
        /// Eliminates price jumps between two consecutive contracts, adding a factor based on the difference of their prices. The last contract has the true price. Factor 0. (5)
        /// </summary>
        /// <remarks>Last contract is the true one, factor 0</remarks>
    BACKWARDSPANAMACNAL(5),
        /// <summary>
        /// Eliminates price jumps between two consecutive contracts, multiplying the prices by their ratio. The last contract has the true price. Factor 1. (6)
        /// </summary>
        /// <remarks>Last contract is the true one, factor 1</remarks>
    BACKWARDSRATIO(6),
        /// <summary>
        /// Splits and dividends are adjusted into the prices in a given date. Only for history requests. (7)
        /// </summary>
        SCALEDRAW(7);

    private final byte code;

    DataNormalizationModeType(int code) {
        this.code = (byte) code;
    }

    private static DataNormalizationModeType of(final byte code) {
        return switch (code) {
            case 0 -> RAW;
            case 1 -> ADJUSTED;
            case 2 -> SPLITADJUSTED;
            case 3 -> TOTALRETURN;
            case 4 -> FORWARDPANAMACANAL;
            case 5 -> BACKWARDSPANAMACNAL;
            case 6 -> BACKWARDSRATIO;
            case 7 -> SCALEDRAW;
            default -> throw new IllegalArgumentException("Unknown DataFeedEndPoint Type " + code);
        };
    }
}
