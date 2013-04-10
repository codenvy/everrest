/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.servlet;

import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Enumeration;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ServletContainerRequest.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public final class ServletContainerRequest extends ContainerRequest {
    /**
     * @param req
     *         HttpServletRequest
     */
    public ServletContainerRequest(final HttpServletRequest req) {
        super(getMethod(req), getRequestUri(req), getBaseUri(req), getEntityStream(req), getHeader(req),
              new SecurityContext() {
                  @Override
                  public Principal getUserPrincipal() {
                      return req.getUserPrincipal();
                  }

                  @Override
                  public boolean isUserInRole(String role) {
                      return req.isUserInRole(role);
                  }

                  @Override
                  public boolean isSecure() {
                      return req.isSecure();
                  }

                  @Override
                  public String getAuthenticationScheme() {
                      return req.getAuthType();
                  }
              }
             );
    }

    /**
     * Extract HTTP method name from servlet request.
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return HTTP method name
     * @see HttpServletRequest#getMethod()
     */
    private static String getMethod(HttpServletRequest servletRequest) {
        return servletRequest.getMethod();
    }

    /**
     * Constructs full request URI from {@link HttpServletRequest}, URI includes
     * query string and fragment.
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return newly created URI
     */
    private static URI getRequestUri(HttpServletRequest servletRequest) {
        // servletRequest.getQueryString() return part of URI after '?', so it return fragment component also
        UriBuilder baseBuilder = UriBuilder.fromUri(getBaseUri(servletRequest));
        return baseBuilder.replacePath(servletRequest.getRequestURI()).replaceQuery(servletRequest.getQueryString())
                          .build();
    }

    /**
     * Constructs base request URI from {@link HttpServletRequest} .
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return newly created URI
     */
    private static URI getBaseUri(HttpServletRequest servletRequest) {
        String server = servletRequest.getScheme() + "://" + servletRequest.getServerName();
        UriBuilder builder = UriBuilder.fromUri(server);
        int port = servletRequest.getServerPort();
        if (port != 80) {
            builder.port(port);
        }
        builder.path(servletRequest.getContextPath() + servletRequest.getServletPath());
        return builder.build();
    }

    /**
     * Get HTTP headers from {@link HttpServletRequest} .
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return request headers
     */
    private static MultivaluedMap<String, String> getHeader(HttpServletRequest servletRequest) {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        Enumeration<?> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String)headerNames.nextElement();
            Enumeration<?> e = servletRequest.getHeaders(name);
            while (e.hasMoreElements()) {
                h.add(name, (String)e.nextElement());
            }
        }
        return new InputHeadersMap(h);
    }

    /**
     * Get input stream from {@link HttpServletRequest} .
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return request stream or null
     */
    private static InputStream getEntityStream(HttpServletRequest servletRequest) {
        try {
            return servletRequest.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
