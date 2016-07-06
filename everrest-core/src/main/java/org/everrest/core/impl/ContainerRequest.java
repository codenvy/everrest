/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.impl.header.AcceptLanguage;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.impl.header.MediaTypeHelper;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.HEAD;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.everrest.core.impl.header.HeaderHelper.convertToString;
import static org.everrest.core.impl.header.HeaderHelper.createAcceptMediaTypeList;
import static org.everrest.core.impl.header.HeaderHelper.createAcceptedLanguageList;

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
    private List<AcceptMediaType> acceptableMediaTypes;

    /** List of accepted language, HTTP header Accept-Language. List is sorted by quality value factor. */
    private List<Locale> acceptLanguages;

    /** Full request URI, includes query string and fragment. */
    private URI requestUri;

    /** Base URI, e.g. servlet path. */
    private URI baseUri;

    /** Security context. */
    private SecurityContext securityContext;

    private VariantsHandler variantsHandler = new VariantsHandler();

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

    @Override
    public MediaType getAcceptableMediaType(List<MediaType> mediaTypes) {
        for (MediaType acceptMediaType : getAcceptableMediaTypes()) {
            for (MediaType checkMediaType : mediaTypes) {
                if (MediaTypeHelper.isMatched(acceptMediaType, checkMediaType)) {
                    return checkMediaType;
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getCookieHeaders() {
        if (cookieHeaders == null) {
            List<String> cookieHeaders = getRequestHeader(COOKIE);
            if (cookieHeaders == null || cookieHeaders.isEmpty()) {
                this.cookieHeaders = emptyList();
            } else {
                this.cookieHeaders = unmodifiableList(cookieHeaders);
            }
        }
        return cookieHeaders;
    }

    @Override
    public InputStream getEntityStream() {
        return entityStream;
    }

    @Override
    public URI getRequestUri() {
        return requestUri;
    }

    @Override
    public URI getBaseUri() {
        return baseUri;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void setEntityStream(InputStream entityStream) {
        this.entityStream = entityStream;

        ApplicationContext context = ApplicationContext.getCurrent();
        context.getAttributes().remove("org.everrest.provider.entity.decoded.form");
        context.getAttributes().remove("org.everrest.provider.entity.encoded.form");
    }

    @Override
    public void setUris(URI requestUri, URI baseUri) {
        this.requestUri = requestUri;
        this.baseUri = baseUri;
    }

    @Override
    public void setCookieHeaders(List<String> cookieHeaders) {
        this.cookieHeaders = cookieHeaders;
        this.cookies = null;
    }

    @Override
    public void setRequestHeaders(MultivaluedMap<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;

        this.cookieHeaders = null;
        this.cookies = null;
        this.contentType = null;
        this.contentLanguage = null;
        this.acceptableMediaTypes = null;
        this.acceptLanguages = null;
    }

    @Override
    public String getAuthenticationScheme() {
        return securityContext.getAuthenticationScheme();
    }

    @Override
    public Principal getUserPrincipal() {
        return securityContext.getUserPrincipal();
    }

    @Override
    public boolean isSecure() {
        return securityContext.isSecure();
    }

    @Override
    public boolean isUserInRole(String role) {
        return securityContext.isUserInRole(role);
    }

    @Override
    public ResponseBuilder evaluatePreconditions(EntityTag etag) {
        checkArgument(etag != null, "Null ETag is not supported");
        ResponseBuilder responseBuilder = evaluateIfMatch(etag);
        if (responseBuilder == null) {
            responseBuilder = evaluateIfNoneMatch(etag);
        }
        return responseBuilder;
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        checkArgument(lastModified != null, "Null last modification date is not supported");
        long lastModifiedTime = lastModified.getTime();
        ResponseBuilder responseBuilder = evaluateIfModified(lastModifiedTime);
        if (responseBuilder == null) {
            responseBuilder = evaluateIfUnmodified(lastModifiedTime);
        }
        return responseBuilder;
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag etag) {
        checkArgument(lastModified != null, "Null last modification date is not supported");
        checkArgument(etag != null, "Null ETag is not supported");

        ResponseBuilder responseBuilder = evaluateIfMatch(etag);
        if (responseBuilder != null) {
            return responseBuilder;
        }

        long lastModifiedTime = lastModified.getTime();
        responseBuilder = evaluateIfModified(lastModifiedTime);
        if (responseBuilder != null) {
            return responseBuilder;
        }

        responseBuilder = evaluateIfNoneMatch(etag);
        if (responseBuilder != null) {
            return responseBuilder;
        }

        return evaluateIfUnmodified(lastModifiedTime);
    }

    @Override
    public ResponseBuilder evaluatePreconditions() {
        List<String> ifMatch = getRequestHeader(IF_MATCH);
        return (ifMatch == null || ifMatch.isEmpty()) ? null : Response.status(PRECONDITION_FAILED);
    }


    @Override
    public Variant selectVariant(List<Variant> variants) {
        checkArgument(!(variants == null || variants.isEmpty()), "The list of variants is null or empty");
        return variantsHandler.handleVariants(this, variants);
    }

    /**
     * If accept-language header does not present or its length is null then default language list will be returned. This list contains
     * only one element Locale with language '*', and it minds any language accepted.
     */
    @Override
    public List<Locale> getAcceptableLanguages() {
        if (acceptLanguages == null) {
            List<AcceptLanguage> acceptLanguages = createAcceptedLanguageList(convertToString(getRequestHeader(ACCEPT_LANGUAGE)));
            List<Locale> locales = new ArrayList<>(acceptLanguages.size());
            locales.addAll(acceptLanguages.stream().map(language -> language.getLanguage().getLocale()).collect(toList()));

            this.acceptLanguages = unmodifiableList(locales);
        }

        return acceptLanguages;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return getAcceptMediaTypeList().stream().map(AcceptMediaType::getMediaType).collect(toList());
    }

    @Override
    public List<AcceptMediaType> getAcceptMediaTypeList() {
        if (acceptableMediaTypes == null) {
            acceptableMediaTypes = createAcceptMediaTypeList(convertToString(getRequestHeader(ACCEPT)));
        }
        return acceptableMediaTypes;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        if (this.cookies == null) {
            Map<String, Cookie> cookies = new HashMap<>();

            for (String cookieHeader : getCookieHeaders()) {
                List<Cookie> parsedCookies = HeaderHelper.parseCookies(cookieHeader);
                for (Cookie cookie : parsedCookies) {
                    cookies.put(cookie.getName(), cookie);
                }
            }

            this.cookies = unmodifiableMap(cookies);
        }

        return cookies;
    }

    @Override
    public Date getDate() {
        String date = getRequestHeaders().getFirst(DATE);
        return date == null ? null : HeaderHelper.parseDateHeader(date);
    }

    @Override
    public int getLength() {
        String length = getRequestHeaders().getFirst(CONTENT_LENGTH);
        return length == null ? -1 : Integer.parseInt(length);
    }

    @Override
    public Locale getLanguage() {
        if (contentLanguage == null && httpHeaders.getFirst(CONTENT_LANGUAGE) != null) {
            contentLanguage = RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class).fromString(httpHeaders.getFirst(CONTENT_LANGUAGE));
        }

        return contentLanguage;
    }

    @Override
    public MediaType getMediaType() {
        if (contentType == null && httpHeaders.getFirst(CONTENT_TYPE) != null) {
            contentType = MediaType.valueOf(httpHeaders.getFirst(CONTENT_TYPE));
        }

        return contentType;
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return httpHeaders.get(name);
    }

    @Override
    public String getHeaderString(String name) {
        return convertToString(getRequestHeader(name));
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return httpHeaders;
    }

    /**
     * Comparison for If-Match header and ETag.
     *
     * @param etag
     *         the ETag
     * @return ResponseBuilder with status 412 (precondition failed) if If-Match header does NOT MATCH to ETag or null otherwise
     */
    private ResponseBuilder evaluateIfMatch(EntityTag etag) {
        String ifMatch = getRequestHeaders().getFirst(IF_MATCH);

        if (isNullOrEmpty(ifMatch)) {
            return null;
        }

        EntityTag otherEtag = EntityTag.valueOf(ifMatch);

        if (eTagsStrongEqual(etag, otherEtag)) {
            return null;
        }
        return Response.status(PRECONDITION_FAILED);
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

        if (Strings.isNullOrEmpty(ifNoneMatch)) {
            return null;
        }

        EntityTag otherEtag = EntityTag.valueOf(ifNoneMatch);
        String httpMethod = getMethod();
        if (httpMethod.equals(GET) || httpMethod.equals(HEAD)) {
            if (eTagsWeakEqual(etag, otherEtag)) {
                return Response.notModified(etag);
            }
        } else {
            if (eTagsStrongEqual(etag, otherEtag)) {
                return Response.status(PRECONDITION_FAILED);
            }
        }
        return null;
    }

    private boolean eTagsStrongEqual(EntityTag etag, EntityTag otherEtag) {
        // Strong comparison is required.
        // From specification:
        // The strong comparison function: in order to be considered equal,
        // both validators MUST be identical in every way, and both MUST NOT be weak.
        return !etag.isWeak() && !otherEtag.isWeak()
               && ("*".equals(otherEtag.getValue()) || etag.getValue().equals(otherEtag.getValue()));
    }

    private boolean eTagsWeakEqual(EntityTag etag, EntityTag otherEtag) {
        return "*".equals(otherEtag.getValue()) || etag.getValue().equals(otherEtag.getValue());
    }

    /**
     * Comparison for lastModified and unmodifiedSince times.
     *
     * @param lastModified
     *         the last modified time
     * @return ResponseBuilder with status 412 (precondition failed) if lastModified time is greater then unmodifiedSince otherwise return
     * null. If date format in header If-Unmodified-Since is wrong also null returned
     */
    private ResponseBuilder evaluateIfUnmodified(long lastModified) {
        String ifUnmodified = getRequestHeaders().getFirst(IF_UNMODIFIED_SINCE);

        if (isNullOrEmpty(ifUnmodified)) {
            return null;
        }
        try {
            long unmodifiedSince = HeaderHelper.parseDateHeader(ifUnmodified).getTime();
            if (lastModified > unmodifiedSince) {
                return Response.status(PRECONDITION_FAILED);
            }
        } catch (IllegalArgumentException ignored) {
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
    private ResponseBuilder evaluateIfModified(long lastModified) {
        String ifModified = getRequestHeaders().getFirst(IF_MODIFIED_SINCE);

        if (isNullOrEmpty(ifModified)) {
            return null;
        }
        try {
            long modifiedSince = HeaderHelper.parseDateHeader(ifModified).getTime();
            if (lastModified <= modifiedSince) {
                return Response.notModified();
            }
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }

    void setVariantsHandler(VariantsHandler variantsHandler) {
        this.variantsHandler = variantsHandler;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("Method", method)
                          .add("BaseUri", baseUri)
                          .add("RequestUri", requestUri)
                          .toString();
    }
}
