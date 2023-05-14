package com.axes.razorcore.tests.test.integration;

import com.axes.razorcore.config.PerformanceConfiguration;

public final class ITFeesMarginBasic extends ITFeesMargin {
    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
