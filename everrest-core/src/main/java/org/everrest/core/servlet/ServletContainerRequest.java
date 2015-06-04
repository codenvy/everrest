/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.servlet;

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Enumeration;

/** @author andrew00x */
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
        StringBuilder uri = new StringBuilder();
        appendSchemaHostPort(uri, servletRequest);
        uri.append(servletRequest.getRequestURI());
        String queryString = servletRequest.getQueryString();
        if (queryString != null) {
            uri.append('?');
            uri.append(queryString);
        }
        //System.out.println("REQ URI :  " + uri);
        return URI.create(uri.toString());
    }

    /**
     * Constructs base request URI from {@link HttpServletRequest} .
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return newly created URI
     */
    private static URI getBaseUri(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder();
        appendSchemaHostPort(uri, servletRequest);
        uri.append(servletRequest.getContextPath());
        uri.append(servletRequest.getServletPath());
        //System.out.println("BASE URI : " + uri);
        return URI.create(uri.toString());
    }
    
    /**
     * Get the effective host information for the request. This takes forwarding
     * headers into account, according to the standard: http://tools.ietf.org/html/rfc7239
     * 
     * @param uri
     * @param servletRequest
     */
    private static void appendSchemaHostPort(StringBuilder uri, HttpServletRequest servletRequest) {
        // Pick the protocol
        // TODO leave this to the RemoteIpValve in server.xml
        String scheme;
        // scheme = servletRequest.getHeader(ExtHttpHeaders.FORWARDED_PROTO);
        // if (scheme == null) {
        scheme = servletRequest.getScheme();
        // }
        // If the host is forwarded, validate it before use
        URL forwardedHostUrl = null;
        String fwdHost = servletRequest.getHeader(ExtHttpHeaders.FORWARDED_HOST);
        if (fwdHost != null) {
            // According to the standard, a host is defined this way:
            // Host = uri-host [ ":" port ]
            String[] fwdHostParts = fwdHost.split(":");
            if (fwdHostParts.length <= 2) {
                try {
                    String fwdHostName = fwdHostParts[0];
                    int fwdPort = -1;
                    // If a port is specified for the forwarded host, make sure
                    // it is non-negative
                    boolean portOk = true;
                    if (fwdHostParts.length == 2) {
                        fwdPort = Integer.parseInt(fwdHostParts[1]);
                        portOk = (fwdPort >= 0);
                    }
                    // Use the standard URI to verify the host details
                    if (portOk) {
                        forwardedHostUrl = new URI(scheme, null, fwdHostName, fwdPort, null, null, null).toURL();
                    }
                } catch (NumberFormatException | URISyntaxException | MalformedURLException e) {
                }
            }
        }
        // Forwarded?
        String hostName = null;
        int port = -1;
        // The host information
        if (forwardedHostUrl == null) {
            hostName = servletRequest.getServerName();
            port = servletRequest.getServerPort();
        } else {
            hostName = forwardedHostUrl.getHost();
            port = forwardedHostUrl.getPort();
            if (port < 0) {
                port = forwardedHostUrl.getDefaultPort();
            }
        }
        // Build the final result
        uri.append(scheme);
        uri.append("://");
        uri.append(hostName);
        if (!(port == 80 || (port == 443 && "https".equals(scheme)))) {
            uri.append(':');
            uri.append(port);
        }
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
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> e = servletRequest.getHeaders(name);
            while (e.hasMoreElements()) {
                h.add(name, e.nextElement());
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
