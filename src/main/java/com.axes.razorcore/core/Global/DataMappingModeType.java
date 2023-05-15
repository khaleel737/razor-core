package com.axes.razorcore.core.Global;

public enum DataMappingModeType {

        /// <summary>
        /// The contract maps on the previous day of expiration of the front month (0)
        /// </summary>
        LASTTRADINGDAY(0),
        /// <summary>
        /// The contract maps on the first date of the delivery month of the front month. If the contract expires prior to this date,
        /// then it rolls on the contract's last trading date instead (1)
        /// </summary>
        /// <remarks>For example, the Crude Oil WTI (CL) 'DEC 2021 CLZ1' contract expires on November, 19 2021, so the mapping date will be its expiration date.</remarks>
        /// <remarks>Another example is the Corn 'DEC 2021 ZCZ1' contract, which expires on December, 14 2021, so the mapping date will be December 1, 2021.</remarks>
        FIRSTDAYMONTH(1),
        /// <summary>
        /// The contract maps when the following back month contract has a higher open interest that the current front month (2)
        /// </summary>
        OPENINTEREST(2),
        /// <summary>
        /// The contract maps when any of the back month contracts of the next year have a higher volume that the current front month (3)
        /// </summary>
        OPENINTERESTANNUAL(3);

    private final byte code;

    DataMappingModeType(int code) {
        this.code = (byte) code;
    }

    private static DataMappingModeType of(final byte code) {
        return switch (code) {
            case 0 -> LASTTRADINGDAY;
            case 1 -> FIRSTDAYMONTH;
            case 2 -> OPENINTEREST;
            case 3 -> OPENINTERESTANNUAL;
            default -> throw new IllegalArgumentException("Unknown DataFeedEndPoint Type " + code);
        };
    }

}
