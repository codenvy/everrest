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

/**
 * Implementation of this interface should be able provide object instance dependent of component
 * lifecycle.
 *
 * @param <T> ObjectModel extensions
 * @author andrew00x
 * @version $Id$
 * @see ObjectModel
 */
public interface ObjectFactory<T extends ObjectModel> {
  /**
   * Create object instance. ApplicationContext can be used for getting required parameters for
   * object constructors or fields.
   *
   * @param context ApplicationContext
   * @return object instance
   */
  Object getInstance(ApplicationContext context);

  /**
   * @return any extension of {@link ObjectModel}. That must allows create object instance and
   *     initialize object's fields for per-request resources
   */
  T getObjectModel();
}
