package com.axes.razorcore.core;

import com.axes.razorcore.core.Global.SecurityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Exchange {
    public static final Exchange UNKNOWN = new Exchange("", "", "UNKNOWN", "");
    public static final Exchange MEMX = new Exchange("MEMX", "MM", "The Long-Term Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange LTSE = new Exchange("LTSE", "L", "The Long-Term Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange NASDAQ = new Exchange("NASDAQ", "Q", "National Association of Securities Dealers Automated Quotation", Market.USA, SecurityType.EQUITY);
    public static final Exchange BATS = new Exchange("BATS", "Z", "Bats Global Markets, Better Alternative Trading System", Market.USA, SecurityType.EQUITY);
    public static final Exchange ARCA = new Exchange("ARCA", "P", "New York Stock Archipelago Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange NYSE = new Exchange("NYSE", "N", "New York Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange SMART = new Exchange("SMART", "SMART", "SMART Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange OTCX = new Exchange("OTCX", "OTCX", "Over the Counter Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange IEX = new Exchange("IEX", "IEX", "Investors Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange NSX = new Exchange("NSE", "C", "National Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange FINRA = new Exchange("FINRA", "D", "The Financial Industry Regulatory Authority", Market.USA, SecurityType.EQUITY);
    public static final Exchange ISE = new Exchange("ISE", "I", "Nasdaq International Securities Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange CSE = new Exchange("CSE", "M", "Chicago Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange CBOE = new Exchange("CBOE", "W", "The Chicago Board OPTIONs Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange NASDAQ_BX = new Exchange("NASDAQ_BX", "B", "National Association of Securities Dealers Automated Quotation BX", Market.USA, SecurityType.EQUITY);
    public static final Exchange SIAC = new Exchange("SIAC", "SIAC", "The Securities Industry Automation Corporation", Market.USA, SecurityType.EQUITY);
    public static final Exchange EDGA = new Exchange("EDGA", "J", "CBOE EDGA U.S. equities Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange EDGX = new Exchange("EDGX", "K", "CBOE EDGX U.S. equities Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange NASDAQ_PSX = new Exchange("NASDAQ_PSX", "X", "National Association of Securities Dealers Automated Quotation PSX", Market.USA, SecurityType.EQUITY);
    public static final Exchange BATS_Y = new Exchange("BATS_Y", "Y", "Bats Global Markets, Better Alternative Trading System", Market.USA, SecurityType.EQUITY);
    public static final Exchange BOSTON = new Exchange("BOSTON", "BB", "The Boston Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange AMEX = new Exchange("AMEX", "A", "The American Stock Exchange", Market.USA, SecurityType.EQUITY);
    public static final Exchange BSE = new Exchange("BSE", "BSE", "Bombay Stock Exchange", Market.India, SecurityType.EQUITY);
    public static final Exchange NSE = new Exchange("NSE", "NSE", "National Stock Exchange of India", Market.India, SecurityType.EQUITY);
    public static final Exchange AMEX_OPTIONS = new Exchange("AMEX", "A", "The American OPTIONs Exchange", Market.USA, SecurityType.OPTION);
    public static final Exchange OPRA = new Exchange("OPRA", "O", "The OPTIONs Price Reporting Authority", Market.USA, SecurityType.OPTION);
    public static final Exchange C2 = new Exchange("C2", "W", "CBOE OPTIONs Exchange", Market.USA, SecurityType.OPTION);
    public static final Exchange MIAX = new Exchange("MIAX", "M", "Miami International Securities OPTIONs Exchange", Market.USA, SecurityType.OPTION);
    public static final Exchange MIAX_PEARL = new Exchange("MIAX_PEARL", "MP", "MIAX PEARL", Market.USA, SecurityType.OPTION, SecurityType.EQUITY);
    public static final Exchange MIAX_EMERALD = new Exchange("MIAX_EMERALD", "ME", "MIAX EMERALD", Market.USA, SecurityType.OPTION);
    public static final Exchange ISE_GEMINI = new Exchange("ISE_GEMINI", "H", "International Securities OPTIONs Exchange GEMINI", Market.USA, SecurityType.OPTION);
    public static final Exchange ISE_MERCURY = new Exchange("ISE_MERCURY", "J", "International Securities OPTIONs Exchange MERCURY", Market.USA, SecurityType.OPTION);
    public static final Exchange CME = new Exchange("CME", "CME", "Futures and OPTIONs Chicago Mercantile Exchange", Market.CME, SecurityType.FUTURE, SecurityType.FUTUREOPTION);
    public static final Exchange CBOT = new Exchange("CBOT", "CBOT", "Chicago Board of Trade Commodity Exchange", Market.CBOT, SecurityType.FUTURE, SecurityType.FUTUREOPTION);
    public static final Exchange CFE = new Exchange("CFE", "CFE", "CFE Futures Exchange", Market.CFE, SecurityType.FUTURE);
    public static final Exchange COMEX = new Exchange("COMEX", "COMEX", "COMEX Futures Exchange", Market.COMEX, SecurityType.FUTURE);
    public static final Exchange ICE = new Exchange("ICE", "ICE", "The Intercontinental Exchange", Market.ICE, SecurityType.FUTURE);
    public static final Exchange NYMEX = new Exchange("NYMEX", "NYMEX", "New York Mercantile Exchange", Market.NYMEX, SecurityType.FUTURE, SecurityType.FUTUREOPTION);
    public static final Exchange NYSELIFFE = new Exchange("NYSELIFFE", "NYSELIFFE", "London International Financial Futures and OPTIONs Exchange", Market.NYSELIFFE, SecurityType.FUTURE, SecurityType.FUTUREOPTION);

    private String description;
    private String code;
    private String name;
    private String market;
    private List<SecurityType> securityTypes = new ArrayList<>();

    public Exchange() {
    }

    private Exchange(String name, String code, String description, String market, SecurityType... securityTypes) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.market = market;
        if (securityTypes != null) {
            Collections.addAll(this.securityTypes, securityTypes);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getMarket() {
        return market;
    }

    public List<SecurityType> getSecurityTypes() {
        return Collections.unmodifiableList(securityTypes);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Exchange exchange = (Exchange) obj;
        return code.equals(exchange.code)
                && market.equals(exchange.market)
                && securityTypes.equals(exchange.securityTypes);
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + market.hashCode();
        result = 31 * result + securityTypes.hashCode();
        return result;
    }
}


