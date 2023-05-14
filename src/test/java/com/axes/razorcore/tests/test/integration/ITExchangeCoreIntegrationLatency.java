package com.axes.razorcore.tests.test.integration;

import exchange.core2.core.common.config.PerformanceConfiguration;

public final class ITExchangeCoreIntegrationLatency extends ITExchangeCoreIntegration {

    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.latencyPerformanceBuilder().build();
    }
}
