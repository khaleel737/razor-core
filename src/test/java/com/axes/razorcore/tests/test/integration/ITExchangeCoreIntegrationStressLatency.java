package com.axes.razorcore.tests.test.integration;

import exchange.core2.core.common.config.PerformanceConfiguration;

public class ITExchangeCoreIntegrationStressLatency extends ITExchangeCoreIntegrationStress {

    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
