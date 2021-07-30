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

import com.google.common.base.MoreObjects;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.everrest.core.Parameter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.uri.UriPattern;

public class SubResourceMethodDescriptorImpl extends ResourceMethodDescriptorImpl
    implements SubResourceMethodDescriptor {

  /** See {@link PathValue}. */
  private final PathValue path;

  /** See {@link UriPattern}. */
  private final UriPattern uriPattern;

  /**
   * Constructs new instance of {@link SubResourceMethodDescriptor}.
   *
   * @param path See {@link PathValue}
   * @param method See {@link Method}
   * @param httpMethod HTTP request method designator
   * @param parameters list of method parameters. See {@link Parameter}
   * @param parentResource parent resource for this method
   * @param consumes list of media types which this method can consume
   * @param produces list of media types which this method can produce
   * @param additional set of additional (not JAX-RS annotations)
   */
  SubResourceMethodDescriptorImpl(
      PathValue path,
      Method method,
      String httpMethod,
      List<Parameter> parameters,
      ResourceDescriptor parentResource,
      List<MediaType> consumes,
      List<MediaType> produces,
      Annotation[] additional) {
    super(method, httpMethod, parameters, parentResource, consumes, produces, additional);
    this.path = path;
    this.uriPattern = new UriPattern(path.getPath());
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
    return MoreObjects.toStringHelper(this)
        .add("resource", getParentResource().getObjectClass())
        .add("path", path)
        .add("HTTP method", getHttpMethod())
        .add("produced media types", produces())
        .add("consumed media types", consumes())
        .add("returned type", getResponseType())
        .toString();
  }
}
