package com.axes.razorcore.core;

import lombok.*;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;

@Builder
@Getter
@ToString(callSuper = true)
public class ForexSymbolSpecification extends SymbolSpecification {

    public final String forexName;
    public final int baseCurrency;  // base currency
    public final int quoteCurrency; // quote/counter currency (OR futures contract currency)
    public final long baseScaleK;   // base currency amount multiplier (lot size in base currency units)
    public final long quoteScaleK;  // quote currency amount multiplier (step size in quote currency units)
    public final SymbolSpecification symbolSpecification;
    public ForexSymbolSpecification(BytesIn bytes, int symbolId, @NonNull SymbolType type, String exchange, long takerFee, long makerFee, long marginBuy, long marginSell) {
        super(bytes);
        this.symbolSpecification = new SymbolSpecification(symbolId, type, exchange, takerFee, makerFee, marginBuy, marginSell);
        this.forexName = bytes.readUtf8();
        this.baseCurrency = bytes.readInt();
        this.quoteCurrency = bytes.readInt();
        this.baseScaleK = bytes.readLong();
        this.quoteScaleK = bytes.readLong();
    }


    //    @Builder
//    public ForexSymbolSpecification(int symbolId, @NonNull SymbolType type, String exchange, long takerFee, long makerFee, long marginBuy, long marginSell, String forexName, int baseCurrency, int quoteCurrency, long baseScaleK, long quoteScaleK) {
//        super(symbolId, type, exchange, takerFee, makerFee, marginBuy, marginSell);
//        this.forexName = forexName;
//        this.baseCurrency = baseCurrency;
//        this.quoteCurrency = quoteCurrency;
//        this.baseScaleK = baseScaleK;
//        this.quoteScaleK = quoteScaleK;
//    }
//
//    public ForexSymbolSpecification(BytesIn bytes) {
//        super(bytes);
//        this.forexName = bytes.readUtf8();
//        this.baseCurrency = bytes.readInt();
//        this.quoteCurrency = bytes.readInt();
//        this.baseScaleK = bytes.readLong();
//        this.quoteScaleK = bytes.readLong();
//    }


    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
        super.writeMarshallable(bytes);
        bytes.writeUtf8(forexName);
        bytes.writeInt(baseCurrency);
        bytes.writeInt(quoteCurrency);
        bytes.writeLong(baseScaleK);
        bytes.writeLong(quoteScaleK);
    }

    @Override
    public int stateHash() {
        super.stateHash();
        return Objects.hash(
                forexName,
                baseCurrency,
                quoteCurrency,
                baseScaleK,
                quoteScaleK
        );
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()) return false;
        if(this == obj) return true;
        ForexSymbolSpecification that = (ForexSymbolSpecification) obj;
        return super.equals(obj) &&
                Objects.equals(forexName, that.forexName) &&
                baseCurrency == that.baseCurrency &&
                quoteCurrency == that.quoteCurrency &&
                baseScaleK == that.baseScaleK &&
                quoteScaleK == that.quoteScaleK;

    }
}
