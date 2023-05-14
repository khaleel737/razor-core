package com.axes.razorcore.data;

import lombok.ToString;

import java.util.Arrays;

@ToString
public class L2MarketData {

    public static final int L2_SIZE = 32;
    public int askSize;
    public int bidSize;

    public long[] askPrices;
    public long[] askVolumes;
    public long[] askOrders;
    public long[] bidPrices;
    public long[] bidVolumes;
    public long[] bidOrders;

    public long timeStamp;
    public long referenceSequence;

    public L2MarketData(long[] askPrices, long[] askVolumes, long[] askOrders, long[] bidPrices, long[] bidVolumes, long[] bidOrders) {
        this.askPrices = askPrices;
        this.askVolumes = askVolumes;
        this.askOrders = askOrders;
        this.bidPrices = bidPrices;
        this.bidVolumes = bidVolumes;
        this.bidOrders = bidOrders;

        this.askSize = askPrices != null ? askPrices.length : 0;
        this.bidSize = bidPrices != null ? bidPrices.length : 0;
    }

    public L2MarketData(int askSize, int bidSize) {
        this.askPrices = new long[askSize];
        this.askVolumes = new long[askSize];
        this.askOrders = new long[askSize];
        this.bidPrices = new long[bidSize];
        this.bidVolumes = new long[bidSize];
        this.bidOrders = new long[bidSize];
    }


    public long[] getAskPricesCopy() {
        return Arrays.copyOf(askPrices, askSize);
    }

    public long[] getAskVolumesCopy() {
        return Arrays.copyOf(askPrices, askSize);
    }

    public long[] getAskOrdersCopy() {
        return Arrays.copyOf(askOrders, askSize);
    }

    public long[] getBidPricesCopy() {
        return Arrays.copyOf(bidPrices, bidSize);
    }

    public long[] getBidVolumesCopy() {
        return Arrays.copyOf(bidVolumes, bidSize);
    }

    public long[] getBidOrdersCopy() {
        return Arrays.copyOf(bidOrders, bidSize);
    }

    public long totalOrderBookVolumesAsk() {
        long totalVolumes = 0L;
        for(int i = 0; i < bidSize; i++) {
            totalVolumes += bidVolumes[i];
        }
        return totalVolumes;
    }

    public long totalOrderBookVolumeBid() {
        long totalVolume = 0L;
        for (int i = 0; i < bidSize; i++) {
            totalVolume += bidVolumes[i];
        }
        return totalVolume;
    }

    public L2MarketData copy() {
        return new L2MarketData(getAskPricesCopy(), getAskVolumesCopy(), getAskOrdersCopy(), getBidPricesCopy(), getBidVolumesCopy(), getBidOrdersCopy());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof L2MarketData marketData)) return false;
        if(askSize != marketData.askSize || bidSize != marketData.bidSize) return false;

        for(int i = 0; i < askSize; i++) {
        if(askPrices[i] != marketData.askPrices[i] || askVolumes[i] != marketData.askVolumes[i] || askOrders[i] != marketData.askOrders[i]) {
            return false;
        }
        }

        for(int i = 0; i < bidSize; i++) {
            if(bidPrices[i] != marketData.bidPrices[i] || bidVolumes[i] != marketData.bidVolumes[i] || bidOrders[i] != marketData.bidOrders[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
