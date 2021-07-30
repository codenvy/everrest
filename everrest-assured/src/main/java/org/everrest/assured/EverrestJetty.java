/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.assured;

import com.jayway.restassured.RestAssured;

import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Field;


public class EverrestJetty implements ITestListener, IInvokedMethodListener {

    public final static  String JETTY_PORT   = "jetty-port";
    public final static  String JETTY_SERVER = "jetty-server";
    private static final Logger LOG          = LoggerFactory.getLogger(EverrestJetty.class);
    private JettyHttpServer httpServer;


    /**
     * @see org.testng.IInvokedMethodListener#afterInvocation(org.testng.IInvokedMethod,
     * org.testng.ITestResult)
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (httpServer != null && hasEverrestJettyListener(method.getTestMethod().getInstance().getClass())) {
            httpServer.resetFactories();
            httpServer.resetFilter();
        }

    }

    /**
     * @see org.testng.IInvokedMethodListener#beforeInvocation(org.testng.IInvokedMethod,
     * org.testng.ITestResult)
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (httpServer != null && hasEverrestJettyListener(method.getTestMethod().getInstance().getClass())) {
            httpServer.resetFactories();
            httpServer.resetFilter();
            initRestResource(method.getTestMethod());
        }
    }

    public void onFinish(ITestContext context) {
        JettyHttpServer httpServer = (JettyHttpServer)context.getAttribute(JETTY_SERVER);
        if (httpServer != null) {
            try {
                httpServer.stop();
                httpServer = null;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }

        }
    }

    @Override
    public void onTestStart(ITestResult result) {

    }

    @Override
    public void onTestSuccess(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {

    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    public void onStart(ITestContext context) {

        ITestNGMethod[] allTestMethods = context.getAllTestMethods();
        if (allTestMethods == null) {
            return;
        }
        if (httpServer == null && hasEverrestJettyListenerTestHierarchy(allTestMethods)) {
            httpServer = new JettyHttpServer();

            context.setAttribute(JETTY_PORT, httpServer.getPort());
            context.setAttribute(JETTY_SERVER, httpServer);

            try {
                httpServer.start();
                httpServer.resetFactories();
                httpServer.resetFilter();
                RestAssured.port = httpServer.getPort();
                RestAssured.basePath = JettyHttpServer.UNSECURE_REST;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
        }
    }

    private void initRestResource(ITestNGMethod... testMethods) {
        for (ITestNGMethod testMethod : testMethods) {
            Object instance = testMethod.getInstance();

            if (hasEverrestJettyListenerTestHierarchy(instance.getClass())) {
                EverrestApplication everrest = new EverrestApplication();
                Field[] fields = instance.getClass().getDeclaredFields();
                for (Field field : fields) {
                    try {
                        if (isRestResource(field.getType())) {
                            ObjectFactory<? extends ObjectModel> factory = createFactory(instance, field);
                            if (factory != null) {
                                everrest.addFactory(factory);
                            }
                        } else if (javax.servlet.Filter.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            Object fieldInstance = field.get(instance);
                            if (fieldInstance != null) {
                                httpServer.addFilter(((javax.servlet.Filter)fieldInstance), "/*");
                            } else {
                                httpServer.addFilter((Class<? extends javax.servlet.Filter>)field.getType(), "/*");
                            }
                        }
                    } catch (IllegalAccessException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                if (everrest.getFactories().size() > 0) {
                    httpServer.publish(everrest);
                }
            }
        }
    }


    private boolean hasEverrestJettyListener(Class<?> clazz) {
        Listeners listeners = clazz.getAnnotation(Listeners.class);
        if (listeners == null) {
            return false;
        }

        for (Class<? extends ITestNGListener> listenerClass : listeners.value()) {
            if (EverrestJetty.class.isAssignableFrom(listenerClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEverrestJettyListenerTestHierarchy(Class<?> testClass) {
        for (Class<?> clazz = testClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            if (hasEverrestJettyListener(clazz)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEverrestJettyListenerTestHierarchy(ITestNGMethod... testMethods) {
        for (ITestNGMethod testMethod : testMethods) {
            Object instance = testMethod.getInstance();
            if (hasEverrestJettyListenerTestHierarchy(instance.getClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRestResource(Class<?> resourceClass) {
        return resourceClass.isAnnotationPresent(Path.class) ||
               resourceClass.isAnnotationPresent(Provider.class) ||
               resourceClass.isAnnotationPresent(Filter.class) ||
               resourceClass.isAssignableFrom(ExceptionMapper.class) ||
               resourceClass.isAssignableFrom(ContextResolver.class) ||
               resourceClass.isAssignableFrom(MessageBodyReader.class) ||
               resourceClass.isAssignableFrom(MessageBodyWriter.class) ||
               resourceClass.isAssignableFrom(MethodInvokerFilter.class) ||
               resourceClass.isAssignableFrom(RequestFilter.class) ||
               resourceClass.isAssignableFrom(ResponseFilter.class);
    }

    public ObjectFactory<? extends ObjectModel> createFactory(Object testObject, Field field) {
        Class clazz = (Class)field.getType();
        if (clazz.getAnnotation(Provider.class) != null) {
            ProviderDescriptor providerDescriptor = new ProviderDescriptorImpl(clazz);
            return new TestResourceFactory<>(providerDescriptor, testObject, field);
        } else if (clazz.getAnnotation(Filter.class) != null) {
            FilterDescriptor filterDescriptor = new FilterDescriptorImpl(clazz);
            return new TestResourceFactory<>(filterDescriptor, testObject, field);
        } else if (clazz.getAnnotation(Path.class) != null) {
            AbstractResourceDescriptor resourceDescriptor = new AbstractResourceDescriptor(clazz);
            return new TestResourceFactory<>(resourceDescriptor, testObject, field);
        }
        return null;
    }
}
