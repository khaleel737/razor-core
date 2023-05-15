package com.axes.razorcore.core;

import com.axes.razorcore.core.Global.OptionRight;
import com.axes.razorcore.core.Global.OptionStyle;
import com.axes.razorcore.core.Global.SecurityType;
import com.espertech.esper.common.client.util.DateTime;

import java.util.Objects;

public class Symbol implements Comparable<Symbol> {
    public static final Symbol Empty = new Symbol(SecurityIdentifier.EMPTY, "");

    private final String value;
    private final SecurityIdentifier id;
    private final Symbol underlying;

    public Symbol(SecurityIdentifier sid, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.id = sid;
        if (id.hasUnderlying()) {
            this.underlying = new Symbol(id.getUnderlying(), id.getUnderlying().getSymbol());
        } else {
            this.underlying = null;
        }
        this.value = value.toUpperCase();
    }

    public static Symbol create(String ticker, SecurityType securityType, String market, String alias, Class<?> baseDataType) {
        SecurityIdentifier sid;

        switch (securityType) {
            case BASE:
                sid = SecurityIdentifier.generateBase(baseDataType, ticker, market);
                break;
            case EQUITY:
                sid = SecurityIdentifier.generateEquity(ticker, market);
                break;
            case FOREX:
                sid = SecurityIdentifier.generateForex(ticker, market);
                break;
            case CFD:
                sid = SecurityIdentifier.generateCfd(ticker, market);
                break;
            case INDEX:
                sid = SecurityIdentifier.generateIndex(ticker, market);
                break;
            case OPTION:
                return createOption(ticker, market, OptionStyle.European, OptionRight.Call, 0, SecurityIdentifier.defaultDate);
            case FUTURE:
                sid = SecurityIdentifier.generateFuture(SecurityIdentifier.defaultDate, ticker, market);
                break;
            case CRYPTO:
                sid = SecurityIdentifier.generateCrypto(ticker, market);
                break;
            case CRYPTOFUTURE:
                sid = SecurityIdentifier.generateCryptoFuture(SecurityIdentifier.defaultDate, ticker, market);
                break;
            case INDEXOPTION:
                return createOption(create(ticker, SecurityType.Index, market), market, OptionStyle.European, OptionRight.Call, 0, SecurityIdentifier.defaultDate);
            case FUTUREOPTION:
                throw new UnsupportedOperationException("Insufficient information to create FutureOption symbol");
            case COMMODITY:
            default:
                throw new UnsupportedOperationException("Security type not implemented yet: " + securityType);
        }

        return new Symbol(sid, alias != null ? alias : ticker);
    }

    public static Symbol createOption(String underlying, String market, OptionStyle style, OptionRight right, double strike, DateTime expiry, String alias) {
        SecurityIdentifier underlyingSid = SecurityIdentifier.generateEquity(underlying, market);
        Symbol underlyingSymbol = new Symbol(underlyingSid, underlying);

        return createOption(underlyingSymbol, market, style, right, strike, expiry, alias);
    }

    public static Symbol createOption(Symbol underlyingSymbol, String market, OptionStyle style, OptionRight right, double strike, DateTime expiry, String alias) {
        return createOption(underlyingSymbol, null, market, style, right, strike, expiry, alias);
    }

    public static Symbol createOption(Symbol underlyingSymbol, String targetOption, String market, OptionStyle style, OptionRight right, double strike, DateTime expiry, String alias) {
        SecurityIdentifier sid = SecurityIdentifier.generateOption(expiry, underlyingSymbol.getId(), targetOption, market, strike, right, style);
        return new Symbol(sid, alias != null ? alias : getAlias(sid, underlyingSymbol), underlyingSymbol);
    }

    public static Symbol createBase(Class<?> baseType, Symbol underlying, String market) {
        DateTime firstDate = null;
        if (underlying.getSecurityType() == SecurityType.Equity || underlying.getSecurityType().isOption() ||
                underlying.getSecurityType() == SecurityType.Future || underlying.getSecurityType() == SecurityType.Base) {
            firstDate = underlying.getID().getDate();
        }
        SecurityIdentifier sid = SecurityIdentifier.generateBase(baseType, underlying.getID().getSymbol(), market, false, firstDate);
        return new Symbol(sid, underlying.getValue(), underlying);
    }

    public static Symbol createCanonicalOption(Symbol underlyingSymbol, String market, String alias) {
        return createCanonicalOption(underlyingSymbol, null, market, alias);
    }

    public static Symbol createCanonicalOption(Symbol underlyingSymbol, String targetOption, String market, String alias) {
        OptionType optionType = getOptionTypeFromUnderlying(underlyingSymbol);
        market = market != null ? market : underlyingSymbol.getID().getMarket();
        return createOption(underlyingSymbol, targetOption, market, optionType.getDefaultOptionStyle(), 0, SecurityIdentifier.defaultDate, alias);
    }

    public boolean isCanonical() {
        return (id.getSecurityType() == SecurityType.Future ||
                (id.getSecurityType().isOption() && hasUnderlying())) &&
                id.getDate() == SecurityIdentifier.defaultDate;
    }

    public Symbol getCanonical() {
        if (underlying != null) {
            return underlying.getCanonical();
        }
        if (!isCanonical()) {
            if (getSecurityType().isOption()) {
                return createCanonicalOption(getUnderlying(), getID().getMarket(), null);
            } else if (getSecurityType() == SecurityType.Future) {
                return create(getID().getSymbol(), SecurityType.Future, getID().getMarket());
            } else {
                throw new UnsupportedOperationException("Canonical representation not defined");
            }
        }
        return this;
    }

    public boolean hasUnderlyingSymbol(Symbol symbol) {
        Symbol current = this;
        while (current.hasUnderlying()) {
            if (current.getUnderlying().equals(symbol)) {
                return true;
            }
            current = current.getUnderlying();
        }
        return false;
    }

    public String getValue() {
        return value;
    }

    public SecurityIdentifier getID() {
        return id;
    }

    public boolean hasUnderlying() {
        return underlying != null;
    }

    public Symbol getUnderlying() {
        return underlying;
    }

    public SecurityType getSecurityType() {
        return id.getSecurityType();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Symbol symbol = (Symbol) obj;
        return id.equals(symbol.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Symbol o) {
        return value.compareToIgnoreCase(o.value);
    }
}


