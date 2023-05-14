package com.axes.razorcore.tests.test.integration;

import com.axes.razorcore.config.PerformanceConfiguration;

public class ITFeesExchangeBasic extends ITFeesExchange {
    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
