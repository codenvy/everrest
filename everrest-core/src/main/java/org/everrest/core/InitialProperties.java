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

import java.util.Map;

/**
 * Container for properties, that may be injected in resource by &#64Context annotation.
 *
 * @author andrew00x
 */
public interface InitialProperties {

  /** @return all properties. */
  Map<String, String> getProperties();

  /**
   * Get property.
   *
   * @param name property name
   * @return value of property with specified name or null
   */
  String getProperty(String name);

  /**
   * Set property.
   *
   * @param name property name
   * @param value property value
   */
  void setProperty(String name, String value);
}
