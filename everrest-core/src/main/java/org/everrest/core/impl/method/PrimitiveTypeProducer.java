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
package org.everrest.core.impl.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import org.everrest.core.util.ReflectionUtils;

/**
 * Constructs a primitive type from string value.
 *
 * @author andrew00x
 */
public final class PrimitiveTypeProducer extends BaseTypeProducer {

  /**
   * Primitive types map, this map contains all primitive java types except char because {@link
   * Character} has not static method valueOf with String parameter.
   */
  static final Map<String, Class<?>> PRIMITIVE_TYPES_MAP;

  /**
   * Default values for primitive types. This value will be used if not found required parameter in
   * request and default value {@link javax.ws.rs.DefaultValue} is null.
   */
  private static final Map<String, Object> PRIMITIVE_TYPE_DEFAULTS;

  static {
    Map<String, Class<?>> primitiveTypes = new HashMap<>(7);
    primitiveTypes.put("boolean", Boolean.class);
    primitiveTypes.put("byte", Byte.class);
    primitiveTypes.put("short", Short.class);
    primitiveTypes.put("int", Integer.class);
    primitiveTypes.put("long", Long.class);
    primitiveTypes.put("float", Float.class);
    primitiveTypes.put("double", Double.class);
    PRIMITIVE_TYPES_MAP = Collections.unmodifiableMap(primitiveTypes);
  }

  static {
    Map<String, Object> primitiveTypesDefaultValues = new HashMap<>(7);
    primitiveTypesDefaultValues.put("boolean", false);
    primitiveTypesDefaultValues.put("byte", (byte) 0);
    primitiveTypesDefaultValues.put("short", (short) 0);
    primitiveTypesDefaultValues.put("int", 0);
    primitiveTypesDefaultValues.put("long", 0L);
    primitiveTypesDefaultValues.put("float", 0.0f);
    primitiveTypesDefaultValues.put("float", 0.0f);
    primitiveTypesDefaultValues.put("double", 0.0d);
    PRIMITIVE_TYPE_DEFAULTS = Collections.unmodifiableMap(primitiveTypesDefaultValues);
  }

  /** Class of object which will be created. */
  private Class<?> clazz;

  /** This will be used if defaultValue is {@code null}. */
  private Object defaultDefaultValue;

  /**
   * Construct PrimitiveTypeProducer.
   *
   * @param clazz class of object
   */
  PrimitiveTypeProducer(Class<?> clazz) {
    this.clazz = clazz;
    this.defaultDefaultValue = PRIMITIVE_TYPE_DEFAULTS.get(clazz.getName());
  }

  @Override
  protected Object createValue(String value) throws Exception {
    Class<?> c = PRIMITIVE_TYPES_MAP.get(clazz.getName());
    Method stringValueOfMethod = ReflectionUtils.getStringValueOfMethod(c);
    return stringValueOfMethod.invoke(null, value);
  }

  @Override
  public Object createValue(
      String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception {
    String value = values.getFirst(param);

    if (value != null) {
      return createValue(value);
    } else if (defaultValue != null) {
      return createValue(defaultValue);
    }

    return this.defaultDefaultValue;
  }
}
