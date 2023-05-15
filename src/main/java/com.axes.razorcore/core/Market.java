package com.axes.razorcore.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Market {
    /// <summary>
    /// USA Market
    /// </summary>
    public static String USA = "usa";

    /// <summary>
    /// Oanda Market
    /// </summary>
    public static String Oanda = "oanda";

    /// <summary>
    /// FXCM Market Hours
    /// </summary>
    public static String FXCM = "fxcm";

    /// <summary>
    /// Dukascopy Market
    /// </summary>
    public static String Dukascopy = "dukascopy";

    /// <summary>
    /// Bitfinex market
    /// </summary>
    public static String Bitfinex = "bitfinex";

    // Futures exchanges

    /// <summary>
    /// CME Globex
    /// </summary>
    public static String Globex = "cmeglobex";

    /// <summary>
    /// NYMEX
    /// </summary>
    public static String NYMEX = "nymex";

    /// <summary>
    /// CBOT
    /// </summary>
    public static String CBOT = "cbot";

    /// <summary>
    /// ICE
    /// </summary>
    public static String ICE = "ice";

    /// <summary>
    /// CBOE
    /// </summary>
    public static String CBOE = "cboe";

    /// <summary>
    /// CFE
    /// </summary>
    public static String CFE = "cfe";

    /// <summary>
    /// NSE - National Stock Exchange
    /// </summary>
    public static String India = "india";

    /// <summary>
    /// Comex
    /// </summary>
    public static String COMEX = "comex";

    /// <summary>
    /// CME
    /// </summary>
    public static String CME = "cme";

    /// <summary>
    /// Singapore Exchange
    /// </summary>
    public static String SGX = "sgx";

    /// <summary>
    /// Hong Kong Exchange
    /// </summary>
    public static String HKFE = "hkfe";

    /// <summary>
    /// London International Financial Futures and Options Exchange
    /// </summary>
    public static String NYSELIFFE = "nyseliffe";

    /// <summary>
    /// GDAX
    /// </summary>
    public static String GDAX = "gdax";

    /// <summary>
    /// Kraken
    /// </summary>
    public static String Kraken = "kraken";

    /// <summary>
    /// Bitstamp
    /// </summary>
    public static String Bitstamp = "bitstamp";

    /// <summary>
    /// OkCoin
    /// </summary>
    public static String OkCoin = "okcoin";

    /// <summary>
    /// Bithumb
    /// </summary>
    public static String Bithumb = "bithumb";

    /// <summary>
    /// Binance
    /// </summary>
    public static String Binance = "binance";

    /// <summary>
    /// Poloniex
    /// </summary>
    public static String Poloniex = "poloniex";

    /// <summary>
    /// Coinone
    /// </summary>
    public static String Coinone = "coinone";

    /// <summary>
    /// HitBTC
    /// </summary>
    public static String HitBTC = "hitbtc";

    /// <summary>
    /// Bittrex
    /// </summary>
    public static String Bittrex = "bittrex";

    /// <summary>
    /// FTX
    /// </summary>
    public static String FTX = "ftx";

    /// <summary>
    /// FTX.US
    /// </summary>
    public static String FTXUS = "ftxus";

    /// <summary>
    /// Binance.US
    /// </summary>
    public static String BinanceUS = "binanceus";

        // the upper bound (non-inclusive) for market identifiers
        private static int MaxMarketIdentifier = 1000;

        private static LinkedHashMap<String, Integer> Markets = new LinkedHashMap<>();
        private static LinkedHashMap<Integer, String> ReverseMarkets = new LinkedHashMap<>();
        private static final LinkedHashMap<String, Integer> HardcodedMarkets = new LinkedHashMap<>();

    static {
        Map<String, Integer> marketMap = new LinkedHashMap<>();
        marketMap.put("empty", 0);
        marketMap.put(USA, 1);
        marketMap.put(FXCM, 2);
        marketMap.put(Oanda, 3);
        marketMap.put(Dukascopy, 4);
        marketMap.put(Bitfinex, 5);
        marketMap.put(Globex, 6);
        marketMap.put(NYMEX, 7);
        marketMap.put(CBOT, 8);
        marketMap.put(ICE, 9);
        marketMap.put(CBOE, 10);
        marketMap.put(India, 11);
        marketMap.put(GDAX, 12);
        marketMap.put(Kraken, 13);
        marketMap.put(Bittrex, 14);
        marketMap.put(Bithumb, 15);
        marketMap.put(Binance, 16);
        marketMap.put(Poloniex, 17);
        marketMap.put(Coinone, 18);
        marketMap.put(HitBTC, 19);
        marketMap.put(OkCoin, 20);
        marketMap.put(Bitstamp, 21);
        marketMap.put(COMEX, 22);
        marketMap.put(CME, 23);
        marketMap.put(SGX, 24);
        marketMap.put(HKFE, 25);
        marketMap.put(NYSELIFFE, 26);
        marketMap.put(CFE, 33);
        marketMap.put(FTX, 34);
        marketMap.put(FTXUS, 35);
        marketMap.put(BinanceUS, 36);
        HardcodedMarkets.putAll(marketMap);
    }

    public Market() {
        // initialize our maps
        for (Map.Entry<String, Integer> entry : HardcodedMarkets.entrySet()) {
            String marketName = entry.getKey();
            Integer marketValue = entry.getValue();
            Markets.put(marketName, marketValue);
            ReverseMarkets.put(marketValue, marketName);
        }
    }

    /// <summary>
        /// Adds the specified market to the map of available markets with the specified identifier.
        /// </summary>
        /// <param name="market">The market String to add</param>
        /// <param name="identifier">The identifier for the market, this value must be positive and less than 1000</param>
    public static void Add(String market, int identifier) {
        if (identifier >= MaxMarketIdentifier) {
            throw new IllegalArgumentException("Invalid market identifier");
        }

        market = market.toLowerCase();

        Integer marketIdentifier = Markets.get(market);
        if (marketIdentifier != null && identifier != marketIdentifier) {
            throw new IllegalArgumentException("Tried to add an existing market with a different identifier");
        }

        String existingMarket = ReverseMarkets.get(identifier);
        if (existingMarket != null) {
            throw new IllegalArgumentException("Tried to add an existing market identifier");
        }

        // Update our maps
        Map<String, Integer> newMarketDictionary = new HashMap<>(Markets);
        newMarketDictionary.put(market, identifier);

        Map<Integer, String> newReverseMarketDictionary = new HashMap<>(ReverseMarkets);
        newReverseMarketDictionary.put(identifier, market);

        Markets.clear();
        ReverseMarkets.clear();
        Markets.putAll(newMarketDictionary);
        ReverseMarkets.putAll(newReverseMarketDictionary);
    }

    public static Integer Encode(String market) {
        return Markets.getOrDefault(market, null);
    }

    public static String Decode(int code) {
        return ReverseMarkets.getOrDefault(code, null);
    }

    public static List<String> SupportedMarkets() {
        return Markets.keySet().stream().collect(Collectors.toList());
    }
}
