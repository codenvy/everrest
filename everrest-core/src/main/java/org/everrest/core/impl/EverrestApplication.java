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

import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;

import javax.ws.rs.core.Application;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines the JAX-RS components depending on EverrestConfiguration. It is uses as 'wrapper' for custom instance of Application.
 * <p/>
 * Usage:
 * <p/>
 * <pre>
 * EverrestProcessor processor = ...
 * Application app = ...
 * EverrestApplication everrest = new EverrestApplication();
 * EverrestConfiguration config = ...
 * ...
 * everrest.addApplication(app);
 * processor.addApplication(everrest);
 * </pre>
 *
 * @author andrew00x
 */
public class EverrestApplication extends Application {
    private final Set<ObjectFactory<? extends ObjectModel>> factories;
    private final Set<Class<?>>                             classes;
    private final Set<Object>                               singletons;
    private final Map<String, Class<?>>                     resourceClasses;
    private final Map<String, Object>                       resourceSingletons;

    public EverrestApplication() {
        classes = new LinkedHashSet<>();
        singletons = new LinkedHashSet<>();
        factories = new LinkedHashSet<>();
        resourceClasses = new LinkedHashMap<>();
        resourceSingletons = new LinkedHashMap<>();
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addSingleton(Object singleton) {
        singletons.add(singleton);
    }

    public void addFactory(ObjectFactory<? extends ObjectModel> factory) {
        factories.add(factory);
    }

    public void addResource(String uriPattern, Class<?> resourceClass) {
        resourceClasses.put(uriPattern, resourceClass);
    }

    public void addResource(String uriPattern, Object resource) {
        resourceSingletons.put(uriPattern, resource);
    }

    public Map<String, Class<?>> getResourceClasses() {
        return resourceClasses;
    }

    public Map<String, Object> getResourceSingletons() {
        return resourceSingletons;
    }

    public Set<ObjectFactory<? extends ObjectModel>> getFactories() {
        return factories;
    }

    /** @see javax.ws.rs.core.Application#getClasses() */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> myClasses = new LinkedHashSet<>(this.classes);
        for (ObjectFactory<? extends ObjectModel> factory : getFactories()) {
            myClasses.add(factory.getObjectModel().getObjectClass());
        }
        return myClasses;
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
     * @see javax.ws.rs.core.Application
     */
    public void addApplication(Application application) {
        if (application != null) {
            Set<Object> appSingletons = application.getSingletons();
            if (appSingletons != null && appSingletons.size() > 0) {
                Set<Object> tmp = new LinkedHashSet<>(this.singletons.size() + appSingletons.size());
                tmp.addAll(appSingletons);
                tmp.addAll(this.singletons);
                this.singletons.clear();
                this.singletons.addAll(tmp);
            }
            Set<Class<?>> appClasses = application.getClasses();
            if (appClasses != null && appClasses.size() > 0) {
                Set<Class<?>> tmp = new LinkedHashSet<>(this.classes.size() + appClasses.size());
                tmp.addAll(appClasses);
                tmp.addAll(this.classes);
                this.classes.clear();
                this.classes.addAll(tmp);
            }
            if (application instanceof EverrestApplication) {
                EverrestApplication everrest = (EverrestApplication)application;
                Set<ObjectFactory<? extends ObjectModel>> appFactories = everrest.getFactories();
                Set<ObjectFactory<? extends ObjectModel>> tmp1 = new LinkedHashSet<>(this.factories.size() + appFactories.size());
                tmp1.addAll(appFactories);
                tmp1.addAll(this.factories);
                this.factories.clear();
                this.factories.addAll(tmp1);

                Map<String, Class<?>> appResourceClasses = everrest.getResourceClasses();
                Map<String, Class<?>> tmp2 = new LinkedHashMap<>(this.resourceClasses.size() + appResourceClasses.size());
                tmp2.putAll(appResourceClasses);
                tmp2.putAll(this.resourceClasses);
                this.resourceClasses.clear();
                this.resourceClasses.putAll(tmp2);

                Map<String, Object> appResourceSingletons = everrest.getResourceSingletons();
                Map<String, Object> tmp3 = new LinkedHashMap<>(this.resourceSingletons.size() + appResourceSingletons.size());
                tmp3.putAll(appResourceSingletons);
                tmp3.putAll(this.resourceSingletons);
                this.resourceSingletons.clear();
                this.resourceSingletons.putAll(tmp3);
            }
        }
    }
}

