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
package org.everrest.core.method;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Can create object by using String value. For each type of object should be created new
 * TypeProducer.
 *
 * @author andrew00x
 */
public interface TypeProducer {

  /**
   * @param param parameter name, parameter name should be getting from parameter annotation
   * @param values all value which can be used for construct object, it can be header parameters,
   *     path parameters, query parameters, etc
   * @param defaultValue default value which can be used if in value can't be found required value
   *     with name <i>param</i>
   * @return newly created object
   * @throws Exception if any errors occurs
   */
  Object createValue(String param, MultivaluedMap<String, String> values, String defaultValue)
      throws Exception;
}
