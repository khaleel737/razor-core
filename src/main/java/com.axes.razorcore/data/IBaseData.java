package com.axes.razorcore.data;

import com.axes.razorcore.core.Global.MarketDataType;
import com.axes.razorcore.core.Symbol;
import com.axes.razorcore.data.consolidators.SubscriptionDataConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.ZonedDateTime;

public interface IBaseData {
    /**
     * Market Data Type of this data - does it come in individual price packets or is it grouped into OHLC.
     */
    MarketDataType getDataType();

    /**
     * Time keeper of data -- all data is time series based.
     */
    ZonedDateTime getTime();

    /**
     * End time of data.
     */
    ZonedDateTime getEndTime();

    /**
     * Symbol for underlying Security.
     */
    Symbol getSymbol();

    /**
     * All time series data is a time-value pair.
     */
    double getValue();

    /**
     * Alias of Value.
     */
    double getPrice();

    /**
     * Reader converts each line of the data source into BaseData objects. Each data type creates its own factory method, and returns a new instance of the object
     * each time it is called. The returned object is assumed to be time stamped in the config.ExchangeTimeZone.
     */
    IBaseData reader(SubscriptionDataConfig config, String line, ZonedDateTime date, boolean isLiveMode);

    /**
     * Reader converts each line of the data source into BaseData objects. Each data type creates its own factory method, and returns a new instance of the object
     * each time it is called. The returned object is assumed to be time stamped in the config.ExchangeTimeZone.
     */
    IBaseData reader(SubscriptionDataConfig config, BufferedReader reader, ZonedDateTime date, boolean isLiveMode) throws IOException;

    /**
     * Return the URL string source of the file. This will be converted to a stream.
     */
    String getSource(SubscriptionDataConfig config, ZonedDateTime date, DataFeedEndpoint datafeed);

    /**
     * Indicates if there is support for mapping.
     */
    boolean requiresMapping();

    /**
     * Return a new instance clone of this object.
     */
    IBaseData clone();
}

