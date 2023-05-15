package com.axes.razorcore.core.Global;

import com.axes.razorcore.core.Exchange;
import com.axes.razorcore.core.Market;

public class Exchanges {
    public static String getPrimaryExchangeCodeGetPrimaryExchange(String exchange,
                                                                  SecurityType securityType,
                                                                  String market) {
        return getPrimaryExchange(exchange, securityType, market).getCode();
    }

    public static Exchange getPrimaryExchange(String exchange,
                                              SecurityType securityType,
                                              String market) {
        Exchange primaryExchange = Exchange.UNKNOWN;
        if (exchange == null || exchange.isEmpty()) {
            return primaryExchange;
        }

        if (securityType == SecurityType.EQUITY) {
            switch (exchange.toLowerCase()) {
                case "t", "q", "nasdaq", "nasdaq_omx" -> {
                    return Exchange.NASDAQ;
                }
                case "z", "bats", "bats z", "bats_z" -> {
                    return Exchange.BATS;
                }
                case "p", "arca" -> {
                    return Exchange.ARCA;
                }
                case "n", "nyse" -> {
                    return Exchange.NYSE;
                }
                case "c", "nsx", "nse" -> {
                    if (market.equals(Market.USA)) {
                        return Exchange.NSX;
                    } else if (market.equals(Market.India)) {
                        return Exchange.NSE;
                    }
                    return Exchange.UNKNOWN;
                }
                case "d", "finra" -> {
                    return Exchange.FINRA;
                }
                case "i", "ise" -> {
                    return Exchange.ISE;
                }
                case "m", "cse" -> {
                    return Exchange.CSE;
                }
                case "w", "cboe" -> {
                    return Exchange.CBOE;
                }
                case "a", "amex" -> {
                    return Exchange.AMEX;
                }
                case "siac" -> {
                    return Exchange.SIAC;
                }
                case "j", "edga" -> {
                    return Exchange.EDGA;
                }
                case "k", "edgx" -> {
                    return Exchange.EDGX;
                }
                case "b", "nasdaq bx", "nasdaq_bx" -> {
                    return Exchange.NASDAQ_BX;
                }
                case "x", "nasdaq psx", "nasdaq_psx" -> {
                    return Exchange.NASDAQ_PSX;
                }
                case "y", "bats y", "bats_y" -> {
                    return Exchange.BATS_Y;
                }
                case "bb", "boston" -> {
                    return Exchange.BOSTON;
                }
                case "bse" -> {
                    return Exchange.BSE;
                }
                case "iex" -> {
                    return Exchange.IEX;
                }
                case "smart" -> {
                    return Exchange.SMART;
                }
                case "otcx" -> {
                    return Exchange.OTCX;
                }
                case "mp", "miax pearl", "miax_pearl" -> {
                    return Exchange.MIAX_PEARL;
                }
                case "l", "ltse" -> {
                    return Exchange.LTSE;
                }
                case "mm", "memx" -> {
                    return Exchange.MEMX;
                }
            }
        } else if (securityType == SecurityType.OPTION) {
            return switch (exchange.toLowerCase()) {
                case "a", "amex" -> Exchange.AMEX_OPTIONS;
                case "m", "miax" -> Exchange.MIAX;
                case "me", "miax emerald", "miax_emerald" -> Exchange.MIAX_EMERALD;
                case "mp", "miax pearl", "miax_pearl" -> Exchange.MIAX_PEARL;
                case "i", "ise" -> Exchange.ISE;
                case "h", "ise gemini", "ise_gemini" -> Exchange.ISE_GEMINI;
                case "j", "ise mercury", "ise_mercury" -> Exchange.ISE_MERCURY;
                case "o", "opra" -> Exchange.OPRA;
                case "w", "c2" -> Exchange.C2;
                default -> Exchange.UNKNOWN;
            };
        } else if (securityType == SecurityType.FUTURE || securityType == SecurityType.FUTUREOPTION) {
            return switch (exchange.toLowerCase()) {
                case "cme" -> Exchange.CME;
                case "cbot" -> Exchange.CBOT;
                case "nymex" -> Exchange.NYMEX;
                case "ice" -> Exchange.ICE;
                case "cfe" -> Exchange.CFE;
                case "comex" -> Exchange.COMEX;
                default -> Exchange.UNKNOWN;
            };
        }
        return Exchange.UNKNOWN;
    }
}