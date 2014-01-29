/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.core.servlet;

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;

import javax.ws.rs.core.Application;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines the JAX-RS components depending on EverrestConfiguration. It is uses as 'wrapper' for custom instance of
 * Application.
 * <p/>
 * Usage:
 * <p/>
 * <pre>
 * EverrestProcessor processor = ...
 * EverrestConfiguration config = ...
 * Application app = ...
 * EverrestApplication everrest = new EverrestApplication(config);
 * everrest.addApplication(app);
 * processor.addApplication(everrest);
 * </pre>
 *
 * @author andrew00x
 */
public final class EverrestApplication extends Application {
    private final Set<Class<?>> classes;
    private final Set<Object>   singletons;

    private Map<String, Class<?>> mappedPerRequestResources;
    private Map<String, Object> mappedSingletonResources;

    public EverrestApplication(EverrestConfiguration config) {
        classes = new LinkedHashSet<Class<?>>(1);
        singletons = new LinkedHashSet<Object>(3);
        if (config.isAsynchronousSupported()) {
            addResource(config.getAsynchronousServicePath(), AsynchronousJobService.class);
            singletons.add(new AsynchronousJobPool(config));
            singletons.add(new AsynchronousProcessListWriter());
        }
        if (config.isCheckSecurity()) {
            singletons.add(new SecurityConstraint());
        }
    }

    public void addResource(String uriPattern, Class<?> resourceClass) {
        if (mappedPerRequestResources == null) {
            mappedPerRequestResources = new LinkedHashMap<String, Class<?>>();
        }
        mappedPerRequestResources.put(uriPattern, resourceClass);
    }

    public void addResource(String uriPattern, Object resource) {
        if (mappedSingletonResources == null) {
            mappedSingletonResources = new LinkedHashMap<String, Object>();
        }
        mappedSingletonResources.put(uriPattern, resource);
    }

    public Map<String, Class<?>> getPerRequestResources() {
        return mappedPerRequestResources;
    }

    public Map<String, Object> getSingletonResources() {
        return mappedSingletonResources;
    }

    /** @see javax.ws.rs.core.Application#getClasses() */
    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    /** @see javax.ws.rs.core.Application#getSingletons() */
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    /**
     * Add components defined by <code>application</code> to this instance.
     *
     * @param application
     *         application
     * @see Application
     */
    public void addApplication(Application application) {
        if (application != null) {
            Set<Object> appSingletons = application.getSingletons();
            if (appSingletons != null && appSingletons.size() > 0) {
                Set<Object> tmp = new LinkedHashSet<Object>(getSingletons().size() + appSingletons.size());
                tmp.addAll(appSingletons);
                tmp.addAll(getSingletons());
                getSingletons().clear();
                getSingletons().addAll(tmp);
            }
            Set<Class<?>> appClasses = application.getClasses();
            if (appClasses != null && appClasses.size() > 0) {
                Set<Class<?>> tmp = new LinkedHashSet<Class<?>>(getClasses().size() + appClasses.size());
                tmp.addAll(appClasses);
                tmp.addAll(getClasses());
                getClasses().clear();
                getClasses().addAll(tmp);
            }
        }
    }
}
