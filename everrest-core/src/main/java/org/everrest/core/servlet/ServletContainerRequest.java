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
import org.everrest.core.util.Logger;

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

/** @author andrew00x */
public final class ServletContainerRequest extends ContainerRequest {

    private static final Logger LOG = Logger.getLogger(ServletContainerRequest.class);

    public static ServletContainerRequest create(final HttpServletRequest req) {
        // If the URL is forwarded, obtain the forwarding information
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
            LOG.debug("Assuming forwarded URL: {}", forwardedUrl);
        }

        // The common URI prefix for both baseUri and requestUri
        final StringBuilder commonUriBuilder = new StringBuilder();
        final String scheme = getScheme(req);
        commonUriBuilder.append(scheme);
        commonUriBuilder.append("://");
        commonUriBuilder.append(host);
        if (!(port < 0 || (port == 80 && "http".equals(scheme)) || (port == 443 && "https".equals(scheme)))) {
            commonUriBuilder.append(':');
            commonUriBuilder.append(port);
        }
        final String commonUriPrefix = commonUriBuilder.toString();

        // The Base URI - up to the servlet path
        final StringBuilder baseUriBuilder = new StringBuilder(commonUriPrefix);
        baseUriBuilder.append(req.getContextPath());
        baseUriBuilder.append(req.getServletPath());
        final URI baseUri = URI.create(baseUriBuilder.toString());

        // The RequestURI - everything in the URL
        final StringBuilder requestUriBuilder = new StringBuilder(commonUriPrefix);
        requestUriBuilder.append(req.getRequestURI());
        final String queryString = req.getQueryString();
        if (queryString != null) {
            requestUriBuilder.append('?');
            requestUriBuilder.append(queryString);
        }
        final URI requestUri = URI.create(requestUriBuilder.toString());
        return new ServletContainerRequest(getMethod(req), requestUri, baseUri, getEntityStream(req), getHeaders(req),
                                           getSecurityContext(req));
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

    /**
     * Get the URL that is forwarded using the standard X-Forwarded-Host header.
     *
     * @param servletRequest
     * @return The URL of the forwarded host. If the header is missing or invalid, null is returned.
     */
    private static URL getForwardedUrl(HttpServletRequest servletRequest) {
        final String forwardedHostAndPort = servletRequest.getHeader(FORWARDED_HOST);
        if (forwardedHostAndPort == null || forwardedHostAndPort.isEmpty()) {
            return null;
        }
        URL url = parseForwardedHostHeader(forwardedHostAndPort, servletRequest);
        if (url == null && LOG.isWarnEnabled()) {
            LOG.warn("Ignoring invalid " + ExtHttpHeaders.FORWARDED_HOST + ": " + forwardedHostAndPort);
        }
        return url;
    }

    /** Parse according to IETF standard for Host field: http://tools.ietf.org/html/rfc7230#section-5.4 */
    private static URL parseForwardedHostHeader(String forwardedHostAndPort, HttpServletRequest servletRequest) {
        final String[] parts = forwardedHostAndPort.split(":");
        if (parts.length > 2) {
            return null;
        }
        int fwdPort = -1;
        if (parts.length == 2) {
            try {
                fwdPort = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return null;
            }
            if (fwdPort < 0) {
                return null;
            }
        }
        final String fwdHost = parts[0];
        final String scheme = getScheme(servletRequest);
        try {
            return new URI(scheme, null, fwdHost, fwdPort, null, null, null).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            LOG.debug(e.getLocalizedMessage());
        }
        return null;
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
