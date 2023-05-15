package com.axes.razorcore.core.Global;

import com.axes.razorcore.core.Symbol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Security;


public class Holding {
    public Symbol symbol = Symbol.Empty;
    @JsonIgnore
    public SecurityType Type = symbol.getSecurityType();
    public String CurrencySymbol;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal averagePrice;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal Quantity;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal MarketPrice;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal ConversionRate;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal MarketValue;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal UnrealizedPnL;

    public Holding() {
        CurrencySymbol = "$";
    }

    public Holding(Security security) {
        this();
        Holding holding = security.Holdings;
        Symbol = holding.Symbol;
        Quantity = holding.Quantity;
        MarketValue = holding.HoldingsValue;
        CurrencySymbol = Currencies.GetCurrencySymbol(security.QuoteCurrency.Symbol);
        ConversionRate = security.QuoteCurrency.ConversionRate;
        int rounding = security.SymbolProperties.MinimumPriceVariation.GetDecimalPlaces();
        AveragePrice = holding.AveragePrice.setScale(rounding, RoundingMode.HALF_UP);
        MarketPrice = holding.Price.setScale(rounding, RoundingMode.HALF_UP);
        UnrealizedPnL = holding.UnrealizedProfit.setScale(2, RoundingMode.HALF_UP);
    }

    public Holding clone() {
        Holding clone = new Holding();
        clone.AveragePrice = AveragePrice;
        clone.Symbol = Symbol;
        clone.Quantity = Quantity;
        clone.MarketPrice = MarketPrice;
        clone.MarketValue = MarketValue;
        clone.UnrealizedPnL = UnrealizedPnL;
        clone.ConversionRate = ConversionRate;
        clone.CurrencySymbol = CurrencySymbol;
        return clone;
    }

    @Override
    public String toString() {
        return "Holding{" +
                "Symbol=" + Symbol +
                ", Type=" + Type +
                ", CurrencySymbol='" + CurrencySymbol + '\'' +
                ", AveragePrice=" + AveragePrice +
                ", Quantity=" + Quantity +
                ", MarketPrice=" + MarketPrice +
                ", ConversionRate=" + ConversionRate +
                ", MarketValue=" + MarketValue +
                ", UnrealizedPnL=" + UnrealizedPnL +
                '}';
    }
}
