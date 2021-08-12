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
package org.everrest.core.resource;

import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Describe resource method. Resource method is method of resource class which has annotation {@link
 * jakarta.ws.rs.HttpMethod}, e.g. {@link jakarta.ws.rs.GET} and has not {@link jakarta.ws.rs.Path}
 * annotation.
 */
public interface ResourceMethodDescriptor extends GenericResourceMethod {
  /**
   * Get HTTP method name.
   *
   * @return HTTP method name
   */
  String getHttpMethod();

  /**
   * Get list of {@link MediaType} which current method consumes.
   *
   * @return list of media types
   */
  List<MediaType> consumes();

  /**
   * Get list of {@link MediaType} which current method produces.
   *
   * @return list of media types
   */
  List<MediaType> produces();
}
