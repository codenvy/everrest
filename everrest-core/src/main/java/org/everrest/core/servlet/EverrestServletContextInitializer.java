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
package org.everrest.core.servlet;

import org.everrest.core.Filter;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.util.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashSet;
import java.util.Set;

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

/** @author andrew00x */
public class EverrestServletContextInitializer {
    public static final String EVERREST_SCAN_COMPONENTS = "org.everrest.scan.components";

    public static final String EVERREST_SCAN_SKIP_PACKAGES = "org.everrest.scan.skip.packages";

    public static final String JAXRS_APPLICATION = "javax.ws.rs.Application";

    private static final Logger LOG = Logger.getLogger(EverrestServletContextInitializer.class);

    protected final ServletContext ctx;

    public EverrestServletContextInitializer(ServletContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Try get application's FQN from context-param javax.ws.rs.Application and instantiate it. If such parameter is not
     * specified then scan web application's folders WEB-INF/classes and WEB-INF/lib for classes which contains JAX-RS
     * annotations. Interesting for three annotations {@link Path}, {@link Provider} and {@link Filter} .
     *
     * @return instance of javax.ws.rs.core.Application
     */
    public Application getApplication() {
        Application application = null;
        String applicationFQN = getParameter(JAXRS_APPLICATION);
        boolean scan = getBoolean(EVERREST_SCAN_COMPONENTS, false);
        if (applicationFQN != null) {
            if (scan) {
                String msg = "Scan of JAX-RS components is disabled cause to specified 'javax.ws.rs.Application'.";
                LOG.warn(msg);
            }
            try {
                Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(applicationFQN);
                application = (Application)cl.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (scan) {
            application = new Application() {
                @Override
                public Set<Class<?>> getClasses() {
                    return new LinkedHashSet<>(ComponentFinder.findComponents());
                }
            };
        }
        return application;
    }

    public EverrestConfiguration getConfiguration() {
        EverrestConfiguration config = new EverrestConfiguration();
        config.setProperty(EVERREST_HTTP_METHOD_OVERRIDE, getParameter(EVERREST_HTTP_METHOD_OVERRIDE));
        config.setProperty(EVERREST_NORMALIZE_URI, getParameter(EVERREST_NORMALIZE_URI));
        config.setProperty(EVERREST_CHECK_SECURITY, getParameter(EVERREST_CHECK_SECURITY));
        config.setProperty(EVERREST_ASYNCHRONOUS, getParameter(EVERREST_ASYNCHRONOUS));
        config.setProperty(EVERREST_ASYNCHRONOUS_POOL_SIZE, getParameter(EVERREST_ASYNCHRONOUS_POOL_SIZE));
        config.setProperty(EVERREST_ASYNCHRONOUS_SERVICE_PATH, getParameter(EVERREST_ASYNCHRONOUS_SERVICE_PATH));
        config.setProperty(EVERREST_ASYNCHRONOUS_QUEUE_SIZE, getParameter(EVERREST_ASYNCHRONOUS_QUEUE_SIZE));
        config.setProperty(EVERREST_ASYNCHRONOUS_CACHE_SIZE, getParameter(EVERREST_ASYNCHRONOUS_CACHE_SIZE));
        config.setProperty(EVERREST_ASYNCHRONOUS_JOB_TIMEOUT, getParameter(EVERREST_ASYNCHRONOUS_JOB_TIMEOUT));
        config.setProperty(EVERREST_MAX_BUFFER_SIZE, getParameter(EVERREST_MAX_BUFFER_SIZE));
        config.setProperty(METHOD_INVOKER_DECORATOR_FACTORY, getParameter(METHOD_INVOKER_DECORATOR_FACTORY));
        return config;
    }

    /**
     * Get parameter with specified name from servlet context initial parameters.
     *
     * @param name
     *         parameter name
     * @return value of parameter with specified name
     */
    protected String getParameter(String name) {
        String str = ctx.getInitParameter(name);
        if (str != null) {
            return str.trim();
        }
        return null;
    }

    protected String getParameter(String name, String def) {
        String value = getParameter(name);
        if (value == null) {
            return def;
        }
        return value;
    }

    protected boolean getBoolean(String name, boolean def) {
        String str = getParameter(name);
        if (str != null) {
            return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str) || "1".equals(str);
        }
        return def;
    }

    protected Double getNumber(String name, double def) {
        String str = getParameter(name);
        if (str != null) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }
}
