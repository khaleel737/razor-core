package com.axes.razorcore.tests.test.integration;

import exchange.core2.core.common.config.PerformanceConfiguration;

public final class ITFeesMarginBasic extends ITFeesMargin {
    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
