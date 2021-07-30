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
package org.everrest.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.everrest.core.impl.ConstructorDescriptorImpl;
import org.everrest.core.impl.FieldInjectorImpl;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.slf4j.LoggerFactory;

public class BaseObjectModel implements ObjectModel {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BaseObjectModel.class);

  private static final Comparator<ConstructorDescriptor>
      CONSTRUCTOR_COMPARATOR_BY_NUMBER_OF_PARAMETERS =
          new ConstructorComparatorByNumberOfParameters();

  /** Compare two ConstructorDescriptor in number parameters order. */
  private static class ConstructorComparatorByNumberOfParameters
      implements Comparator<ConstructorDescriptor> {

    @Override
    public int compare(
        ConstructorDescriptor constructorDescriptorOne,
        ConstructorDescriptor constructorDescriptorTwo) {
      int result =
          constructorDescriptorTwo.getParameters().size()
              - constructorDescriptorOne.getParameters().size();
      if (result == 0) {
        LOG.warn(
            "Two constructors with the same number of parameter found {} and {}",
            constructorDescriptorOne,
            constructorDescriptorTwo);
      }
      return result;
    }
  }

  protected final Class<?> clazz;
  protected final List<ConstructorDescriptor> constructors;
  protected final List<FieldInjector> fields;

  private ParameterResolverFactory parameterResolverFactory;

  /** Optional properties. */
  private MultivaluedMapImpl properties;

  public BaseObjectModel(Object instance) {
    this.clazz = instance.getClass();
    this.constructors = new ArrayList<>();
    this.fields = new ArrayList<>();
    parameterResolverFactory = new ParameterResolverFactory();
  }

  public BaseObjectModel(Class<?> clazz) {
    this.clazz = clazz;
    this.constructors = new ArrayList<>();
    this.fields = new ArrayList<>();
    parameterResolverFactory = new ParameterResolverFactory();

    processConstructors();
    sortConstructorByNumberOfParameters();
    processFields();
  }

  void setParameterResolverFactory(ParameterResolverFactory parameterResolverFactory) {
    this.parameterResolverFactory = parameterResolverFactory;
  }

  protected void processConstructors() {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      constructors.add(new ConstructorDescriptorImpl(constructor, parameterResolverFactory));
    }
    if (constructors.size() == 0) {
      throw new RuntimeException(
          String.format("Not found accepted constructors for provider class %s", clazz.getName()));
    }
  }

  private void sortConstructorByNumberOfParameters() {
    if (constructors.size() > 1) {
      Collections.sort(constructors, CONSTRUCTOR_COMPARATOR_BY_NUMBER_OF_PARAMETERS);
    }
  }

  protected void processFields() {
    for (java.lang.reflect.Field jField : clazz.getDeclaredFields()) {
      fields.add(new FieldInjectorImpl(jField, parameterResolverFactory));
    }
    Class<?> superclass = clazz.getSuperclass();
    while (superclass != null && superclass != Object.class) {
      for (java.lang.reflect.Field jField : superclass.getDeclaredFields()) {
        FieldInjector fieldInjector = new FieldInjectorImpl(jField, parameterResolverFactory);
        if (fieldInjector.getAnnotation() != null) {
          fields.add(fieldInjector);
        }
      }
      superclass = superclass.getSuperclass();
    }
  }

  @Override
  public Class<?> getObjectClass() {
    return clazz;
  }

  @Override
  public List<ConstructorDescriptor> getConstructorDescriptors() {
    return constructors;
  }

  @Override
  public List<FieldInjector> getFieldInjectors() {
    return fields;
  }

  @Override
  public MultivaluedMap<String, String> getProperties() {
    if (properties == null) {
      properties = new MultivaluedMapImpl();
    }
    return properties;
  }

  @Override
  public List<String> getProperty(String key) {
    if (properties != null) {
      return properties.get(key);
    }
    return null;
  }
}
