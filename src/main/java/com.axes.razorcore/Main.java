package com.axes.razorcore;

import com.axes.razorcore.core.ForexSymbolSpecification;
import com.axes.razorcore.core.SymbolSpecification;
import com.axes.razorcore.core.SymbolType;

public class Main {

    SymbolSpecification symbol = new SymbolSpecification(334, SymbolType.EQUITIES, "NYSE", 3123, 13312, 4324, 12313);
    public static void main(String[] args) {

        SymbolSpecification equitySymbolSpecification = ForexSymbolSpecification.builder()
                .symbolSpecification(new SymbolSpecification(334, SymbolType.EQUITIES, "NYSE", 3123, 13312, 4324, 12313))
                .forexName("EUR/USD")
                .baseCurrency(77)
                .quoteCurrency(44)
                .quoteScaleK(321)
                .baseScaleK(321)
                .build();

        equitySymbolSpecification.toString();
    }
}
