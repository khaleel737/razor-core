
package com.axes.razorcore.utils;

import com.axes.razorcore.core.SymbolSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CoreArithmeticUtils {

    public static long calculateAmountAsk(long size, SymbolSpecification spec) {
        return size * spec.baseScaleK;
    }

    public static long calculateAmountBid(long size, long price, SymbolSpecification spec) {
        return size * (price * spec.quoteScaleK);
    }

    public static long calculateAmountBidTakerFee(long size, long price, SymbolSpecification spec) {
        return size * (price * spec.quoteScaleK + spec.takerFee);
    }

    public static long calculateAmountBidReleaseCorrMaker(long size, long priceDiff, SymbolSpecification spec) {
        return size * (priceDiff * spec.quoteScaleK + (spec.takerFee - spec.makerFee));
    }

    public static long calculateAmountBidTakerFeeForBudget(long size, long budgetInSteps, SymbolSpecification spec) {

        return budgetInSteps * spec.quoteScaleK + size * spec.takerFee;
    }

}
