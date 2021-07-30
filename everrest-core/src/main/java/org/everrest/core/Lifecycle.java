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
 * Interface provides methods lifecycle control.
 *
 * @author andrew00x
 */
public interface Lifecycle {
  /**
   * Star Lifecycle. If this interface implemented by container it must notify all its components.
   * If Lifecycle already started repeated calling of this method has no effect.
   */
  void start();

  /**
   * Stop Lifecycle. If this interface implemented by container it must notify all its components.
   * If Lifecycle already stopped repeated calling of this method has no effect.
   */
  void stop();
}
