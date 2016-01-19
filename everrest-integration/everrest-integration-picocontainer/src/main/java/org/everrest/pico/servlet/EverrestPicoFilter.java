/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.pico.servlet;

import org.everrest.core.ApplicationContext;
import org.everrest.core.InitialProperties;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.EnvironmentContext;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.web.PicoServletContainerFilter;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.lang.reflect.Type;

/**
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class EverrestPicoFilter extends PicoServletContainerFilter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EverrestPicoFilter.class);

    public static class HttpHeadersInjector extends AbstractAdapter<HttpHeaders> {
        public HttpHeadersInjector() {
            super(HttpHeaders.class, HttpHeaders.class);
        }

        @Override
        public HttpHeaders getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getHttpHeaders();
        }

        @Override
        public String getDescriptor() {
            return "HttpHeaders";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class InitialPropertiesInjector extends AbstractAdapter<InitialProperties> {
        public InitialPropertiesInjector() {
            super(InitialProperties.class, InitialProperties.class);
        }

        @Override
        public InitialProperties getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getInitialProperties();
        }

        @Override
        public String getDescriptor() {
            return "InitialProperties";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class ProvidersInjector extends AbstractAdapter<Providers> {
        public ProvidersInjector() {
            super(Providers.class, Providers.class);
        }

        @Override
        public Providers getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getProviders();
        }

        @Override
        public String getDescriptor() {
            return "Providers";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class RequestInjector extends AbstractAdapter<Request> {
        public RequestInjector() {
            super(Request.class, Request.class);
        }

        @Override
        public Request getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getRequest();
        }

        @Override
        public String getDescriptor() {
            return "Request";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class SecurityContextInjector extends AbstractAdapter<SecurityContext> {
        public SecurityContextInjector() {
            super(SecurityContext.class, SecurityContext.class);
        }

        @Override
        public SecurityContext getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getSecurityContext();
        }

        @Override
        public String getDescriptor() {
            return "SecurityContext";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class ServletConfigInjector extends AbstractAdapter<ServletConfig> {
        public ServletConfigInjector() {
            super(ServletConfig.class, ServletConfig.class);
        }

        @Override
        public ServletConfig getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {

            EnvironmentContext context = EnvironmentContext.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest EnvironmentContext is not initialized.");
            }
            return (ServletConfig)context.get(ServletConfig.class);
        }

        @Override
        public String getDescriptor() {
            return "ServletConfig";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class ServletContextInjector extends AbstractAdapter<ServletContext> {
        public ServletContextInjector() {
            super(ServletContext.class, ServletContext.class);
        }

        @Override
        public ServletContext getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {

            EnvironmentContext context = EnvironmentContext.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest EnvironmentContext is not initialized.");
            }
            return (ServletContext)context.get(ServletContext.class);
        }

        @Override
        public String getDescriptor() {
            return "ServletContext";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class UriInfoInjector extends AbstractAdapter<UriInfo> {
        public UriInfoInjector() {
            super(UriInfo.class, UriInfo.class);
        }

        @Override
        public UriInfo getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getUriInfo();
        }

        @Override
        public String getDescriptor() {
            return "UriInfo";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    public static class ApplicationInjector extends AbstractAdapter<Application> {
        public ApplicationInjector() {
            super(Application.class, Application.class);
        }

        @Override
        public Application getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
            }
            return context.getApplication();
        }

        @Override
        public String getDescriptor() {
            return "Application";
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
        }
    }

    private static final ThreadLocal<MutablePicoContainer> currentAppContainer     = new ThreadLocal<MutablePicoContainer>();
    private static final ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
    private static final ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();

    public static <T> T getComponent(Class<T> type) {
        // Since containers are inherited start lookup components from top
        // container. It is application scope container in our case.
        T object = null;
        object = getAppContainer().getComponent(type);
        if (object == null) {
            final MutablePicoContainer sessionContainer = getSessionContainer();
            if (sessionContainer != null) {
                object = sessionContainer.getComponent(type);
            }
        }
        if (object == null) {
            object = getRequestContainer().getComponent(type);
        }
        if (object == null && LOG.isDebugEnabled()) {
            LOG.debug("Component with type " + type.getName() + " not found in any containers.");
        }

        return object;
    }

    public static Object getComponent(Object key) {
        // Since containers are inherited start lookup components from top
        // container. It is application scope container in our case.
        Object object = null;
        object = getAppContainer().getComponent(key);
        if (object == null) {
            final MutablePicoContainer sessionContainer = getSessionContainer();
            if (sessionContainer != null) {
                object = sessionContainer.getComponent(key);
            }
        }
        if (object == null) {
            object = getRequestContainer().getComponent(key);
        }
        if (object == null && LOG.isDebugEnabled()) {
            LOG.debug("Component " + key + " not found in any containers.");
        }
        return object;
    }

    static MutablePicoContainer getAppContainer() {
        MutablePicoContainer container = currentAppContainer.get();
        if (container == null) {
            throw new IllegalStateException("No container was found in application scope. ");
        }
        return container;
    }

    static MutablePicoContainer getRequestContainer() {
        MutablePicoContainer container = currentRequestContainer.get();
        if (container == null) {
            throw new IllegalStateException("No container was found in request scope. ");
        }
        return container;
    }

    static MutablePicoContainer getSessionContainer() {
        return currentSessionContainer.get();
    }

    @Override
    public void destroy() {
        try {
            currentAppContainer.remove();
            currentSessionContainer.remove();
            currentRequestContainer.remove();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        super.destroy();
    }

    @Override
    public void initAdditionalScopedComponents(MutablePicoContainer sessionContainer, MutablePicoContainer reqContainer) {
        // Add injectors for some components required by JAX-RS resources and providers.

        // NOTE: Still have issue with injected components via constructors. JAX-RS
        // specification provide wide set of annotations that can be applied to
        // wide set of Java types, e.g. @CookieParam, @QueryParam, etc. See
        // section 3.1.2 of JAX-RS specification. How to do it with picocontainer ???
        // This issue ONLY for constructor parameters, all fields for components
        // of 'request container' will be initialized in
        // PicoObjectFactory.getInstance(ApplicationContext).
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new InitialPropertiesInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new HttpHeadersInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ProvidersInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new RequestInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new SecurityContextInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new UriInfoInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ApplicationInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ServletConfigInjector());
        reqContainer.as(Characteristics.NO_CACHE).addAdapter(new ServletContextInjector());
    }

    @Override
    public void setAppContainer(MutablePicoContainer container) {
        currentAppContainer.set(container);
    }

    @Override
    public void setRequestContainer(MutablePicoContainer container) {
        currentRequestContainer.set(container);
    }

    @Override
    public void setSessionContainer(MutablePicoContainer container) {
        currentSessionContainer.set(container);
    }
}
