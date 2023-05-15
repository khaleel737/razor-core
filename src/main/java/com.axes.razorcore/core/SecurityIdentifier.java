package com.axes.razorcore.core;

import com.axes.razorcore.core.Global.OptionRight;
import com.axes.razorcore.core.Global.OptionStyle;
import com.axes.razorcore.core.Global.SecurityType;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@AllArgsConstructor
@Builder
@Slf4j
public class SecurityIdentifier implements Comparable<SecurityIdentifier>, Cloneable {
    private static final String EMPTY_SYMBOL = "";
    private static final BigDecimal DEFAULT_STRIKE_PRICE = BigDecimal.ZERO;
    private static final OptionRight DEFAULT_OPTION_RIGHT = OptionRight.Put;
    private static final OptionStyle DEFAULT_OPTION_STYLE = OptionStyle.American;
    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0);
    private static final int SECURITY_TYPE_WIDTH = 100;
    private static final int SECURITY_TYPE_OFFSET = 1;
    private static final int MARKET_WIDTH = 1000;
    private static final int MARKET_OFFSET = SECURITY_TYPE_OFFSET * SECURITY_TYPE_WIDTH;
    private static final int STRIKE_SCALE_WIDTH = 100;
    private static final int STRIKE_SCALE_OFFSET = MARKET_OFFSET * MARKET_WIDTH;
    private static final int STRIKE_WIDTH = 1000000;
    private static final int STRIKE_OFFSET = STRIKE_SCALE_OFFSET * STRIKE_SCALE_WIDTH;
    private static final int OPTION_STYLE_WIDTH = 10;
    private static final int OPTION_STYLE_OFFSET = STRIKE_OFFSET * STRIKE_WIDTH;
    private static final int DAYS_WIDTH = 100000;
    private static final int DAYS_OFFSET = OPTION_STYLE_OFFSET * OPTION_STYLE_WIDTH;
    private static final int PUT_CALL_OFFSET = DAYS_OFFSET * DAYS_WIDTH;
    private static final int PUT_CALL_WIDTH = 10;
    private static final ImmutableSet<Character> INVALID_SYMBOL_CHARACTERS = ImmutableSet.of('|', ' ');

    private static final BiMap<Integer, Market> MARKET_MAP = ImmutableBiMap.<Integer, Market>builder()
            .put(1, Market.BTC) // Example market mapping
            .build();

    private static final Map<String, SecurityIdentifier> CACHE = new HashMap<>();

    private final String symbol;
    private final long properties;
    private final SecurityIdentifier underlying;
    private BigDecimal strikePrice;
    private OptionStyle optionStyle;
    private OptionRight optionRight;
    private LocalDate date;
    private int hashCode;

    public static final SecurityIdentifier EMPTY = new SecurityIdentifier(EMPTY_SYMBOL, 0);
    public static final SecurityIdentifier NONE = new SecurityIdentifier("NONE", 0);
    public static final SecurityIdentifier DEFAULT = new SecurityIdentifier(EMPTY_SYMBOL, 0);
    public static final BigDecimal STRIKE_DEFAULT_SCALE_EXPANDED = BigDecimal.TEN.pow(4);

    private SecurityIdentifier(String symbol, long properties) {
        this(symbol, properties, null);
    }

    private SecurityIdentifier(String symbol, long properties, SecurityIdentifier underlying) {
        this.symbol = symbol;
        this.properties = properties;
        this.underlying = underlying;
    }

    public static SecurityIdentifier generateOption(LocalDateTime expiry, SecurityIdentifier underlying,
                                                    String market, BigDecimal strike,
                                                    OptionRight optionRight, OptionStyle optionStyle) {
        return generateOption(expiry, underlying, null, market, strike, optionRight, optionStyle);
    }

    public static SecurityIdentifier generateOption(LocalDateTime expiry, SecurityIdentifier underlying,
                                                    String targetOption, String market, BigDecimal strike,
                                                    OptionRight optionRight, OptionStyle optionStyle) {
        if (targetOption == null || targetOption.isEmpty()) {
            if (underlying.SecurityType == SecurityType.FUTURE) {
                targetOption = FuturesOptionsSymbolMappings.map(underlying.getSymbol());
            } else {
                // by default the target option matches the underlying symbol
                targetOption = underlying.getSymbol();
            }

            return generate(expiry, targetOption,
                    Symbol.getOptionTypeFromUnderlying(underlying.getSecurityType()), market,
                    strike, optionRight, optionStyle, underlying);
        }

        public static SecurityIdentifier generateFuture(LocalDateTime expiry, String symbol, String market) {
            return generate(expiry, symbol, SecurityType.Future, market);
        }

        public static SecurityIdentifier generateEquity(String symbol, String market, boolean mapSymbol) {
            LocalDate firstDate = DEFAULT_DATE;
            if (mapSymbol) {
                TickerDatePair firstTickerDate = getFirstTickerAndDate(symbol, market, SecurityType.Equity);
                firstDate = firstTickerDate.getDate();
                symbol = firstTickerDate.getTicker();
            }

            return generateEquity(firstDate, symbol, market);
        }

        public static SecurityIdentifier generateEquity(LocalDate date, String symbol, String market) {
            return generate(date, symbol, SecurityType.Equity, market);
        }

        public static SecurityIdentifier generateConstituentIdentifier(String symbol, SecurityType securityType,
                String market) {
            return generate(DEFAULT_DATE, symbol, securityType, market, false);
        }

        public static String generateBaseSymbol(Class<?> dataType, String symbol) {
            if (dataType == null) {
                return symbol;
            }

            return symbol.toUpperCase() + "." + dataType.getSimpleName();
        }

        public static SecurityIdentifier generateBase(Class<?> dataType, SecurityIdentifier underlying,
                String market) {
            LocalDate firstDate = DEFAULT_DATE;
            if (underlying.getSecurityType() == SecurityType.Equity) {
                TickerDatePair firstTickerDate = getFirstTickerAndDate(underlying.getSymbol(), market, SecurityType.Equity);
                firstDate = firstTickerDate.getDate();
            }

            return generate(firstDate, generateBaseSymbol(dataType, underlying.getSymbol()), SecurityType.Base,
                    market, false, underlying);
        }

        public static SecurityIdentifier generateForex(String symbol, String market) {
            return generate(DEFAULT_DATE, symbol, SecurityType.Forex, market);
        }

        public static SecurityIdentifier generateCrypto(String symbol, String market) {
            return generate(DEFAULT_DATE, symbol, SecurityType.Crypto, market);
        }

        public static SecurityIdentifier generateCryptoFuture(LocalDateTime expiry, String symbol, String market) {
            return generate(expiry, symbol, SecurityType.CryptoFuture, market);
        }

        public static SecurityIdentifier generateCfd(String symbol, String market) {
            return generate(DEFAULT_DATE, symbol, SecurityType.Cfd, market);
        }

        public static SecurityIdentifier generateIndex(String symbol, String market) {
            return generate(DEFAULT_DATE, symbol, SecurityType.Index, market);
        }

        private static SecurityIdentifier generate(LocalDateTime dateTime, String symbol, SecurityType securityType,
                String market) {
            LocalDate date = dateTime.toLocalDate();
            return generate(date, symbol, securityType, market);
        }

        private static SecurityIdentifier generate(LocalDate date, String symbol, SecurityType securityType,
                String market) {
            return generate(date, symbol, securityType, market, true);
        }

        private static SecurityIdentifier generate(LocalDate date, String symbol, SecurityType securityType,
                String market, boolean forceSymbolToUpper) {
            symbol = forceSymbolToUpper ? symbol.toUpperCase() : symbol;

            long days = date.toEpochDay() * DAYS_OFFSET;
            int marketCode = getMarketIdentifier(market);
            long marketOffset = marketCode * MARKET_OFFSET;

            // ...
            long otherData = days + marketOffset + securityType.getValue();
            long properties = otherData;

            SecurityIdentifier underlying = null;
            if (symbol != null && !symbol.isEmpty()) {
                int index = symbol.indexOf('|');
                if (index != -1) {
                    underlying = generate(symbol.substring(index + 1), market);
                    symbol = symbol.substring(0, index).trim();
                }
            }

            SecurityIdentifier securityIdentifier = new SecurityIdentifier(symbol, properties, underlying);

            if (securityType.isOption()) {
                BigDecimal normalizedStrike = normalizeStrike(strike);
                int strikeScale = normalizedStrike.scale();
                long scaledStrike = normalizedStrike.movePointRight(strikeScale).longValue();
                long strikeOffset = scaledStrike * STRIKE_OFFSET;
                long strikeScaleOffset = strikeScale * STRIKE_SCALE_OFFSET;
                long optionStyleOffset = optionStyle.getValue() * OPTION_STYLE_OFFSET;
                long putCallOffset = optionRight.getValue() * PUT_CALL_OFFSET;

                properties = strikeOffset + strikeScaleOffset + optionStyleOffset + putCallOffset + properties;
                securityIdentifier.strikePrice = normalizedStrike;
                securityIdentifier.optionStyle = optionStyle;
                securityIdentifier.optionRight = optionRight;
            }

            securityIdentifier.date = date;
            securityIdentifier.hashCode = Objects.hash(symbol, properties);
            CACHE.put(symbol + "|" + properties, securityIdentifier);
            return securityIdentifier;
        }

        private static SecurityIdentifier generate(String symbol, String market) {
            if (CACHE.containsKey(symbol)) {
                return CACHE.get(symbol);
            }

            String[] parts = symbol.split("\\|");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid symbol format");
            }

            String underlyingSymbol = parts[0].trim();
            long properties = Long.parseLong(parts[1].trim());

            SecurityIdentifier underlying = generate(underlyingSymbol, market);
            SecurityIdentifier securityIdentifier = new SecurityIdentifier(symbol, properties, underlying);
            CACHE.put(symbol, securityIdentifier);
            return securityIdentifier;
        }

        private static int getMarketIdentifier(String market) {
            if (!MARKET_MAP.containsValue(market)) {
                throw new IllegalArgumentException("Market not found");
            }

            return MARKET_MAP.inverse().get(market);
        }

        private static BigDecimal normalizeStrike(BigDecimal strike) {
            if (strike.equals(BigDecimal.ZERO)) {
                return BigDecimal.ZERO;
            }

            int scale = 0;
            while (strike.remainder(BigDecimal.TEN).equals(BigDecimal.ZERO)) {
                strike = strike.divide(BigDecimal.TEN);
                scale++;
            }

            if (strike.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) > 0) {
                throw new IllegalArgumentException("Invalid strike price");
            }

            return strike.setScale(scale);
        }

        private static TickerDatePair getFirstTickerAndDate(String ticker, String market, SecurityType securityType) {
            // Implement the logic to get the first ticker and date for the given parameters
            // Return the TickerDatePair object containing the ticker and date
        }

    }

    @Override
    public int compareTo(@NotNull SecurityIdentifier o) {
        return 0;
    }
}
