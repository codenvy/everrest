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
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;

import static org.everrest.core.ExtHttpHeaders.FORWARDED_HOST;

/** @author andrew00x */
public final class ServletContainerRequest extends ContainerRequest {

    public static ServletContainerRequest create(final HttpServletRequest req) {
        final URL forwardedUrl = getForwardedUrl(req);
        String host;
        int port;
        if (forwardedUrl == null) {
            host = req.getServerName();
            port = req.getServerPort();
        } else {
            host = forwardedUrl.getHost();
            port = forwardedUrl.getPort();
            if (port < 0) {
                port = forwardedUrl.getDefaultPort();
            }
        }

        final String scheme = getScheme(req);
        final StringBuilder baseUriBuilder = uriBuilder(scheme, host, port);
        baseUriBuilder.append(req.getContextPath());
        baseUriBuilder.append(req.getServletPath());
        final URI baseUri = URI.create(baseUriBuilder.toString());

        final StringBuilder requestUriBuilder = uriBuilder(scheme, host, port);
        requestUriBuilder.append(req.getRequestURI());
        final String queryString = req.getQueryString();
        if (queryString != null) {
            requestUriBuilder.append('?');
            requestUriBuilder.append(queryString);
        }
        final URI requestUri = URI.create(requestUriBuilder.toString());
        return new ServletContainerRequest(getMethod(req), requestUri, baseUri, getEntityStream(req), getHeaders(req), getSecurityContext(req));
    }

    private ServletContainerRequest(String method, URI requestUri, URI baseUri, InputStream entityStream,
                                    MultivaluedMap<String, String> httpHeaders, SecurityContext securityContext) {
        super(method, requestUri, baseUri, entityStream, httpHeaders, securityContext);
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

    private static String getScheme(HttpServletRequest servletRequest) {
        return servletRequest.getScheme();
    }

    private static URL getForwardedUrl(HttpServletRequest servletRequest) {
        final String scheme = getScheme(servletRequest);
        final String forwardedHostAndPort = servletRequest.getHeader(FORWARDED_HOST);
        if (forwardedHostAndPort != null && !forwardedHostAndPort.isEmpty()) {
            final String host = getForwardedHost(forwardedHostAndPort);
            final int port = getForwardedPort(forwardedHostAndPort);
            try {
                // Use the standard URI to verify the host details
                return new URI(scheme, null, host, port, null, null, null).toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    public static String getForwardedHost(String forwardedHostAndPort) {
        final int colonIndex = forwardedHostAndPort.indexOf(':');
        if (colonIndex < 0) {
            return forwardedHostAndPort;
        }
        return forwardedHostAndPort.substring(0, colonIndex);
    }

    public static int getForwardedPort(String forwardedHostAndPort) {
        final int colonIndex = forwardedHostAndPort.indexOf(':');
        if (colonIndex >= 0) {
            try {
                return Integer.parseInt(forwardedHostAndPort.substring(colonIndex + 1, forwardedHostAndPort.length()));
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private static StringBuilder uriBuilder(String scheme, String host, int port) {
        final StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(scheme);
        uriBuilder.append("://");
        uriBuilder.append(host);
        if (!(port == 80 || (port == 443 && "https".equals(scheme)))) {
            uriBuilder.append(':');
            uriBuilder.append(port);
        }
        return uriBuilder;
    }

    /**
     * Get HTTP headers from {@link HttpServletRequest} .
     *
     * @param servletRequest
     *         {@link HttpServletRequest}
     * @return request headers
     */
    private static MultivaluedMap<String, String> getHeaders(HttpServletRequest servletRequest) {
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

    private static SecurityContext getSecurityContext(final HttpServletRequest servletRequest) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return servletRequest.getUserPrincipal();
            }

            @Override
            public boolean isUserInRole(String role) {
                return servletRequest.isUserInRole(role);
            }

            @Override
            public boolean isSecure() {
                return servletRequest.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return servletRequest.getAuthType();
            }
        };
    }
}
