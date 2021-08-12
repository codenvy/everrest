/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
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

import jakarta.ws.rs.core.Application;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;

/**
 * Defines the JAX-RS components depending on EverrestConfiguration. It is uses as 'wrapper' for
 * custom instance of Application.
 *
 * <p>Usage:
 *
 * <p>
 *
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
  private final Set<Class<?>> classes;
  private final Set<Object> singletons;
  private final Map<String, Class<?>> resourceClasses;
  private final Map<String, Object> resourceSingletons;

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

  /** @see jakarta.ws.rs.core.Application#getClasses() */
  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> myClasses = new LinkedHashSet<>(this.classes);
    for (ObjectFactory<? extends ObjectModel> factory : getFactories()) {
      myClasses.add(factory.getObjectModel().getObjectClass());
    }
    return myClasses;
  }

  /** @see jakarta.ws.rs.core.Application#getSingletons() */
  @Override
  public Set<Object> getSingletons() {
    return singletons;
  }

  /**
   * Add components defined by <code>application</code> to this instance.
   *
   * @param application application
   * @see jakarta.ws.rs.core.Application
   */
  public void addApplication(Application application) {
    if (application != null) {
      Set<Object> appSingletons = application.getSingletons();
      if (appSingletons != null && !appSingletons.isEmpty()) {
        Set<Object> allSingletons =
            new LinkedHashSet<>(this.singletons.size() + appSingletons.size());
        allSingletons.addAll(appSingletons);
        allSingletons.addAll(this.singletons);
        this.singletons.clear();
        this.singletons.addAll(allSingletons);
      }
      Set<Class<?>> appClasses = application.getClasses();
      if (appClasses != null && !appClasses.isEmpty()) {
        Set<Class<?>> allClasses = new LinkedHashSet<>(this.classes.size() + appClasses.size());
        allClasses.addAll(appClasses);
        allClasses.addAll(this.classes);
        this.classes.clear();
        this.classes.addAll(allClasses);
      }
      if (application instanceof EverrestApplication) {
        EverrestApplication everrest = (EverrestApplication) application;
        Set<ObjectFactory<? extends ObjectModel>> appFactories = everrest.getFactories();
        if (!appFactories.isEmpty()) {
          Set<ObjectFactory<? extends ObjectModel>> allFactories =
              new LinkedHashSet<>(this.factories.size() + appFactories.size());
          allFactories.addAll(appFactories);
          allFactories.addAll(this.factories);
          this.factories.clear();
          this.factories.addAll(allFactories);
        }

        Map<String, Class<?>> appResourceClasses = everrest.getResourceClasses();
        if (!appResourceClasses.isEmpty()) {
          Map<String, Class<?>> allResourceClasses =
              new LinkedHashMap<>(this.resourceClasses.size() + appResourceClasses.size());
          allResourceClasses.putAll(appResourceClasses);
          allResourceClasses.putAll(this.resourceClasses);
          this.resourceClasses.clear();
          this.resourceClasses.putAll(allResourceClasses);
        }

        Map<String, Object> appResourceSingletons = everrest.getResourceSingletons();
        if (!appResourceSingletons.isEmpty()) {
          Map<String, Object> allResourceSingletons =
              new LinkedHashMap<>(this.resourceSingletons.size() + appResourceSingletons.size());
          allResourceSingletons.putAll(appResourceSingletons);
          allResourceSingletons.putAll(this.resourceSingletons);
          this.resourceSingletons.clear();
          this.resourceSingletons.putAll(allResourceSingletons);
        }
      }
    }
  }
}
