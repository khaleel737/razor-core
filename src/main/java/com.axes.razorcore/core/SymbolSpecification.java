package com.axes.razorcore.core;

import com.axes.razorcore.utils.StateHash;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;

@AllArgsConstructor
@Getter
@ToString
public class SymbolSpecification implements WriteBytesMarshallable, StateHash {

    public final int symbolId;
    @NonNull
    public final SymbolType type;
    public final String exchange; // stock exchange on which equity is traded
    public final long takerFee; // TODO check invariant: taker fee is not less than maker fee
    public final long makerFee;

    // margin settings (for type=FUTURES_CONTRACT only)
    public final long marginBuy;   // buy margin (quote currency)
    public final long marginSell;  // sell margin (quote currency)


    //Constructor for All Symbols, Need to seperate them for each of their own builder classes
    public SymbolSpecification(BytesIn bytes, EquitySymbolSpecification equitySymbolSpecification, ForexSymbolSpecification forexSymbolSpecification, OptionsSymbolSpecification optionsSymbolSpecification, FuturesSymbolSpecification futuresSymbolSpecification) {
        this.symbolId = bytes.readInt();
        this.type = SymbolType.of(bytes.readByte());
        this.exchange = bytes.readUtf8();
        this.takerFee = bytes.readLong();
        this.makerFee = bytes.readLong();
        this.marginBuy = bytes.readLong();
        this.marginSell = bytes.readLong();
    }


    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
        bytes.writeInt(symbolId);
        bytes.writeByte(type.getCode());
        bytes.writeUtf8(exchange);
        bytes.writeLong(takerFee);
        bytes.writeLong(makerFee);
        bytes.writeLong(marginBuy);
        bytes.writeLong(marginSell);
    }

    @Override
    public int stateHash() {
        return Objects.hash(
                symbolId,
                type.getCode(),
                exchange,
                takerFee,
                makerFee,
                marginBuy ,
                marginSell
        );
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        SymbolSpecification that = (SymbolSpecification) obj;
        return symbolId == that.symbolId &&
                Objects.equals(exchange, that.exchange) &&
                takerFee == that.takerFee &&
                makerFee == that.makerFee &&
                marginBuy == that.marginBuy &&
                marginSell == that.marginSell &&
                type == that.type;
    }
}
