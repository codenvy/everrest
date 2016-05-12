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

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.util.CaselessMultivaluedMap;
import org.everrest.core.util.CaselessStringWrapper;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.ALLOW;
import static javax.ws.rs.core.HttpHeaders.CACHE_CONTROL;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LOCATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.DATE;
import static javax.ws.rs.core.HttpHeaders.ETAG;
import static javax.ws.rs.core.HttpHeaders.EXPIRES;
import static javax.ws.rs.core.HttpHeaders.LAST_MODIFIED;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;
import static javax.ws.rs.core.HttpHeaders.VARY;
import static org.everrest.core.impl.header.HeaderHelper.getHeaderAsString;

/**
 * @author andrew00x
 */
public final class ResponseImpl extends Response {
    /** HTTP status. */
    private final int status;

    /** Entity of response */
    private final Object entity;

    /** Annotations that will be passed to the {@link javax.ws.rs.ext.MessageBodyWriter}. */
    private Annotation[] entityAnnotations;

    /** HTTP headers. */
    private final MultivaluedMap<String, Object> headers;

    private boolean closed;

    /**
     * Construct Response with supplied status, entity and headers.
     *
     * @param status
     *         HTTP status
     * @param entity
     *         an entity
     * @param headers
     *         HTTP headers
     */
    ResponseImpl(int status, Object entity, Annotation[] entityAnnotations, MultivaluedMap<String, Object> headers) {
        this.status = status;
        this.entity = entity;
        this.entityAnnotations = entityAnnotations;
        this.headers = headers;
    }

    @Override
    public Object getEntity() {
        checkState(!closed, "Response already closed");
        return entity;
    }

    public Annotation[] getEntityAnnotations() {
        return entityAnnotations;
    }

    @Override
    public <T> T readEntity(Class<T> entityType) {
        return doReadEntity(entityType, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readEntity(GenericType<T> entityType) {
        return doReadEntity((Class<T>)entityType.getRawType(), entityType.getType(), null);
    }

    @Override
    public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
        return doReadEntity(entityType, null, annotations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
        return doReadEntity((Class<T>)entityType.getRawType(), entityType.getType(), annotations);
    }

    private <T> T doReadEntity(Class<T> type, Type genericType, Annotation[] annotations) {
        checkState(!closed, "Response already closed");
        // TODO: implement as part of client implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasEntity() {
        checkState(!closed, "Response already closed");
        return entity != null;
    }

    @Override
    public boolean bufferEntity() {
        checkState(!closed, "Response already closed");
        // TODO: implement as part of client implementation
        return false;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public MediaType getMediaType() {
        Object value = getMetadata().getFirst(CONTENT_TYPE);
        if (value == null) {
            return null;
        }
        if (value instanceof MediaType) {
            return (MediaType)value;
        }
        return MediaType.valueOf(value instanceof String ? (String)value : getHeaderAsString(value));
    }

    @Override
    public Locale getLanguage() {
        Object value = getMetadata().getFirst(CONTENT_LANGUAGE);
        if (value == null) {
            return null;
        }
        if (value instanceof Locale) {
            return (Locale)value;
        }
        return RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class)
                              .fromString(value instanceof String ? (String)value : getHeaderAsString(value));
    }

    @Override
    public int getLength() {
        Object value = getMetadata().getFirst(CONTENT_LENGTH);
        if (value == null) {
            return -1;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return Integer.valueOf(value instanceof String ? (String)value : getHeaderAsString(value));
    }

    @Override
    public Set<String> getAllowedMethods() {
        List<Object> allowedHeaders = getMetadata().get(ALLOW);
        if (allowedHeaders == null) {
            return Collections.emptySet();
        }
        Set<String> allowedMethods = new LinkedHashSet<>();
        for (Object allowMethod : allowedHeaders) {
            if (allowMethod instanceof String) {
                for (String s : ((String)allowMethod).split(",")) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        allowedMethods.add(s.toUpperCase());
                    }
                }
            } else if (allowMethod != null) {
                allowedMethods.add(getHeaderAsString(allowMethod).toUpperCase());
            }
        }
        return allowedMethods;
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        List<Object> cookieHeaders = getMetadata().get(SET_COOKIE);
        if (cookieHeaders == null) {
            return Collections.emptyMap();
        }
        Map<String, NewCookie> cookies = new HashMap<>();
        for (Object cookieHeader : cookieHeaders) {
            if (cookieHeader instanceof NewCookie) {
                NewCookie newCookie = (NewCookie)cookieHeader;
                cookies.put(newCookie.getName(), newCookie);
            } else if (cookieHeader != null) {
                NewCookie newCookie = NewCookie.valueOf(getHeaderAsString(cookieHeader));
                if (newCookie != null) {
                    cookies.put(newCookie.getName(), newCookie);
                }
            }
        }

        return cookies;
    }

    @Override
    public EntityTag getEntityTag() {
        Object value = getMetadata().getFirst(ETAG);
        if (value == null) {
            return null;
        }
        if (value instanceof EntityTag) {
            return (EntityTag)value;
        }
        return EntityTag.valueOf(value instanceof String ? (String)value : getHeaderAsString(value));
    }

    @Override
    public Date getDate() {
        return getDateHeader(DATE);
    }

    @Override
    public Date getLastModified() {
        return getDateHeader(LAST_MODIFIED);
    }

    private Date getDateHeader(String name) {
        Object value = getMetadata().getFirst(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date)value;
        }
        return HeaderHelper.parseDateHeader(value instanceof String ? (String)value : getHeaderAsString(value));
    }

    @Override
    public URI getLocation() {
        Object value = getMetadata().getFirst(LOCATION);
        if (value == null) {
            return null;
        }
        if (value instanceof URI) {
            return (URI)value;
        }
        return URI.create(value instanceof String ? (String)value : getHeaderAsString(value));
    }

    @Override
    public Set<Link> getLinks() {
        List<Object> links = getMetadata().get(LINK);
        if (links == null) {
            return Collections.emptySet();
        }
        Set<Link> linkSet = new LinkedHashSet<>();
        for (Object value : links) {
            if (value instanceof Link) {
                linkSet.add((Link)value);
            } else {
                linkSet.add(Link.valueOf(value instanceof String ? (String)value : getHeaderAsString(value)));
            }
        }
        return linkSet;
    }

    @Override
    public boolean hasLink(String relation) {
        for (Link link : getLinks()) {
            if (link.getRels().contains(relation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Link getLink(String relation) {
        for (Link link : getLinks()) {
            if (link.getRels().contains(relation)) {
                return link;
            }
        }
        return null;
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        Link link = getLink(relation);
        if (link == null) {
            return null;
        }
        return Link.fromLink(link);
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        return headers;
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        CaselessMultivaluedMap<String> headerStrings = new CaselessMultivaluedMap<>();
        for (Map.Entry<String, List<Object>> entry : getMetadata().entrySet()) {
            List<Object> values = entry.getValue();
            if (values != null) {
                for (Object value : values) {
                    headerStrings.add(entry.getKey(), getHeaderAsString(value));
                }
            }
        }
        return headerStrings;
    }

    @Override
    public String getHeaderString(String name) {
        List<Object> headers = getMetadata().get(name);
        if (headers == null) {
            return null;
        }
        List<String> headerStrings = headers.stream().map(HeaderHelper::getHeaderAsString).collect(toList());
        return HeaderHelper.convertToString(headerStrings);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public StatusType getStatusInfo() {
        final Status statusInstance = Status.fromStatusCode(status);
        if (statusInstance != null) {
            return statusInstance;
        }
        return new StatusType() {
            @Override
            public int getStatusCode() {
                return status;
            }

            @Override
            public Status.Family getFamily() {
                return Status.Family.familyOf(status);
            }

            @Override
            public String getReasonPhrase() {
                return "Unknown";
            }
        };
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("Status", status)
                          .add("Content type", getMediaType())
                          .add("Entity type", entity == null ? null : entity.getClass())
                          .omitNullValues()
                          .toString();
    }

    /** @see ResponseBuilder */
    public static final class ResponseBuilderImpl extends ResponseBuilder {

        /** HTTP headers which can't be multivalued. */
        static final Set<CaselessStringWrapper> SINGLE_VALUE_HEADERS =
                newHashSet(new CaselessStringWrapper(CACHE_CONTROL),
                           new CaselessStringWrapper(CONTENT_LANGUAGE),
                           new CaselessStringWrapper(CONTENT_LOCATION),
                           new CaselessStringWrapper(CONTENT_TYPE),
                           new CaselessStringWrapper(CONTENT_LENGTH),
                           new CaselessStringWrapper(ETAG),
                           new CaselessStringWrapper(LAST_MODIFIED),
                           new CaselessStringWrapper(LOCATION),
                           new CaselessStringWrapper(EXPIRES));

        /** Default HTTP status, No-content, 204. */
        private static final int DEFAULT_HTTP_STATUS = Response.Status.NO_CONTENT.getStatusCode();

        /** Default HTTP status. */
        private int status = DEFAULT_HTTP_STATUS;

        /** Entity. Entity will be written as response message body. */
        private Object entity;

        private Annotation[] entityAnnotations;

        /** HTTP headers. */
        private final ExtMultivaluedMap<String, Object> headers = new CaselessMultivaluedMap<>();

        /** HTTP cookies, Set-Cookie header. */
        private final Map<String, NewCookie> cookies = new HashMap<>();

        /** See {@link ResponseBuilder}. */
        ResponseBuilderImpl() {
        }

        /**
         * Useful for clone method.
         *
         * @param other
         *         other ResponseBuilderImpl
         * @see #clone()
         */
        private ResponseBuilderImpl(ResponseBuilderImpl other) {
            this.status = other.status;
            this.entity = other.entity;
            this.headers.putAll(other.headers);
            this.cookies.putAll(other.cookies);
            if (other.entityAnnotations != null) {
                this.entityAnnotations = new Annotation[other.entityAnnotations.length];
                System.arraycopy(other.entityAnnotations, 0, this.entityAnnotations, 0, this.entityAnnotations.length);
            }
        }


        @Override
        public Response build() {
            MultivaluedMap<String, Object> httpHeaders = new CaselessMultivaluedMap<>(headers);
            if (!cookies.isEmpty()) {
                for (NewCookie c : cookies.values()) {
                    httpHeaders.add(SET_COOKIE, c);
                }
            }
            Response response = new ResponseImpl(status, entity, entityAnnotations, httpHeaders);
            reset();
            return response;
        }

        /** Set ResponseBuilder to default state. */
        private void reset() {
            status = DEFAULT_HTTP_STATUS;
            entity = null;
            entityAnnotations = null;
            headers.clear();
            cookies.clear();
        }


        @Override
        public ResponseBuilder cacheControl(CacheControl cacheControl) {
            if (cacheControl == null) {
                headers.remove(CACHE_CONTROL);
            } else {
                headers.putSingle(CACHE_CONTROL, cacheControl);
            }
            return this;
        }

        @Override
        public ResponseBuilder encoding(String encoding) {
            if (encoding == null) {
                headers.remove(CONTENT_ENCODING);
            } else {
                headers.putSingle(CONTENT_ENCODING, encoding);
            }
            return this;
        }

        @Override
        public ResponseBuilder clone() {
            return new ResponseBuilderImpl(this);
        }

        @Override
        public ResponseBuilder contentLocation(URI location) {
            if (location == null) {
                headers.remove(CONTENT_LOCATION);
            } else {
                headers.putSingle(CONTENT_LOCATION, location);
            }
            return this;
        }

        @Override
        public ResponseBuilder cookie(NewCookie... cookies) {
            if (cookies == null) {
                this.cookies.clear();
                this.headers.remove(SET_COOKIE);
            } else {
                for (NewCookie cookie : cookies) {
                    this.cookies.put(cookie.getName(), cookie);
                }
            }
            return this;
        }

        @Override
        public ResponseBuilder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public ResponseBuilder entity(Object entity, Annotation[] annotations) {
            this.entity = entity;
            this.entityAnnotations = annotations;
            return this;
        }

        @Override
        public ResponseBuilder allow(String... methods) {
            if (methods == null) {
                headers.remove(ALLOW);
            } else {
                headers.addAll(ALLOW, methods);
            }
            return this;
        }

        @Override
        public ResponseBuilder allow(Set<String> methods) {
            if (methods == null) {
                headers.remove(ALLOW);
            } else {
                headers.getList(ALLOW).addAll(methods);
            }
            return this;
        }


        @Override
        public ResponseBuilder expires(Date expires) {
            if (expires == null) {
                headers.remove(EXPIRES);
            } else {
                headers.putSingle(EXPIRES, expires);
            }
            return this;
        }


        @Override
        public ResponseBuilder header(String name, Object value) {
            if (value == null) {
                headers.remove(name);
            } else {
                if (SINGLE_VALUE_HEADERS.contains(new CaselessStringWrapper(name))) {
                    headers.putSingle(name, value);
                } else {
                    headers.add(name, value);
                }
            }
            return this;
        }

        @Override
        public ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
            this.headers.clear();
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        @Override
        public ResponseBuilder language(String language) {
            if (language == null) {
                headers.remove(CONTENT_LANGUAGE);
            } else {
                headers.putSingle(CONTENT_LANGUAGE, language);
            }
            return this;
        }

        @Override
        public ResponseBuilder language(Locale language) {
            if (language == null) {
                headers.remove(CONTENT_LANGUAGE);
            } else {
                headers.putSingle(CONTENT_LANGUAGE, language);
            }
            return this;
        }

        @Override
        public ResponseBuilder lastModified(Date lastModified) {
            if (lastModified == null) {
                headers.remove(LAST_MODIFIED);
            } else {
                headers.putSingle(LAST_MODIFIED, lastModified);
            }
            return this;
        }

        @Override
        public ResponseBuilder location(URI location) {
            if (location == null) {
                headers.remove(LOCATION);
            } else {
                headers.putSingle(LOCATION, location);
            }
            return this;
        }

        @Override
        public ResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        @Override
        public ResponseBuilder tag(EntityTag tag) {
            if (tag == null) {
                headers.remove(ETAG);
            } else {
                headers.putSingle(ETAG, tag);
            }
            return this;
        }

        @Override
        public ResponseBuilder tag(String tag) {
            if (tag == null) {
                headers.remove(ETAG);
            } else {
                headers.putSingle(ETAG, tag);
            }
            return this;
        }

        @Override
        public ResponseBuilder type(MediaType type) {
            if (type == null) {
                headers.remove(CONTENT_TYPE);
            } else {
                headers.putSingle(CONTENT_TYPE, type);
            }
            return this;
        }

        @Override
        public ResponseBuilder type(String type) {
            if (type == null) {
                headers.remove(CONTENT_TYPE);
            } else {
                headers.putSingle(CONTENT_TYPE, type);
            }
            return this;
        }

        @Override
        public ResponseBuilder variant(Variant variant) {
            if (variant == null) {
                type((String)null);
                language((String)null);
                encoding(null);
            } else {
                type(variant.getMediaType());
                language(variant.getLanguage());
                encoding(variant.getEncoding());
            }
            return this;
        }

        @Override
        public ResponseBuilder variants(Variant... variants) {
            return variants(variants == null ? null : Arrays.asList(variants));
        }

        @Override
        public ResponseBuilder variants(List<Variant> variants) {
            if (variants == null) {
                headers.remove(VARY);
                return this;
            }
            if (variants.isEmpty()) {
                return this;
            }

            boolean acceptMediaType = variants.get(0).getMediaType() != null;
            boolean acceptLanguage = variants.get(0).getLanguage() != null;
            boolean acceptEncoding = variants.get(0).getEncoding() != null;

            for (Variant variant : variants) {
                acceptMediaType |= variant.getMediaType() != null;
                acceptLanguage |= variant.getLanguage() != null;
                acceptEncoding |= variant.getEncoding() != null;
            }

            List<String> varyHeader = new ArrayList<>();
            if (acceptMediaType) {
                varyHeader.add(ACCEPT);
            }
            if (acceptLanguage) {
                varyHeader.add(ACCEPT_LANGUAGE);
            }
            if (acceptEncoding) {
                varyHeader.add(ACCEPT_ENCODING);
            }

            if (varyHeader.size() > 0) {
                header(VARY, Joiner.on(',').join(varyHeader));
            }
            return this;
        }

        @Override
        public ResponseBuilder links(Link... links) {
            if (links == null) {
               headers.remove(LINK);
            } else {
                headers.addAll(LINK, links);
            }
            return this;
        }

        @Override
        public ResponseBuilder link(URI uri, String rel) {
            headers.getList(LINK).add(Link.fromUri(uri).rel(rel).build());
            return this;
        }

        @Override
        public ResponseBuilder link(String uri, String rel) {
            return link(URI.create(uri), rel);
        }
    }
}
