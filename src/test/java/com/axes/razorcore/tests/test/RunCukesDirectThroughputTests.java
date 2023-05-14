package com.axes.razorcore.tests.test;

import exchange.core2.core.common.config.PerformanceConfiguration;
import exchange.core2.tests.steps.OrderStepdefs;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResources({
    @SelectClasspathResource("exchange/core2/tests/features/basic.feature"),
    @SelectClasspathResource("exchange/core2/tests/features/risk.feature")
})
@ConfigurationParameters({
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber/cucumber.html, exchange.core2.tests.RunCukesDirectThroughputTests$CukeNaiveLifeCycleHandler"),
})
@Slf4j
public class RunCukesDirectThroughputTests {

    public static class CukeNaiveLifeCycleHandler implements EventListener {

        @Override
        public void setEventPublisher(EventPublisher eventPublisher) {
            eventPublisher.registerHandlerFor(TestRunStarted.class,
                event -> OrderStepdefs.testPerformanceConfiguration = PerformanceConfiguration.throughputPerformanceBuilder()
                    .build());
            eventPublisher.registerHandlerFor(TestRunFinished.class,
                event -> OrderStepdefs.testPerformanceConfiguration = null);
        }
    }
}
