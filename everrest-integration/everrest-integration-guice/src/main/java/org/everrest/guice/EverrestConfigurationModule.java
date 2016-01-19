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
package org.everrest.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.ServletModule;

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.servlet.EverrestServletContextInitializer;

import javax.inject.Named;
import javax.servlet.ServletContext;

import static org.everrest.core.impl.EverrestConfiguration.EVERREST_ASYNCHRONOUS;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_ASYNCHRONOUS_CACHE_SIZE;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_ASYNCHRONOUS_JOB_TIMEOUT;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_ASYNCHRONOUS_POOL_SIZE;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_ASYNCHRONOUS_QUEUE_SIZE;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_ASYNCHRONOUS_SERVICE_PATH;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_CHECK_SECURITY;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_HTTP_METHOD_OVERRIDE;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE;
import static org.everrest.core.impl.EverrestConfiguration.EVERREST_NORMALIZE_URI;
import static org.everrest.core.impl.EverrestConfiguration.METHOD_INVOKER_DECORATOR_FACTORY;

/**
 * @author andrew00x
 */
public class EverrestConfigurationModule extends ServletModule {

    static class EverrestConfigurationProvider implements Provider<EverrestConfiguration> {
        ServletContext servletContext;

        EverrestConfigurationProvider(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Inject(optional = true)
        @Named(EVERREST_ASYNCHRONOUS)
        private String asynchronous;
        @Inject(optional = true)
        @Named(EVERREST_ASYNCHRONOUS_CACHE_SIZE)
        private String asynchronousCacheSize;
        @Inject(optional = true)
        @Named(EVERREST_ASYNCHRONOUS_JOB_TIMEOUT)
        private String asynchronousJobTimeout;
        @Inject(optional = true)
        @Named(EVERREST_ASYNCHRONOUS_POOL_SIZE)
        private String asynchronousPoolSize;
        @Inject(optional = true)
        @Named(EVERREST_ASYNCHRONOUS_QUEUE_SIZE)
        private String asynchronousQueueSize;
        @Inject(optional = true)
        @Named(EVERREST_ASYNCHRONOUS_SERVICE_PATH)
        private String asynchronousServicePath;
        @Inject(optional = true)
        @Named(EVERREST_CHECK_SECURITY)
        private String checkSecurity;
        @Inject(optional = true)
        @Named(EVERREST_HTTP_METHOD_OVERRIDE)
        private String httpMethodOverride;
        @Inject(optional = true)
        @Named(EVERREST_MAX_BUFFER_SIZE)
        private String maxBufferSize;
        @Inject(optional = true)
        @Named(EVERREST_NORMALIZE_URI)
        private String normalizeUrl;
        @Inject(optional = true)
        @Named(METHOD_INVOKER_DECORATOR_FACTORY)
        private String methodInvokerDecoratorFactory;
        @Inject(optional = true)
        @Named("org.everrest.websocket.readtimeout")
        private String websocketReadTimeout;

        @Override
        public EverrestConfiguration get() {
            final EverrestConfiguration configuration = new EverrestServletContextInitializer(servletContext).getConfiguration();
            setConfigurationPropertyIfNotNull(configuration, EVERREST_ASYNCHRONOUS, asynchronous);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_ASYNCHRONOUS_CACHE_SIZE, asynchronousCacheSize);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_ASYNCHRONOUS_JOB_TIMEOUT, asynchronousJobTimeout);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_ASYNCHRONOUS_POOL_SIZE, asynchronousPoolSize);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_ASYNCHRONOUS_QUEUE_SIZE, asynchronousQueueSize);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_ASYNCHRONOUS_SERVICE_PATH, asynchronousServicePath);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_CHECK_SECURITY, checkSecurity);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_HTTP_METHOD_OVERRIDE, httpMethodOverride);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_MAX_BUFFER_SIZE, maxBufferSize);
            setConfigurationPropertyIfNotNull(configuration, EVERREST_NORMALIZE_URI, normalizeUrl);
            setConfigurationPropertyIfNotNull(configuration, METHOD_INVOKER_DECORATOR_FACTORY, methodInvokerDecoratorFactory);
            setConfigurationPropertyIfNotNull(configuration, "org.everrest.websocket.readtimeout", websocketReadTimeout);
            return configuration;
        }

        private void setConfigurationPropertyIfNotNull(EverrestConfiguration everrestConfiguration, String name, String value) {
            if (value != null) {
                everrestConfiguration.setProperty(name, value);
            }
        }
    }

    @Override
    protected void configureServlets() {
        bind(EverrestConfiguration.class).toProvider(new EverrestConfigurationProvider(getServletContext()));
    }
}
