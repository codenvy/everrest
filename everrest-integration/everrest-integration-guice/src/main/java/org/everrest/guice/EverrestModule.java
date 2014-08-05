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
package org.everrest.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import org.everrest.core.ApplicationContext;
import org.everrest.core.InitialProperties;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.EnvironmentContext;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author andrew00x
 */
public class EverrestModule implements Module {
    public static class HttpHeadersProvider implements Provider<HttpHeaders> {
        @Override
        public HttpHeaders get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getHttpHeaders();
        }
    }

    public static class InitialPropertiesProvider implements Provider<InitialProperties> {
        @Override
        public InitialProperties get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getInitialProperties();
        }
    }

    public static class ProvidersProvider implements Provider<Providers> {
        @Override
        public Providers get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getProviders();
        }
    }

    public static class RequestProvider implements Provider<Request> {
        @Override
        public Request get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getRequest();
        }
    }

    public static class SecurityContextProvider implements Provider<SecurityContext> {
        @Override
        public SecurityContext get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getSecurityContext();
        }
    }

    public static class ServletConfigProvider implements Provider<ServletConfig> {
        @Override
        public ServletConfig get() {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest EnvironmentContext is not initialized.");
            }
            return (ServletConfig)EnvironmentContext.getCurrent().get(ServletConfig.class);
        }
    }

    public static class UriInfoProvider implements Provider<UriInfo> {
        @Override
        public UriInfo get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getUriInfo();
        }
    }

    public static class ApplicationProvider implements Provider<Application> {
        @Override
        public Application get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getApplication();
        }
    }

    /**
     * Add binding for HttpHeaders, InitialProperties, Providers, Request, SecurityContext, ServletConfig, UriInfo. All this types will be
     * supported for injection in constructor or fields of component of Guice container.
     *
     * @see javax.inject.Inject
     * @see com.google.inject.Inject
     */
    @Override
    public void configure(Binder binder) {
        // Override if need other binding.
        binder.bind(HttpHeaders.class).toProvider(new HttpHeadersProvider());
        binder.bind(InitialProperties.class).toProvider(new InitialPropertiesProvider());
        binder.bind(Providers.class).toProvider(new ProvidersProvider());
        binder.bind(Request.class).toProvider(new RequestProvider());
        binder.bind(SecurityContext.class).toProvider(new SecurityContextProvider());
        binder.bind(ServletConfig.class).toProvider(new ServletConfigProvider());
        binder.bind(UriInfo.class).toProvider(new UriInfoProvider());
        binder.bind(Application.class).toProvider(new ApplicationProvider());
    }
}
