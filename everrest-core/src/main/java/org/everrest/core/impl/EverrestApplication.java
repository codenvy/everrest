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
        classes = new LinkedHashSet<>(1);
        singletons = new LinkedHashSet<>(3);
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
            mappedPerRequestResources = new LinkedHashMap<>();
        }
        mappedPerRequestResources.put(uriPattern, resourceClass);
    }

    public void addResource(String uriPattern, Object resource) {
        if (mappedSingletonResources == null) {
            mappedSingletonResources = new LinkedHashMap<>();
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
                Set<Object> tmp = new LinkedHashSet<>(getSingletons().size() + appSingletons.size());
                tmp.addAll(appSingletons);
                tmp.addAll(getSingletons());
                getSingletons().clear();
                getSingletons().addAll(tmp);
            }
            Set<Class<?>> appClasses = application.getClasses();
            if (appClasses != null && appClasses.size() > 0) {
                Set<Class<?>> tmp = new LinkedHashSet<>(getClasses().size() + appClasses.size());
                tmp.addAll(appClasses);
                tmp.addAll(getClasses());
                getClasses().clear();
                getClasses().addAll(tmp);
            }
        }
    }
}
