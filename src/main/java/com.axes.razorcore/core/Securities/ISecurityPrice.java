package com.axes.razorcore.core.Securities;

import com.axes.razorcore.core.Symbol;

import java.math.BigDecimal;
import java.util.List;

public interface ISecurityPrice {
    /**
     * Get the current value of the security.
     */
    BigDecimal getPrice();

    /**
     * If this uses trade bar data, return the most recent close.
     */
    BigDecimal getClose();

    /**
     * Access to the volume of the equity today.
     */
    BigDecimal getVolume();

    /**
     * Gets the most recent bid price if available.
     */
    BigDecimal getBidPrice();

    /**
     * Gets the most recent bid size if available.
     */
    BigDecimal getBidSize();

    /**
     * Gets the most recent ask price if available.
     */
    BigDecimal getAskPrice();

    /**
     * Gets the most recent ask size if available.
     */
    BigDecimal getAskSize();

    /**
     * Access to the open interest of the security today.
     */
    long getOpenInterest();

    /**
     * Gets the symbol for the asset.
     */
    Symbol getSymbol();

    /**
     * Update any security properties based on the latest market data and time.
     *
     * @param data New data packet
     */
    void setMarketPrice(BaseData data);

    /**
     * Updates all of the security properties, such as price/OHLCV/bid/ask based
     * on the data provided. Data is also stored into the security's data cache.
     *
     * @param data                     The security update data
     * @param dataType                 The data type
     * @param containsFillForwardData  Flag indicating whether the data contains any fill forward bar or not
     */
    void update(List<BaseData> data, Class<? extends BaseData> dataType, Boolean containsFillForwardData);

    /**
     * Get the last price update set to the security.
     *
     * @return BaseData object for this security
     */
    BaseData getLastData();
}
