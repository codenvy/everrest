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

import jakarta.ws.rs.Path;
import org.everrest.core.BaseObjectModel;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.uri.UriPattern;

/** @author andrew00x */
public class FilterDescriptorImpl extends BaseObjectModel implements FilterDescriptor {
  public static final String DEFAULT_PATH = "/{path:.*}";
  /** @see PathValue */
  private final PathValue path;

  /** @see UriPattern */
  private final UriPattern uriPattern;

  /** @param filterClass filter class */
  public FilterDescriptorImpl(Class<?> filterClass) {
    super(filterClass);
    final String p = PathValue.getPath(filterClass.getAnnotation(Path.class));
    if (p != null) {
      this.path = new PathValue(p);
      this.uriPattern = new UriPattern(p);
    } else {
      this.path = new PathValue(DEFAULT_PATH);
      this.uriPattern = new UriPattern(DEFAULT_PATH);
    }
  }

  /** @param filter filter */
  public FilterDescriptorImpl(Object filter) {
    super(filter);
    final String p = PathValue.getPath(filter.getClass().getAnnotation(Path.class));
    if (p != null) {
      this.path = new PathValue(p);
      this.uriPattern = new UriPattern(p);
    } else {
      this.path = new PathValue(DEFAULT_PATH);
      this.uriPattern = new UriPattern(DEFAULT_PATH);
    }
  }

  @Override
  public PathValue getPathValue() {
    return path;
  }

  @Override
  public UriPattern getUriPattern() {
    return uriPattern;
  }

  @Override
  public String toString() {
    return "[ FilterDescriptorImpl: "
        + "path: "
        + getPathValue()
        + "; filter class: "
        + getObjectClass()
        + "; "
        + getConstructorDescriptors()
        + "; "
        + getFieldInjectors()
        + " ]";
  }
}
