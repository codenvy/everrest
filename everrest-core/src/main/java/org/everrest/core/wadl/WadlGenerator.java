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
package org.everrest.core.wadl;

import jakarta.ws.rs.core.MediaType;
import org.everrest.core.Parameter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.wadl.research.Application;
import org.everrest.core.wadl.research.Param;
import org.everrest.core.wadl.research.RepresentationType;
import org.everrest.core.wadl.research.Resources;

/**
 * A WadGenerator creates structure that can be reflected to WADL representation.
 *
 * @author andrew00x
 */
public interface WadlGenerator {

  /** @return {@link Application} instance, it is root element in WADL */
  Application createApplication();

  /**
   * @return {@link Resources} instance. Element <i>resources</i> in WADL document is container for
   *     the descriptions of resources provided by application
   */
  Resources createResources();

  /**
   * @param rd See {@link org.everrest.core.resource.ResourceDescriptor}
   * @return {@link org.everrest.core.wadl.research.Resource.Resource} describes application
   *     resource, each resource identified by a URI
   */
  org.everrest.core.wadl.research.Resource createResource(ResourceDescriptor rd);

  /**
   * @param path resource relative path
   * @return {@link org.everrest.core.wadl.research.Resource.Resource} describes application
   *     resource, each resource identified by a URI
   */
  org.everrest.core.wadl.research.Resource createResource(String path);

  /**
   * @param md See {@link ResourceMethodDescriptor}
   * @return {@link org.everrest.core.rest.wadl.research.Method} describes the input to and output
   *     from an HTTP protocol method they may be applied to a resource
   */
  org.everrest.core.wadl.research.Method createMethod(ResourceMethodDescriptor md);

  /**
   * @return {@link org.everrest.core.wadl.research.Request} describes the input to be included when
   *     applying an HTTP method to a resource
   * @see {@link org.everrest.core.wadl.research.Method}
   */
  org.everrest.core.wadl.research.Request createRequest();

  /**
   * @return {@link org.everrest.core.wadl.research.Response} describes the output that result from
   *     performing an HTTP method on a resource
   * @see {@link org.everrest.core.wadl.research.Method}
   */
  org.everrest.core.wadl.research.Response createResponse();

  /**
   * @param mediaType one of media type that resource can consume
   * @return {@link RepresentationType} describes a representation of resource's state
   */
  RepresentationType createRequestRepresentation(MediaType mediaType);

  /**
   * @param mediaType one of media type that resource can produce
   * @return {@link RepresentationType} describes a representation of resource's state
   */
  RepresentationType createResponseRepresentation(MediaType mediaType);

  /**
   * @param methodParameter See {@link Parameter}
   * @return {@link Param} describes a parameterized component of its parent element resource,
   *     request, response
   * @see org.everrest.core.wadl.research.Resource
   * @see org.everrest.core.wadl.research.Request
   * @see org.everrest.core.wadl.research.Response
   */
  Param createParam(Parameter methodParameter);
}
