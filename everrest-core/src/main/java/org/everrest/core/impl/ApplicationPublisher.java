/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl;

import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.ResourceBinder;

import javax.ws.rs.core.Application;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ApplicationPublisher {
    private final ResourceBinder        resources;
    private final RestComponentResolver componentResolver;

    public ApplicationPublisher(ResourceBinder resources, ProviderBinder providers) {
        this(resources, new RestComponentResolver(resources, providers));
    }

    ApplicationPublisher(ResourceBinder resources, RestComponentResolver componentResolver) {
        this.resources = resources;
        this.componentResolver = componentResolver;
    }

    public void publish(Application application) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Set<Class<?>> appClasses = application.getClasses();
        if (appClasses != null) {
            classes.addAll(appClasses);
        }
        if (application instanceof EverrestApplication) {
            EverrestApplication everrest = (EverrestApplication)application;
            for (Map.Entry<String, Class<?>> e : everrest.getResourceClasses().entrySet()) {
                Class<?> clazz = e.getValue();
                addResource(e.getKey(), clazz);
                classes.remove(clazz);
            }
            for (Map.Entry<String, Object> e : everrest.getResourceSingletons().entrySet()) {
                addResource(e.getKey(), e.getValue());
            }
            for (ObjectFactory<? extends ObjectModel> factory : everrest.getFactories()) {
                addFactory(factory);
                classes.remove(factory.getObjectModel().getObjectClass());
            }
        }
        for (Class<?> clazz : classes) {
            addPerRequest(clazz);
        }
        Set<Object> singletons = application.getSingletons();
        if (singletons != null) {
            for (Object instance : singletons) {
                addSingleton(instance);
            }
        }
    }

    private void addResource(String uriPattern, Class<?> resourceClass) {
        resources.addResource(uriPattern, resourceClass, null);
    }

    private void addResource(String uriPattern, Object resource) {
        resources.addResource(uriPattern, resource, null);
    }

    private void addSingleton(Object instance) {
        componentResolver.addSingleton(instance);
    }

    @SuppressWarnings({"unchecked"})
    private void addPerRequest(Class clazz) {
        componentResolver.addPerRequest(clazz);
    }

    @SuppressWarnings({"unchecked"})
    private void addFactory(ObjectFactory factory) {
        componentResolver.addFactory(factory);
    }
}
