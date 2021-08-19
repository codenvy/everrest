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
package org.everrest.core.impl.resource;

import jakarta.ws.rs.Path;

/**
 * Describe the Path annotation, see {@link jakarta.ws.rs.Path}.
 *
 * @author andrew00x
 */
public class PathValue {
  public static String getPath(Path annotation) {
    return annotation == null ? null : annotation.value();
  }

  /** URI template, see {@link jakarta.ws.rs.Path#value()} . */
  private final String path;

  /** @param path URI template */
  public PathValue(String path) {
    this.path = path;
  }

  /** @return URI template string */
  public String getPath() {
    return path;
  }

  public String toString() {
    return "(" + path + ")";
  }
}
