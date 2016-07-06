package org.everrest.core.impl;

import org.everrest.core.LifecycleMethodStrategy;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LifecycleComponentTest {
    @Test
    public void invokesInitializeMethodWithDefaultLifecycleMethodStrategy() throws Exception {
        Resource instance  = new Resource();
        LifecycleComponent lifecycleComponent = new LifecycleComponent(instance);
        lifecycleComponent.initialize();

        assertEquals(1, instance.initVisitsCounter.get());
    }

    @Test
    public void invokesDestroyMethodWithDefaultLifecycleMethodStrategy() throws Exception {
        Resource instance  = new Resource();
        LifecycleComponent lifecycleComponent = new LifecycleComponent(instance);
        lifecycleComponent.destroy();

        assertEquals(1, instance.destroyVisitsCounter.get());
    }

    @Test
    public void invokesInitializeMethodWithCustomLifecycleMethodStrategy() throws Exception {
        LifecycleMethodStrategy lifecycleMethodStrategy = mock(LifecycleMethodStrategy.class);
        Resource instance  = new Resource();
        LifecycleComponent lifecycleComponent = new LifecycleComponent(instance, lifecycleMethodStrategy);
        lifecycleComponent.initialize();

        verify(lifecycleMethodStrategy).invokeInitializeMethods(instance);
    }

    @Test
    public void invokesDestroyMethodWithCustomLifecycleMethodStrategy() throws Exception {
        LifecycleMethodStrategy lifecycleMethodStrategy = mock(LifecycleMethodStrategy.class);
        Resource instance  = new Resource();
        LifecycleComponent lifecycleComponent = new LifecycleComponent(instance, lifecycleMethodStrategy);
        lifecycleComponent.destroy();

        verify(lifecycleMethodStrategy).invokeDestroyMethods(instance);
    }

    public static class Resource {
        private AtomicInteger initVisitsCounter = new AtomicInteger();
        private AtomicInteger destroyVisitsCounter = new AtomicInteger();

        @PostConstruct
        void init() {
            initVisitsCounter.incrementAndGet();
        }

        @PreDestroy
        void destroy() {
            destroyVisitsCounter.incrementAndGet();
        }
    }
}