//package com.axes.razorcore.core;
//
//import lombok.Builder;
//import lombok.Getter;
//import lombok.ToString;
//
//
//@Builder
//@Getter
//@ToString
//public class EquitySymbolSpecification extends SymbolSpecification {
////
////    // equity specification
////    public final String equityName; // name of equity stock
////    public final String equitySymbol; // symbol of equity stock
////    public final long equityPrice;
////    public final SymbolSpecification symbolSpecification;
////
////    //    Constructor for All Equity Symbols
////    public EquitySymbolSpecification(BytesIn bytes, int symbolId, @NonNull SymbolType type, String exchange, long takerFee, long makerFee, long marginBuy, long marginSell) {
////        super(bytes);
////        this.symbolSpecification = new SymbolSpecification(symbolId, type, exchange, takerFee, makerFee, marginBuy, marginSell);
////        this.equityName = bytes.readUtf8();
////        this.equitySymbol = bytes.readUtf8();
////        this.equityPrice = bytes.readLong();
////    }
////
////    @Override
////    public void writeMarshallable(BytesOut bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
////        super.writeMarshallable(bytes);
////        bytes.writeUtf8(equityName);
////        bytes.writeUtf8(equitySymbol);
////        bytes.writeLong(equityPrice);
////    }
////
////    @Override
////    public int stateHash() {
////        super.stateHash();
////        return Objects.hash(
////                symbolId,
////                type.getCode(),
////                equityName,
////                equitySymbol,
////                equityPrice,
////                marginBuy ,
////                marginSell
////        );
////    }
//}
