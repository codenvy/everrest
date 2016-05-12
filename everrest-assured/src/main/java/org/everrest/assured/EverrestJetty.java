/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package org.everrest.assured;

import com.jayway.restassured.RestAssured;

import org.everrest.core.Filter;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.method.MethodInvokerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;
import org.testng.annotations.Listeners;

import javax.ws.rs.Path;
import javax.ws.rs.ext.*;
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
                Field[] fields = instance.getClass().getDeclaredFields();
                for (Field field : fields) {
                    try {
                        if (isRestResource(field.getType())) {
                            field.setAccessible(true);
                            Object fieldInstance = field.get(instance);
                            if (fieldInstance != null) {
                                httpServer.addSingleton(fieldInstance);
                            } else {
                                ///httpServer.addPerRequest(field.getType());
                                httpServer.addFactory(new TestResourceFactory(field.getType(), instance, field));
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

    private boolean isRestResource(Class<? extends Object> resourceClass) {
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

}
