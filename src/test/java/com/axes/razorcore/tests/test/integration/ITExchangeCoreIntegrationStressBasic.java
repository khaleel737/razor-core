package com.axes.razorcore.tests.test.integration;

import exchange.core2.core.common.config.PerformanceConfiguration;

public class ITExchangeCoreIntegrationStressBasic extends ITExchangeCoreIntegrationStress {

    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
