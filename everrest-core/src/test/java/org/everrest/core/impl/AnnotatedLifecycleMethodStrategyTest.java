/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

public class AnnotatedLifecycleMethodStrategyTest {
    static final List<String> callAssertion = new ArrayList<>();

    public static class WithLifeCycleAnnotation {
        @PostConstruct
        public void init() {
            callAssertion.add("init");
        }

        @PostConstruct
        private void nonPublicInit() {
            callAssertion.add("nonPublicInit");
        }

        @PostConstruct
        public static void staticInit() {
            callAssertion.add("staticInit");
        }

        @PostConstruct
        public void initWithParams(String str) {
            callAssertion.add("initWithParams");
        }

        @PostConstruct
        public void initThatThrowsCheckedException() throws Exception {
            callAssertion.add("initThatThrowsCheckedException");
        }

        @PreDestroy
        public void destroy() {
            callAssertion.add("destroy");
        }

        @PreDestroy
        private void nonPublicdestroy() {
            callAssertion.add("nonPublicDestroy");
        }

        @PreDestroy
        public static void staticDestroy() {
            callAssertion.add("staticDestroy");
        }

        @PreDestroy
        public void destroyWithParams(String str) {
            callAssertion.add("destroyWithParams");
        }

        @PreDestroy
        public void destroyThatThrowsCheckedException() throws Exception {
            callAssertion.add("destroyThatThrowsCheckedException");
        }
    }

    @Rule public ExpectedException thrown = ExpectedException.none();
    private AnnotatedLifecycleMethodStrategy lifecycleMethodStrategy;

    @Before
    public void setUp() throws Exception {
        lifecycleMethodStrategy = new AnnotatedLifecycleMethodStrategy();
    }

    @After
    public void tearDown() throws Exception {
        callAssertion.clear();
    }

    @Test
    public void invokesInitializeMethods() throws Exception {
        lifecycleMethodStrategy.invokeInitializeMethods(new WithLifeCycleAnnotation());
        assertTrue(String.format("Only \"init\" and \"nonPublicInit\" methods are expected to be invoked but %s were invoked", callAssertion),
                   callAssertion.size() == 2 && callAssertion.containsAll(newArrayList("init", "nonPublicInit")));
    }

    @Test
    public void invokesDestroyMethods() throws Exception {
        lifecycleMethodStrategy.invokeDestroyMethods(new WithLifeCycleAnnotation());
        assertTrue(String.format("Only \"destroy\" and \"nonPublicDestroy\" methods are expected to be invoked but %s were invoked", callAssertion),
                   callAssertion.size() == 2 && callAssertion.containsAll(newArrayList("destroy", "nonPublicDestroy")));
    }

    public static class LifeCycleMethodsThrowsRuntimeException {
        @PostConstruct
        public void init() {
            throw new RuntimeException("init fails");
        }

        @PreDestroy
        public void destroy() {
            throw new RuntimeException("destroy fails");
        }
    }

    @Test
    public void wrapsRuntimeExceptionThrownByInitMethodWithInternalException() {
        thrown.expect(lifecycleMethodInvocationExceptionMatcher("init fails"));
        lifecycleMethodStrategy.invokeInitializeMethods(new LifeCycleMethodsThrowsRuntimeException());
    }

    @Test
    public void wrapsRuntimeExceptionThrownByDestroyMethodWithInternalException() {
        thrown.expect(lifecycleMethodInvocationExceptionMatcher("destroy fails"));
        lifecycleMethodStrategy.invokeDestroyMethods(new LifeCycleMethodsThrowsRuntimeException());
    }

    private BaseMatcher<Throwable> lifecycleMethodInvocationExceptionMatcher(String expectedMessage) {
        return new BaseMatcher<Throwable>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof InternalException
                       && ((InternalException) item).getCause() instanceof RuntimeException
                       && expectedMessage.equals(((InternalException) item).getCause().getMessage());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Expected exception with message: %s", expectedMessage));
            }
        };
    }

}