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
package org.everrest.core.impl;

import org.everrest.core.GenericContainerRequest;
import org.everrest.core.impl.header.AcceptLanguage;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.impl.header.Language;
import org.everrest.core.impl.header.MediaTypeHelper;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Variant;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author andrew00x
 */
public class ContainerRequest implements GenericContainerRequest {
    /** HTTP method. */
    private String method;

    /** HTTP request message body as stream. */
    private InputStream entityStream;

    /** HTTP headers. */
    private MultivaluedMap<String, String> httpHeaders;

    /** Parsed HTTP cookies. */
    private Map<String, Cookie> cookies;

    /** Source strings of HTTP cookies. */
    private List<String> cookieHeaders;

    /** HTTP header Content-Type. */
    private MediaType contentType;

    /** HTTP header Content-Language. */
    private Locale contentLanguage;

    /** List of accepted media type, HTTP header Accept. List is sorted by quality value factor. */
    private List<MediaType> acceptMediaType;

    /** List of accepted language, HTTP header Accept-Language. List is sorted by quality value factor. */
    private List<Locale> acceptLanguage;

    /** Full request URI, includes query string and fragment. */
    private URI requestUri;

    /** Base URI, e.g. servlet path. */
    private URI baseUri;

    /** Security context. */
    private SecurityContext securityContext;

    /**
     * Constructs new instance of ContainerRequest.
     *
     * @param method
     *         HTTP method
     * @param requestUri
     *         full request URI
     * @param baseUri
     *         base request URI
     * @param entityStream
     *         request message body as stream
     * @param httpHeaders
     *         HTTP headers
     * @param securityContext
     *         SecurityContext
     */
    public ContainerRequest(String method, URI requestUri, URI baseUri, InputStream entityStream,
                            MultivaluedMap<String, String> httpHeaders, SecurityContext securityContext) {
        this.method = method;
        this.requestUri = requestUri;
        this.baseUri = baseUri;
        this.entityStream = entityStream;
        this.httpHeaders = httpHeaders;
        this.securityContext = securityContext;
    }

    // GenericContainerRequest

    /** {@inheritDoc} */
    @Override
    public MediaType getAcceptableMediaType(List<MediaType> mediaTypes) {
        for (MediaType at : getAcceptableMediaTypes()) {
            for (MediaType rt : mediaTypes) {
                if (MediaTypeHelper.isMatched(at, rt)) {
                    return rt;
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getCookieHeaders() {
        if (cookieHeaders == null) {
            List<String> c = getRequestHeader(COOKIE);
            if (c != null && c.size() > 0) {
                cookieHeaders = Collections.unmodifiableList(getRequestHeader(COOKIE));
            } else {
                cookieHeaders = Collections.emptyList();
            }
        }
        return cookieHeaders;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getEntityStream() {
        return entityStream;
    }

    /** {@inheritDoc} */
    @Override
    public URI getRequestUri() {
        return requestUri;
    }

    /** {@inheritDoc} */
    @Override
    public URI getBaseUri() {
        return baseUri;
    }

    /** {@inheritDoc} */
    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    /** {@inheritDoc} */
    @Override
    public void setEntityStream(InputStream entityStream) {
        this.entityStream = entityStream;

        // reset form data, it should be recreated
        ApplicationContextImpl.getCurrent().getAttributes().remove("org.everrest.provider.entity.form");
    }

    /** {@inheritDoc} */
    @Override
    public void setUris(URI requestUri, URI baseUri) {
        this.requestUri = requestUri;
        this.baseUri = baseUri;
    }

    /** {@inheritDoc} */
    @Override
    public void setCookieHeaders(List<String> cookieHeaders) {
        this.cookieHeaders = cookieHeaders;

        // reset parsed cookies
        this.cookies = null;
    }

    /** {@inheritDoc} */
    @Override
    public void setRequestHeaders(MultivaluedMap<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;

        // reset dependent fields
        this.cookieHeaders = null;
        this.cookies = null;
        this.contentType = null;
        this.contentLanguage = null;
        this.acceptMediaType = null;
        this.acceptLanguage = null;
    }

    // javax.ws.rs.core.SecurityContext

    /** {@inheritDoc} */
    @Override
    public String getAuthenticationScheme() {
        return securityContext.getAuthenticationScheme();
    }

    /** {@inheritDoc} */
    @Override
    public Principal getUserPrincipal() {
        return securityContext.getUserPrincipal();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSecure() {
        return securityContext.isSecure();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUserInRole(String role) {
        return securityContext.isUserInRole(role);
    }

    // javax.ws.rs.core.Request

    /** {@inheritDoc} */
    @Override
    public ResponseBuilder evaluatePreconditions(EntityTag etag) {
        ResponseBuilder rb = evaluateIfMatch(etag);
        if (rb != null) {
            return rb;
        }

        return evaluateIfNoneMatch(etag);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        long lastModifiedTime = lastModified.getTime();
        ResponseBuilder rb = evaluateIfModified(lastModifiedTime);
        if (rb != null) {
            return rb;
        }

        return evaluateIfUnmodified(lastModifiedTime);

    }

    /** {@inheritDoc} */
    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag etag) {
        ResponseBuilder rb = evaluateIfMatch(etag);
        if (rb != null) {
            return rb;
        }

        long lastModifiedTime = lastModified.getTime();
        rb = evaluateIfModified(lastModifiedTime);
        if (rb != null) {
            return rb;
        }

        rb = evaluateIfNoneMatch(etag);
        if (rb != null) {
            return rb;
        }

        return evaluateIfUnmodified(lastModifiedTime);

    }

    /** {@inheritDoc} */
    @Override
    public String getMethod() {
        return method;
    }

    /** {@inheritDoc} */
    @Override
    public Variant selectVariant(List<Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("The list of variants is null or empty");
        }
        // TODO constructs and set 'Vary' header in response
        // Response will be set in RequestDispatcher if set Response
        // now then it will be any way rewrite in RequestDispatcher.
        return VariantsHandler.handleVariants(this, variants);
    }

    // javax.ws.rs.core.HttpHeaders

    /**
     * If accept-language header does not present or its length is null then default language list will be returned. This list contains
     * only one element Locale with language '*', and it minds any language accepted.
     * {@inheritDoc}
     */
    @Override
    public List<Locale> getAcceptableLanguages() {
        if (acceptLanguage == null) {
            List<AcceptLanguage> l =
                    HeaderHelper.createAcceptedLanguageList(HeaderHelper.convertToString(getRequestHeader(ACCEPT_LANGUAGE)));
            List<Locale> t = new ArrayList<Locale>(l.size());
            // extract Locales from AcceptLanguage
            for (AcceptLanguage al : l) {
                t.add(al.getLocale());
            }

            acceptLanguage = Collections.unmodifiableList(t);
        }

        return acceptLanguage;
    }

    /**
     * If accept header does not presents or its length is null then list with one element will be returned. That one element is default
     * media type, see {@link AcceptMediaType#DEFAULT}.
     * {@inheritDoc}
     */
    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        if (acceptMediaType == null) {
            // 'extract' MediaType from AcceptMediaType
            List<MediaType> t =
                    new ArrayList<MediaType>(HeaderHelper.createAcceptedMediaTypeList(HeaderHelper
                                                                                              .convertToString(getRequestHeader(ACCEPT))));
            acceptMediaType = Collections.unmodifiableList(t);
        }

        return acceptMediaType;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Cookie> getCookies() {
        if (cookies == null) {
            Map<String, Cookie> t = new HashMap<String, Cookie>();

            for (String ch : getCookieHeaders()) {
                List<Cookie> l = HeaderHelper.parseCookies(ch);
                for (Cookie c : l) {
                    t.put(c.getName(), c);
                }
            }

            cookies = Collections.unmodifiableMap(t);
        }

        return cookies;
    }

    /** {@inheritDoc} */
    @Override
    public Locale getLanguage() {
        if (contentLanguage == null && httpHeaders.getFirst(CONTENT_LANGUAGE) != null) {
            contentLanguage = Language.getLocale(httpHeaders.getFirst(CONTENT_LANGUAGE));
        }

        return contentLanguage;
    }

    /** {@inheritDoc} */
    @Override
    public MediaType getMediaType() {
        if (contentType == null && httpHeaders.getFirst(CONTENT_TYPE) != null) {
            contentType = MediaType.valueOf(httpHeaders.getFirst(CONTENT_TYPE));
        }

        return contentType;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getRequestHeader(String name) {
        return httpHeaders.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return httpHeaders;
    }

    /**
     * Comparison for If-Match header and ETag.
     *
     * @param etag
     *         the ETag
     * @return ResponseBuilder with status 412 (precondition failed) if If-Match header is NOT MATCH to ETag or null otherwise
     */
    private ResponseBuilder evaluateIfMatch(EntityTag etag) {
        String ifMatch = getRequestHeaders().getFirst(IF_MATCH);
        // Strong comparison is required.
        // From specification:
        // The strong comparison function: in order to be considered equal,
        // both validators MUST be identical in every way, and both MUST
        // NOT be weak.

        if (ifMatch == null) {
            return null;
        }

        EntityTag otherEtag = EntityTag.valueOf(ifMatch);

        if (etag.isWeak() || otherEtag.isWeak()
            || (!"*".equals(otherEtag.getValue()) && !etag.getValue().equals(otherEtag.getValue()))) {
            return Response.status(Response.Status.PRECONDITION_FAILED);
        }

        // if tags are not matched then do as tag 'if-match' is absent
        return null;

    }

    /**
     * Comparison for If-None-Match header and ETag.
     *
     * @param etag
     *         the ETag
     * @return ResponseBuilder with status 412 (precondition failed) if If-None-Match header is MATCH to ETag and HTTP method is not GET or
     * HEAD. If method is GET or HEAD and If-None-Match is MATCH to ETag then ResponseBuilder with status 304 (not modified) will be
     * returned.
     */
    private ResponseBuilder evaluateIfNoneMatch(EntityTag etag) {
        String ifNoneMatch = getRequestHeaders().getFirst(IF_NONE_MATCH);

        if (ifNoneMatch == null) {
            return null;
        }

        EntityTag otherEtag = EntityTag.valueOf(ifNoneMatch);
        String httpMethod = getMethod();
        // The weak comparison function can only be used with GET or HEAD requests.
        if (httpMethod.equals(HttpMethod.GET) || httpMethod.equals(HttpMethod.HEAD)) {

            if ("*".equals(otherEtag.getValue()) || etag.getValue().equals(otherEtag.getValue())) {
                return Response.notModified(etag);
            }

        } else {
            // Use strong comparison (ignore weak tags) because HTTP method is not GET
            // or HEAD. If one of tag is weak then tags are not identical.
            if (!etag.isWeak() && !otherEtag.isWeak()
                && ("*".equals(otherEtag.getValue()) || etag.getValue().equals(otherEtag.getValue()))) {
                return Response.status(Response.Status.PRECONDITION_FAILED);
            }

        }

        // if tags are matched then do as tag 'if-none-match' is absent
        return null;

    }

    /**
     * Comparison for lastModified and unmodifiedSince times.
     *
     * @param lastModified
     *         the last modified time
     * @return ResponseBuilder with status 412 (precondition failed) if lastModified time is greater then unmodifiedSince otherwise return
     * null. If date format in header If-Unmodified-Since is wrong also null returned
     */
    private ResponseBuilder evaluateIfModified(long lastModified) {
        String ifUnmodified = getRequestHeaders().getFirst(IF_UNMODIFIED_SINCE);

        if (ifUnmodified == null) {
            return null;
        }
        try {
            long unmodifiedSince = HeaderHelper.parseDateHeader(ifUnmodified).getTime();
            if (lastModified > unmodifiedSince) {
                return Response.status(Response.Status.PRECONDITION_FAILED);
            }

        } catch (IllegalArgumentException e) {
            // If the specified date is invalid, the header is ignored.
        }

        return null;
    }

    /**
     * Comparison for lastModified and modifiedSince times.
     *
     * @param lastModified
     *         the last modified time
     * @return ResponseBuilder with status 304 (not modified) if lastModified time is greater then modifiedSince otherwise return null. If
     * date format in header If-Modified-Since is wrong also null returned
     */
    private ResponseBuilder evaluateIfUnmodified(long lastModified) {
        String ifModified = getRequestHeaders().getFirst(IF_MODIFIED_SINCE);

        if (ifModified == null) {
            return null;
        }
        try {
            long modifiedSince = HeaderHelper.parseDateHeader(ifModified).getTime();
            if (lastModified < modifiedSince) {
                return Response.notModified();
            }

        } catch (IllegalArgumentException ignored) {
            // If the specified date is invalid, the header is ignored.
        }

        return null;
    }
}
