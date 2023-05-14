package com.axes.razorcore.tests.test.integration;

import com.axes.razorcore.config.PerformanceConfiguration;

public class ITExchangeCoreIntegrationRejectionLatency extends ITExchangeCoreIntegrationRejection {

    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.latencyPerformanceBuilder().build();
    }
}
