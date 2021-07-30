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
package org.everrest.guice;

import javax.ws.rs.core.UriBuilder;
import org.everrest.core.impl.uri.UriBuilderImpl;

/**
 * Allows to use service proxy classes which are created by guice for interceptors.
 *
 * @author Max Shaposhnik
 */
public class GuiceUriBuilderImpl extends UriBuilderImpl {

  private static final String PROXY_MARKER = "$EnhancerByGuice$";

  public GuiceUriBuilderImpl() {
    super();
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public UriBuilder path(Class resource) {
    if (resource == null) {
      throw new IllegalArgumentException("Resource is null");
    }

    if (resource.getName().contains(PROXY_MARKER)) {
      return super.path(resource.getSuperclass());
    }
    return super.path(resource);
  }

  @Override
  public UriBuilder path(Class resource, String method) {
    if (resource == null) {
      throw new IllegalArgumentException("Resource is null");
    }

    if (resource.getName().contains(PROXY_MARKER)) {
      return super.path(resource.getSuperclass(), method);
    }
    return super.path(resource, method);
  }

  protected GuiceUriBuilderImpl(GuiceUriBuilderImpl cloned) {
    super(cloned);
  }

  @Override
  public UriBuilder clone() {
    return new GuiceUriBuilderImpl(this);
  }
}
