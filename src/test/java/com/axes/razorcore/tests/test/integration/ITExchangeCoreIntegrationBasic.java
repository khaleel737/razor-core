package com.axes.razorcore.tests.test.integration;

import exchange.core2.core.common.config.PerformanceConfiguration;

public final class ITExchangeCoreIntegrationBasic extends ITExchangeCoreIntegration {

    @Override
    public PerformanceConfiguration getPerformanceConfiguration() {
        return PerformanceConfiguration.DEFAULT;
    }
}
