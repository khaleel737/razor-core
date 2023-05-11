package com.axes.razorcore.core;

import lombok.NonNull;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;

public class OptionsSymbolSpecification extends SymbolSpecification {
    public final String equityOptionName; // name of equity option
    public final String equityOptionSymbol; // symbol of equity option
    public final long contractSizeO; // size of equity option contract
    public final long exercisePrice; // option exercise price
    public final OrderDirection optionType; // option type (call/put)
    public final long expirationDate; // option expiration date
    public final SymbolSpecification symbolSpecification;

    public OptionsSymbolSpecification(BytesIn bytes, int symbolId, @NonNull SymbolType type, String exchange, long takerFee, long makerFee, long marginBuy, long marginSell) {
        super(bytes);
        this.symbolSpecification = new SymbolSpecification(symbolId, type, exchange, takerFee, makerFee, marginBuy, marginSell);
        this.equityOptionName = bytes.readUtf8();
        this.equityOptionSymbol = bytes.readUtf8();
        this.contractSizeO = bytes.readLong();
        this.exercisePrice = bytes.readLong();
        this.optionType = OrderDirection.of(bytes.readByte());
        this.expirationDate = bytes.readLong();
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
        super.writeMarshallable(bytes);
        bytes.writeUtf8(equityOptionName);
        bytes.writeUtf8(equityOptionSymbol);
        bytes.writeLong(contractSizeO);
        bytes.writeLong(exercisePrice);
        bytes.writeInt(optionType.getMultiplier());
        bytes.writeLong(expirationDate);
    }

    @Override
    public int stateHash() {
        super.stateHash();
        return Objects.hash(
                equityOptionName,
                equityOptionSymbol,
                contractSizeO,
                exercisePrice,
                optionType,
                expirationDate
        );
    }
}
