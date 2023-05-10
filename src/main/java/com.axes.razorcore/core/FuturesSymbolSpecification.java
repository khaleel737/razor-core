package com.axes.razorcore.core;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;

@Getter
@Builder
public class FuturesSymbolSpecification extends SymbolSpecification {
    public final String futuresContractName; // name of futures contract
    public final String futuresContractSymbol; // symbol of futures contract
    public final long expirationTimeF; // expiration time of futures contract
    public final long contractSizeF; // size of futures contract

    public FuturesSymbolSpecification(BytesIn bytes, int symbolId, @NonNull SymbolType type, String exchange, long takerFee, long makerFee, long marginBuy, long marginSell) {
        super(symbolId, type, exchange, takerFee, makerFee, marginBuy, marginSell);
        this.futuresContractName = bytes.readUtf8();
        this.futuresContractSymbol = bytes.readUtf8();
        this.expirationTimeF = bytes.readLong();
        this.contractSizeF = bytes.readLong();
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
        super.writeMarshallable(bytes);
        bytes.writeUtf8(futuresContractName);
        bytes.writeUtf8(futuresContractSymbol);
        bytes.writeLong(expirationTimeF);
        bytes.writeLong(contractSizeF);
    }

    @Override
    public int stateHash() {
       super.stateHash();
       return Objects.hash(
               futuresContractName,
               futuresContractSymbol,
               expirationTimeF,
               contractSizeF
       );
    }
}
