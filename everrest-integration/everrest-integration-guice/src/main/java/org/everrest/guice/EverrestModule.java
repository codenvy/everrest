/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestModule implements Module {
    public static class HttpHeadersProvider implements Provider<HttpHeaders> {
        public HttpHeaders get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getHttpHeaders();
        }
    }

    public static class InitialPropertiesProvider implements Provider<InitialProperties> {
        public InitialProperties get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getInitialProperties();
        }
    }

    public static class ProvidersProvider implements Provider<Providers> {
        public Providers get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getProviders();
        }
    }

    public static class RequestProvider implements Provider<Request> {
        public Request get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getRequest();
        }
    }

    public static class SecurityContextProvider implements Provider<SecurityContext> {
        public SecurityContext get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getSecurityContext();
        }
    }

    public static class ServletConfigProvider implements Provider<ServletConfig> {
        public ServletConfig get() {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest EnvironmentContext is not initialized.");
            }
            return (ServletConfig)EnvironmentContext.getCurrent().get(ServletConfig.class);
        }
    }

    public static class UriInfoProvider implements Provider<UriInfo> {
        public UriInfo get() {
            ApplicationContext context = ApplicationContextImpl.getCurrent();
            if (context == null) {
                throw new ProvisionException("EverRest ApplicationContext is not initialized.");
            }
            return context.getUriInfo();
        }
    }

    /**
     * Add binding for HttpHeaders, InitialProperties, Providers, Request,
     * SecurityContext, ServletConfig, UriInfo. All this types will be supported
     * for injection in constructor or fields of component of Guice container.
     *
     * @see com.google.inject.Inject
     */
    public void configure(Binder binder) {
        // Override if need other binding.
        binder.bind(HttpHeaders.class).toProvider(new HttpHeadersProvider());
        binder.bind(InitialProperties.class).toProvider(new InitialPropertiesProvider());
        binder.bind(Providers.class).toProvider(new ProvidersProvider());
        binder.bind(Request.class).toProvider(new RequestProvider());
        binder.bind(SecurityContext.class).toProvider(new SecurityContextProvider());
        binder.bind(ServletConfig.class).toProvider(new ServletConfigProvider());
        binder.bind(UriInfo.class).toProvider(new UriInfoProvider());
    }

}
