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

import com.google.common.base.MoreObjects;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.everrest.core.Parameter;

/** Describes constructor's parameter. */
public class ConstructorParameter implements Parameter {
  /** All annotations including JAX-RS annotation. */
  private final Annotation[] additional;

  /** One of JAX-RS annotations. */
  private final Annotation annotation;

  /** Parameter type. See {@link java.lang.reflect.Constructor#getGenericParameterTypes()} . */
  private final Type genericType;

  /** Parameter class. See {@link java.lang.reflect.Constructor#getParameterTypes()} */
  private final Class<?> clazz;

  /**
   * Default value for this parameter, default value can be used if there is not found required
   * parameter in request. See {@link jakarta.ws.rs.DefaultValue}.
   */
  private final String defaultValue;

  /** See {@link jakarta.ws.rs.Encoded}. */
  private final boolean encoded;

  /**
   * Constructs new instance of MethodParameter.
   *
   * @param annotation see {@link #annotation}
   * @param additional see {@link #additional}
   * @param clazz parameter class
   * @param genericType generic parameter type
   * @param defaultValue default value for parameter. See {@link jakarta.ws.rs.DefaultValue}.
   * @param encoded true if parameter must not be decoded false otherwise
   */
  public ConstructorParameter(
      Annotation annotation,
      Annotation[] additional,
      Class<?> clazz,
      Type genericType,
      String defaultValue,
      boolean encoded) {
    this.annotation = annotation;
    this.additional = additional;
    this.clazz = clazz;
    this.genericType = genericType;
    this.defaultValue = defaultValue;
    this.encoded = encoded;
  }

  @Override
  public Annotation getAnnotation() {
    return annotation;
  }

  @Override
  public Annotation[] getAnnotations() {
    return additional;
  }

  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
  public Class<?> getParameterClass() {
    return clazz;
  }

  @Override
  public Type getGenericType() {
    return genericType;
  }

  @Override
  public boolean isEncoded() {
    return encoded;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("annotation", annotation)
        .add("type", clazz)
        .add("genericType", genericType)
        .add("defaultValue", defaultValue)
        .add("isEncoded", encoded)
        .omitNullValues()
        .toString();
  }
}
