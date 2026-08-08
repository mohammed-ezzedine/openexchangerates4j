package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenExchangeRates4jAutoConfigurationTest {

    private static final String SCHEDULER_BEAN_NAME = "openExchangeRatesCurrencyRatesRefreshScheduler";

    // Beans are marked lazy so that merely checking whether a bean definition was registered
    // (i.e. whether its @Conditional matched) never actually instantiates it. The refresh scheduler
    // bean performs a real network call on a background thread as soon as it's constructed, which
    // must not happen as a side effect of this configuration-wiring test.
    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withInitializer(context -> context.addBeanFactoryPostProcessor((ConfigurableListableBeanFactory beanFactory) ->
                        Arrays.stream(beanFactory.getBeanDefinitionNames())
                                .forEach(name -> beanFactory.getBeanDefinition(name).setLazyInit(true))))
                .withUserConfiguration(Configuration.class);
    }

    @Test
    @DisplayName("it should not register the proactive refresh scheduler bean by default")
    void it_should_not_register_the_proactive_refresh_scheduler_bean_by_default() {
        runner()
                .withPropertyValues("openexchangerates.appId=some-app-id")
                .run(context -> assertFalse(context.containsBeanDefinition(SCHEDULER_BEAN_NAME)));
    }

    @Test
    @DisplayName("it should register the proactive refresh scheduler bean when caching and refresh are both enabled")
    void it_should_register_the_proactive_refresh_scheduler_bean_when_caching_and_refresh_are_both_enabled() {
        runner()
                .withPropertyValues(
                        "openexchangerates.appId=some-app-id",
                        "openexchangerates.cache.enabled=true",
                        "openexchangerates.cache.refresh.enabled=true"
                )
                .run(context -> assertTrue(context.containsBeanDefinition(SCHEDULER_BEAN_NAME)));
    }

    @Test
    @DisplayName("it should not register the proactive refresh scheduler bean when caching is disabled, even if refresh is enabled")
    void it_should_not_register_the_proactive_refresh_scheduler_bean_when_caching_is_disabled_even_if_refresh_is_enabled() {
        runner()
                .withPropertyValues(
                        "openexchangerates.appId=some-app-id",
                        "openexchangerates.cache.enabled=false",
                        "openexchangerates.cache.refresh.enabled=true"
                )
                .run(context -> assertFalse(context.containsBeanDefinition(SCHEDULER_BEAN_NAME)));
    }

    @Import(OpenExchangeRates4jAutoConfiguration.class)
    static class Configuration {

    }
}
