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
import org.everrest.core.resource.ResourceMethodDescriptor;

public class ResourceMethodDescriptorImpl implements ResourceMethodDescriptor {
  /** This method will be invoked. */
  private final Method method;

  /** HTTP request method designator. */
  private final String httpMethod;

  /** List of method's parameters. See {@link Parameter} . */
  private final List<Parameter> parameters;

  /** Parent resource for this method resource, in other words class which contains this method. */
  private final ResourceDescriptor parentResource;

  /** List of media types which this method can consume. See {@link javax.ws.rs.Consumes} . */
  private final List<MediaType> consumes;

  /** List of media types which this method can produce. See {@link javax.ws.rs.Produces} . */
  private final List<MediaType> produces;

  private final Annotation[] additional;

  /**
   * Constructs new instance of {@link ResourceMethodDescriptor}.
   *
   * @param method See {@link Method}
   * @param httpMethod HTTP request method designator
   * @param parameters list of method parameters. See {@link Parameter}
   * @param parentResource parent resource for this method
   * @param consumes list of media types which this method can consume
   * @param produces list of media types which this method can produce
   * @param additional set of additional (not JAX-RS annotations)
   */
  ResourceMethodDescriptorImpl(
      Method method,
      String httpMethod,
      List<Parameter> parameters,
      ResourceDescriptor parentResource,
      List<MediaType> consumes,
      List<MediaType> produces,
      Annotation[] additional) {
    this.method = method;
    this.httpMethod = httpMethod;
    this.parameters = parameters;
    this.parentResource = parentResource;
    this.consumes = consumes;
    this.produces = produces;
    this.additional = additional;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public List<Parameter> getMethodParameters() {
    return parameters;
  }

  @Override
  public ResourceDescriptor getParentResource() {
    return parentResource;
  }

  @Override
  public List<MediaType> consumes() {
    return consumes;
  }

  @Override
  public String getHttpMethod() {
    return httpMethod;
  }

  @Override
  public List<MediaType> produces() {
    return produces;
  }

  @Override
  public Class<?> getResponseType() {
    return getMethod().getReturnType();
  }

  @Override
  public Annotation[] getAnnotations() {
    return additional;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("resource", parentResource.getObjectClass())
        .add("HTTP method", httpMethod)
        .add("produced media types", produces)
        .add("consumed media types", consumes)
        .add("returned type", getResponseType())
        .toString();
  }
}
