package com.axes.razorcore.core.Global;

import lombok.Getter;

@Getter
public enum SecurityType {
        /// <summary>
        /// Base class for all security types (0)
        /// </summary>
        BASE(0),

        /// <summary>
        /// US Equity Security (1)
        /// </summary>
        EQUITY(1),

        /// <summary>
        /// Option Security Type (2)
        /// </summary>
        OPTION(2),

        /// <summary>
        /// Commodity Security Type (3)
        /// </summary>
        COMMODITY(3),

        /// <summary>
        /// FOREX Security (4)
        /// </summary>
        FOREX(4),

        /// <summary>
        /// Future Security Type (5)
        /// </summary>
        FUTURE(5),

        /// <summary>
        /// Contract For a Difference Security Type (6)
        /// </summary>
        CFD(6),

        /// <summary>
        /// Cryptocurrency Security Type (7)
        /// </summary>
        CRYPTO(7),

        /// <summary>
        /// Futures Options Security Type (8)
        /// </summary>
        /// <remarks>
        /// Futures options function similar to equity options, but with a few key differences.
        /// Firstly, the contract unit of trade is 1x, rather than 100x. This means that each
        /// option represents the right to buy or sell 1 future contract at expiry/exercise.
        /// The contract multiplier for Futures Options plays a big part in determining the premium
        /// of the option, which can also differ from the underlying future's multiplier.
        /// </remarks>
        FUTUREOPTION(8),

        /// <summary>
        /// Index Security Type (9)
        /// </summary>
        INDEX(9),

        /// <summary>
        /// Index Option Security Type (10)
        /// </summary>
        /// <remarks>
        /// For index options traded on American markets, they tend to be European-style options and are Cash-settled.
        /// </remarks>
        INDEXOPTION(10),

        /// <summary>
        /// Crypto futures
        /// </summary>
        CRYPTOFUTURE(11);

        private final byte code;

        SecurityType(final int code) {
            this.code = (byte) code;
        }

        public static SecurityType of(final byte code) {
            return switch (code) {
                case 0 -> BASE;
                case 1 -> EQUITY;
                case 2 -> OPTION;
                case 3 -> COMMODITY;
                case 4 -> FOREX;
                case 5 -> FUTURE;
                case 6 -> CFD;
                case 7 -> CRYPTO;
                case 8 -> FUTUREOPTION;
                case 9 -> INDEX;
                case 10 -> INDEXOPTION;
                case 11 -> CRYPTOFUTURE;
                default -> throw new IllegalArgumentException("Unknown SecurityType " + code);
            };
        }

}
