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
package org.everrest.core.servlet;

import org.everrest.core.Filter;
import org.everrest.core.impl.EverrestConfiguration;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** @author andrew00x */
public class EverrestServletContextInitializer {
    public static final String EVERREST_SCAN_COMPONENTS = "org.everrest.scan.components";

    public static final String EVERREST_SCAN_SKIP_PACKAGES = "org.everrest.scan.skip.packages";

    public static final String JAXRS_APPLICATION = "javax.ws.rs.Application";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EverrestServletContextInitializer.class);

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
        for (String parameterName : getParameterNames()) {
            config.setProperty(parameterName, getParameter(parameterName));
        }
        return config;
    }

    protected List<String> getParameterNames() {
        return Collections.list(ctx.getInitParameterNames());
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
