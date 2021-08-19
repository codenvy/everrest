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

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * Abstract description of object.
 *
 * @author andrew00x
 */
public interface ObjectModel {

  /**
   * @return collections constructor, MAY return empty collection or null if object is singleton.
   *     There is no setter for this to add new ConstructorInjector use <code>
   *     ObjectModel.getConstructorDescriptors().add(ConstructorInjector)</code>
   */
  List<ConstructorDescriptor> getConstructorDescriptors();

  /**
   * @return collections of object fields, MAY return empty collection or null if object is
   *     singleton. There is no setter for this to add new ConstructorInjector use <code>
   *     ObjectModel.getFieldInjectors().add(FieldInjector)</code>
   */
  List<FieldInjector> getFieldInjectors();

  /** @return {@link Class} of object */
  Class<?> getObjectClass();

  /**
   * @param key property name
   * @return property by key
   * @see #getProperties()
   */
  List<String> getProperty(String key);

  /**
   * Optional attributes.
   *
   * @return all properties. If there is no any optional attributes then empty map returned never
   *     <code>null</code>
   */
  MultivaluedMap<String, String> getProperties();
}
