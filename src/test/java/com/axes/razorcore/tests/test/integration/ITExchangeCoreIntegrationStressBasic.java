package com.axes.razorcore.tests.test.integration;

import com.axes.razorcore.config.PerformanceConfiguration;

public class ITExchangeCoreIntegrationStressBasic extends ITExchangeCoreIntegrationStress {

    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
