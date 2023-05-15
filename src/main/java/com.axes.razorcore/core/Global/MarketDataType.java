package com.axes.razorcore.core.Global;

public enum MarketDataType {

        /// Base market data type (0)
        BASE(0),
        /// TradeBar market data type (OHLC summary bar) (1)
        TRADEBAR(1),
        /// Tick market data type (price-time pair) (2)
        TICK(2),
        /// Data associated with an instrument (3)
        AUXILIRY(3),
        /// QuoteBar market data type (4) [Bid(OHLC), Ask(OHLC) and Mid(OHLC) summary bar]
        QUOTEBAR(4),
        /// Option chain data (5)
        OPTIONCHAIN(5),
        /// Futures chain data (6)
        FUTURESCHAIN(6);

        private final byte code;

        MarketDataType(int code) {
            this.code = (byte) code;
        }

        public static MarketDataType of(final byte code) {
            return switch (code) {
                case 1 -> BASE;
                case 2 -> TRADEBAR;
                case 3 -> TICK;
                case 4 -> AUXILIRY;
                case 5 -> QUOTEBAR;
                case 6 -> OPTIONCHAIN;
                case 7 -> FUTURESCHAIN;
                default -> throw new IllegalArgumentException("Unknown MarketData Type " + code);
            };
        }

}
