package com.axes.razorcore.data.consolidators;

import com.axes.razorcore.core.Global.Resolution;
import com.axes.razorcore.core.Global.TickType;
import com.axes.razorcore.core.Symbol;
import com.quantconnect.data.consolidators.IDataConsolidator;
import com.quantconnect.securities.DataMappingMode;
import com.quantconnect.securities.Symbol;
import com.quantconnect.util.ConcurrentSet;
import org.threeten.extra.Interval;

import java.util.Objects;
import java.util.Set;

public class SubscriptionDataConfig implements Cloneable {
    private final boolean mappedConfig;
    private final Symbol sid;

    private Object type;
    private Symbol symbol;
    private TickType tickType;
    private Resolution resolution;
    private Interval interval;
    private boolean fillDataForward;
    private boolean extendedMarketHours;
    private boolean isInternalFeed;
    private boolean isCustomData;
    private boolean isFilteredSubscription;
    private boolean isFilteredSubscription;
    private DataNormalizationMode dataNormalizationMode;
    private DataMappingMode dataMappingMode;
    private int contractDepthOffset;
    private double priceScaleFactor;
    private Set<IDataConsolidator> consolidators;

    public SubscriptionDataConfig(Type type,
                                  Symbol symbol,
                                  Resolution resolution,
                                  boolean fillDataForward,
                                  boolean extendedMarketHours,
                                  boolean isInternalFeed,
                                  boolean isCustomData,
                                  boolean isFilteredSubscription,
                                  DataNormalizationMode dataNormalizationMode,
                                  DataMappingMode dataMappingMode,
                                  int contractDepthOffset,
                                  boolean mappedConfig) {
        this.type = type;
        this.symbol = symbol;
        this.tickType = TickType.Trade;
        this.resolution = resolution;
        this.fillDataForward = fillDataForward;
        this.extendedMarketHours = extendedMarketHours;
        this.isInternalFeed = isInternalFeed;
        this.isCustomData = isCustomData;
        this.isFilteredSubscription = isFilteredSubscription;
        this.dataNormalizationMode = dataNormalizationMode;
        this.dataMappingMode = dataMappingMode;
        this.contractDepthOffset = contractDepthOffset;
        this.mappedConfig = mappedConfig;
        this.priceScaleFactor = 1;
        this.consolidators = new ConcurrentSet<>();
        this.interval = resolution.toInterval();
    }

    public SubscriptionDataConfig(SubscriptionDataConfig config,
                                  Type type,
                                  Symbol symbol,
                                  Resolution resolution,
                                  boolean fillDataForward,
                                  boolean extendedMarketHours,
                                  boolean isInternalFeed,
                                  boolean isCustomData,
                                  TickType tickType,
                                  boolean isFilteredSubscription,
                                  DataNormalizationMode dataNormalizationMode,
                                  DataMappingMode dataMappingMode,
                                  int contractDepthOffset,
                                  boolean mappedConfig) {
        this.type = type != null ? type : config.type;
        this.symbol = symbol != null ? symbol : config.symbol;
        this.tickType = tickType != null ? tickType : config.tickType;
        this.resolution = resolution != null ? resolution : config.resolution;
        this.fillDataForward = fillDataForward != null ? fillDataForward : config.fillDataForward;
        this.extendedMarketHours = extendedMarketHours != null ? extendedMarketHours : config.extendedMarketHours;
        this.isInternalFeed = isInternalFeed != null ? isInternalFeed : config.isInternalFeed;
        this.isCustomData = isCustomData != null ? isCustomData : config.isCustomData;
        this.isFilteredSubscription = isFilteredSubscription != null ? isFilteredSubscription : config.isFilteredSubscription;
        this.dataNormalizationMode = dataNormalizationMode != null ? dataNormalizationMode : config.dataNormalizationMode;
        this.dataMappingMode = dataMappingMode != null ? dataMappingMode : config.dataMappingMode;
        contractDepthOffset != null ? contractDepthOffset : config.contractDepthOffset;
        this.mappedConfig = mappedConfig != null ? mappedConfig : config.mappedConfig;
        this.priceScaleFactor = config.priceScaleFactor;
        this.consolidators = new ConcurrentSet<>(config.consolidators);
        this.interval = config.interval;
    }

    // Getters and Setters

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public TickType getTickType() {
        return tickType;
    }

    public void setTickType(TickType tickType) {
        this.tickType = tickType;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public boolean isFillDataForward() {
        return fillDataForward;
    }

    public void setFillDataForward(boolean fillDataForward) {
        this.fillDataForward = fillDataForward;
    }

    public boolean isExtendedMarketHours() {
        return extendedMarketHours;
    }

    public void setExtendedMarketHours(boolean extendedMarketHours) {
        this.extendedMarketHours = extendedMarketHours;
    }

    public boolean isInternalFeed() {
        return isInternalFeed;
    }

    public void setInternalFeed(boolean internalFeed) {
        isInternalFeed = internalFeed;
    }

    public boolean isCustomData() {
        return isCustomData;
    }

    public void setCustomData(boolean customData) {
        isCustomData = customData;
    }

    public boolean isFilteredSubscription() {
        return isFilteredSubscription;
    }

    public void setFilteredSubscription(boolean filteredSubscription) {
        isFilteredSubscription = filteredSubscription;
    }

    public DataNormalizationMode getDataNormalizationMode() {
        return dataNormalizationMode;
    }

    public void setDataNormalizationMode(DataNormalizationMode dataNormalizationMode) {
        this.dataNormalizationMode = dataNormalizationMode;
    }

    public DataMappingMode getDataMappingMode() {
        return dataMappingMode;
    }

    public void setDataMappingMode(DataMappingMode dataMappingMode) {
        this.dataMappingMode = dataMappingMode;
    }

    public int getContractDepthOffset() {
        return contractDepthOffset;
    }

    public void setContractDepthOffset(int contractDepthOffset) {
        this.contractDepthOffset = contractDepthOffset;
    }

    public double getPriceScaleFactor() {
        return priceScaleFactor;
    }

    public void setPriceScaleFactor(double priceScaleFactor) {
        this.priceScaleFactor = priceScaleFactor;
    }

    public Set<IDataConsolidator> getConsolidators() {
        return consolidators;
    }

    public void setConsolidators(Set<IDataConsolidator> consolidators) {
        this.consolidators = consolidators;
    }

    public boolean isMappedConfig() {
        return mappedConfig;
    }

    // Equals and HashCode methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionDataConfig that = (SubscriptionDataConfig) o;
        return contractDepthOffset == that.contractDepthOffset &&
                mappedConfig == that.mappedConfig &&
                Objects.equals(type, that.type) &&
                Objects.equals(symbol, that.symbol)
        Objects.equals(tickType, that.tickType) &&
                Objects.equals(resolution, that.resolution) &&
                Objects.equals(interval, that.interval) &&
                fillDataForward == that.fillDataForward &&
                extendedMarketHours == that.extendedMarketHours &&
                isInternalFeed == that.isInternalFeed &&
                isCustomData == that.isCustomData &&
                isFilteredSubscription == that.isFilteredSubscription &&
                dataNormalizationMode == that.dataNormalizationMode &&
                dataMappingMode == that.dataMappingMode &&
                Double.compare(that.priceScaleFactor, priceScaleFactor) == 0 &&
                Objects.equals(consolidators, that.consolidators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, symbol, tickType, resolution, interval, fillDataForward, extendedMarketHours, isInternalFeed, isCustomData, isFilteredSubscription, dataNormalizationMode, dataMappingMode, contractDepthOffset, priceScaleFactor, consolidators);
    }

    @Override
    public String toString() {
        return "SubscriptionDataConfig{" +
                "type=" + type +
                ", symbol=" + symbol +
                ", tickType=" + tickType +
                ", resolution=" + resolution +
                ", interval=" + interval +
                ", fillDataForward=" + fillDataForward +
                ", extendedMarketHours=" + extendedMarketHours +
                ", isInternalFeed=" + isInternalFeed +
                ", isCustomData=" + isCustomData +
                ", isFilteredSubscription=" + isFilteredSubscription +
                ", dataNormalizationMode=" + dataNormalizationMode +
                ", dataMappingMode=" + dataMappingMode +
                ", contractDepthOffset=" + contractDepthOffset +
                ", priceScaleFactor=" + priceScaleFactor +
                ", consolidators=" + consolidators +
                '}';
    }
}

