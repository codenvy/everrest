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

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import org.everrest.core.impl.header.AcceptMediaType;

public interface GenericContainerRequest extends Request, SecurityContext, ExtHttpHeaders {
  /**
   * Get read-only list of cookie header.
   *
   * @return cookie as it get in request
   */
  List<String> getCookieHeaders();

  /**
   * Select the first media type, from a given list of media types, that is most acceptable
   * according to the requested acceptable media types.
   *
   * @param mediaTypes the list of media types
   * @return the most acceptable media type, or null if no media type was found to be acceptable
   */
  MediaType getAcceptableMediaType(List<MediaType> mediaTypes);

  /**
   * Get entity body represented by InputStream.
   *
   * @return entity data stream or null if no entity in request
   */
  InputStream getEntityStream();

  /** @return full request URI include query string and fragment */
  URI getRequestUri();

  /** @return common part of URI string for all services, e. g. servlet path */
  URI getBaseUri();

  /**
   * Set HTTP method.
   *
   * @param method HTTP method, i. e. GET, POST, etc
   */
  void setMethod(String method);

  /**
   * Set entity body InputStream.
   *
   * @param entityStream request message body as stream
   */
  void setEntityStream(InputStream entityStream);

  /**
   * Set new request URI and base URI.
   *
   * @param requestUri request URI
   * @param baseUri base URI
   */
  void setUris(URI requestUri, URI baseUri);

  /**
   * Set list of cookie headers.
   *
   * @param cookieHeaders list of cookies as sources string
   */
  void setCookieHeaders(List<String> cookieHeaders);

  /**
   * Set HTTP request headers.
   *
   * @param httpHeaders read-only case insensitive {@link MultivaluedMap}
   */
  void setRequestHeaders(MultivaluedMap<String, String> httpHeaders);

  /**
   * If accept header does not presents or its length is null then list with one element will be
   * returned. That one element is default media type, see {@link AcceptMediaType#DEFAULT}.
   */
  List<AcceptMediaType> getAcceptMediaTypeList();
}
