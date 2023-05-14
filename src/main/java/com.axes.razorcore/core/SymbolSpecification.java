package com.axes.razorcore.core;

import com.axes.razorcore.utils.StateHash;
import lombok.*;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.Objects;
import java.util.Optional;

@Builder
@AllArgsConstructor
@Getter
@ToString
public class SymbolSpecification implements WriteBytesMarshallable, StateHash {

    public final int symbolId;

    @NonNull
    public final SymbolType type;

    // currency pair specification
    public final int baseCurrency;  // base currency
    public final int quoteCurrency; // quote/counter currency (OR futures contract currency)
    public final long baseScaleK;   // base currency amount multiplier (lot size in base currency units)
    public final long quoteScaleK;  // quote currency amount multiplier (step size in quote currency units)

    // fees per lot in quote? currency units
    public final long takerFee; // TODO check invariant: taker fee is not less than maker fee
    public final long makerFee;

    // margin settings (for type=FUTURES_CONTRACT only)
    public final long marginBuy;   // buy margin (quote currency)
    public final long marginSell;  // sell margin (quote currency)

    // Additional fields for different symbol types
    public final Optional<Long> highLimit;   // high price limit (for FUTURES and OPTIONS)
    public final Optional<Long> lowLimit;    // low price limit (for FUTURES and OPTIONS)
    public final Optional<Long> longSwap;    // long swap rate (for FUTURES and OPTIONS)
    public final Optional<Long> shortSwap;   // short swap rate (for FUTURES and OPTIONS)
    public final Optional<Boolean> isActive; // activity status (inactive or active)

    public SymbolSpecification(BytesIn bytes) {
        this.symbolId = bytes.readInt();
        this.type = SymbolType.of(bytes.readByte());
        this.baseCurrency = bytes.readInt();
        this.quoteCurrency = bytes.readInt();
        this.baseScaleK = bytes.readLong();
        this.quoteScaleK = bytes.readLong();
        this.takerFee = bytes.readLong();
        this.makerFee = bytes.readLong();
        this.marginBuy = bytes.readLong();
        this.marginSell = bytes.readLong();
        this.highLimit = readOptionalLong(bytes);
        this.lowLimit = readOptionalLong(bytes);
        this.longSwap = readOptionalLong(bytes);
        this.shortSwap = readOptionalLong(bytes);
        this.isActive = readOptionalBoolean(bytes);
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeInt(symbolId);
        bytes.writeByte(type.getCode());
        bytes.writeInt(baseCurrency);
        bytes.writeInt(quoteCurrency);
        bytes.writeLong(baseScaleK);
        bytes.writeLong(quoteScaleK);
        bytes.writeLong(takerFee);
        bytes.writeLong(makerFee);
        bytes.writeLong(marginBuy);
        bytes.writeLong(marginSell);
        writeOptionalLong(highLimit, bytes);
        writeOptionalLong(lowLimit, bytes);
        writeOptionalLong(longSwap, bytes);
        writeOptionalLong(shortSwap, bytes);
        writeOptionalBoolean(isActive, bytes);
    }


    @Override
    public int stateHash() {
        return Objects.hash(
                symbolId,
                type.getCode(),
                baseCurrency,
                quoteCurrency,
                baseScaleK,
                quoteScaleK,
                takerFee,
                makerFee,
                marginBuy,
                marginSell,
                highLimit.orElse(0L),
                lowLimit.orElse(0L),
                longSwap.orElse(0L),
                shortSwap.orElse(0L),
                isActive.orElse(false)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolSpecification that = (SymbolSpecification) o;
        return symbolId == that.symbolId &&
                baseCurrency == that.baseCurrency &&
                quoteCurrency == that.quoteCurrency &&
                baseScaleK == that.baseScaleK &&
                quoteScaleK == that.quoteScaleK &&
                takerFee == that.takerFee &&
                makerFee == that.makerFee &&
                marginBuy == that.marginBuy &&
                marginSell == that.marginSell &&
                Objects.equals(highLimit, that.highLimit) &&
                Objects.equals(lowLimit, that.lowLimit) &&
                Objects.equals(longSwap, that.longSwap) &&
                Objects.equals(shortSwap, that.shortSwap) &&
                Objects.equals(isActive, that.isActive) &&
                type == that.type;
    }

    private Optional<Long> readOptionalLong(BytesIn bytes) {
        return bytes.readBoolean() ? Optional.of(bytes.readLong()) : Optional.empty();
    }

    private Optional<Boolean> readOptionalBoolean(BytesIn bytes) {
        return bytes.readBoolean() ? Optional.of(bytes.readBoolean()) : Optional.empty();
    }

    private void writeOptionalLong(Optional<Long> value, BytesOut bytes) {
        bytes.writeBoolean(value.isPresent());
        value.ifPresent(bytes::writeLong);
    }

    private void writeOptionalBoolean(Optional<Boolean> value, BytesOut bytes) {
        bytes.writeBoolean(value.isPresent());
        value.ifPresent(bytes::writeBoolean);
    }
}