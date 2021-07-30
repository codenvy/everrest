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
 * Process the original {@link GenericContainerResponse} before pass it for serialization to
 * environment, e. g. servlet container. NOTE this filter must not be used directly, it is part of
 * REST framework.
 *
 * @author andrew00x
 */
public interface ResponseFilter {
  /**
   * Can modify original response.
   *
   * @param response the response from resource
   */
  void doFilter(GenericContainerResponse response);
}
