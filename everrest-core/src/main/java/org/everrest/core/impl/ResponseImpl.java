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

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.impl.header.Language;
import org.everrest.core.util.CaselessMultivaluedMap;
import org.everrest.core.util.CaselessStringWrapper;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public final class ResponseImpl extends Response {
    /** HTTP status. */
    private final int status;

    /** Entity. Entity will be written as response message body. */
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
        failIfClosed();
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
        failIfClosed();
        // TODO: implement
        if (entity == null) {
            throw new IllegalStateException("Reading for null entity isn't supported");
        }
        throw new IllegalStateException("Reading for null entity " + entity.getClass() + " supported");
    }

    @Override
    public boolean hasEntity() {
        failIfClosed();
        return entity != null;
    }

    @Override
    public boolean bufferEntity() {
        failIfClosed();
        // TODO: implement
        return false;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    private void failIfClosed() {
        if (closed) {
            throw new IllegalArgumentException("Response already closed");
        }
    }

    @Override
    public MediaType getMediaType() {
        Object value = getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
        if (value == null) {
            return null;
        }
        if (value instanceof MediaType) {
            return (MediaType)value;
        }
        return MediaType.valueOf(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value));
    }

    @Override
    public Locale getLanguage() {
        Object value = getMetadata().getFirst(HttpHeaders.CONTENT_LANGUAGE);
        if (value == null) {
            return null;
        }
        if (value instanceof Locale) {
            return (Locale)value;
        }
        return Language.getLocale(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value));
    }

    @Override
    public int getLength() {
        Object value = getMetadata().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (value == null) {
            return -1;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return Integer.valueOf(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value));
    }

    @Override
    public Set<String> getAllowedMethods() {
        List<Object> allowed = getMetadata().get(HttpHeaders.ALLOW);
        if (allowed == null) {
            return Collections.emptySet();
        }
        Set<String> allowedSet = new LinkedHashSet<>();
        for (Object value : allowed) {
            if (value instanceof String) {
                for (String s : ((String)value).split(",")) {
                    if (!s.trim().isEmpty()) {
                        allowedSet.add(s.toUpperCase());
                    }
                }
            } else if (value != null) {
                allowedSet.add(HeaderHelper.getHeaderAsString(value).toUpperCase());
            }
        }
        return allowedSet;
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        return null;
    }

    @Override
    public EntityTag getEntityTag() {
        Object value = getMetadata().getFirst(HttpHeaders.ETAG);
        if (value == null) {
            return null;
        }
        if (value instanceof EntityTag) {
            return (EntityTag)value;
        }
        return EntityTag.valueOf(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value));
    }

    @Override
    public Date getDate() {
        return getDateHeader(HttpHeaders.DATE);
    }

    @Override
    public Date getLastModified() {
        return getDateHeader(HttpHeaders.LAST_MODIFIED);
    }

    private Date getDateHeader(String name) {
        Object value = getMetadata().getFirst(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date)value;
        }
        return HeaderHelper.parseDateHeader(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value));
    }

    @Override
    public URI getLocation() {
        Object value = getMetadata().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (value == null) {
            return null;
        }
        if (value instanceof URI) {
            return (URI)value;
        }
        return URI.create(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value));
    }

    @Override
    public Set<Link> getLinks() {
        List<Object> links = getMetadata().get(HttpHeaders.LINK);
        if (links == null) {
            return Collections.emptySet();
        }
        Set<Link> linkSet = new LinkedHashSet<>();
        for (Object value : links) {
            if (value instanceof Link) {
                linkSet.add((Link)value);
            } else {
                linkSet.add(Link.valueOf(value instanceof String ? (String)value : HeaderHelper.getHeaderAsString(value)));
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
                    headerStrings.add(entry.getKey(), HeaderHelper.getHeaderAsString(value));
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
        List<String> headerStrings = new LinkedList<>();
        for (Object header : headers) {
            headerStrings.add(HeaderHelper.getHeaderAsString(header));
        }
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

    // ResponseBuilder

    /** @see ResponseBuilder */
    public static final class ResponseBuilderImpl extends ResponseBuilder {

        /** HTTP headers which can't be multivalued. */
        private enum HEADERS {
            /** Cache control. */
            CACHE_CONTROL,
            /** Content-Language. */
            CONTENT_LANGUAGE,
            /** Content-Location. */
            CONTENT_LOCATION,
            /** Content-Type. */
            CONTENT_TYPE,
            /** Content-length. */
            CONTENT_LENGTH,
            /** ETag. */
            ETAG,
            /** Expires. */
            EXPIRES,
            /** Last-Modified. */
            LAST_MODIFIED,
            /** Location. */
            LOCATION
        }

        private static final Map<CaselessStringWrapper, HEADERS> HEADER_TO_ENUM = new HashMap<>();

        static {
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.CACHE_CONTROL), HEADERS.CACHE_CONTROL);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.CONTENT_LANGUAGE), HEADERS.CONTENT_LANGUAGE);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.CONTENT_LOCATION), HEADERS.CONTENT_LOCATION);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.CONTENT_TYPE), HEADERS.CONTENT_TYPE);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.CONTENT_LENGTH), HEADERS.CONTENT_LENGTH);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.ETAG), HEADERS.ETAG);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.LAST_MODIFIED), HEADERS.LAST_MODIFIED);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.LOCATION), HEADERS.LOCATION);
            HEADER_TO_ENUM.put(new CaselessStringWrapper(HttpHeaders.EXPIRES), HEADERS.EXPIRES);
        }

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
            MultivaluedMap<String, Object> m = new OutputHeadersMap(headers);
            // add cookies
            if (cookies.size() > 0) {
                for (NewCookie c : cookies.values()) {
                    m.add(HttpHeaders.SET_COOKIE, c);
                }
            }
            Response response = new ResponseImpl(status, entity, entityAnnotations, m);
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
            headers.putSingle(HttpHeaders.CACHE_CONTROL, cacheControl);
            return this;
        }

        @Override
        public ResponseBuilder encoding(String encoding) {
            if (encoding == null) {
                headers.remove(HttpHeaders.CONTENT_ENCODING);
            } else {
                headers.putSingle(HttpHeaders.CONTENT_ENCODING, encoding);
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
                headers.remove(HttpHeaders.CONTENT_LOCATION);
            } else {
                headers.putSingle(HttpHeaders.CONTENT_LOCATION, location);
            }
            return this;
        }


        @Override
        public ResponseBuilder cookie(NewCookie... cookies) {
            if (cookies == null) {
                this.cookies.clear();
                this.headers.remove(HttpHeaders.SET_COOKIE);
            } else {
                // new cookie overwrite old ones with the same name
                for (NewCookie c : cookies) {
                    this.cookies.put(c.getName(), c);
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
                headers.remove(HttpHeaders.ALLOW);
            } else {
                Collections.addAll(headers.getList(HttpHeaders.ALLOW), methods);
            }
            return this;
        }

        @Override
        public ResponseBuilder allow(Set<String> methods) {
            if (methods == null) {
                headers.remove(HttpHeaders.ALLOW);
            } else {
                headers.getList(HttpHeaders.ALLOW).addAll(methods);
            }
            return this;
        }


        @Override
        public ResponseBuilder expires(Date expires) {
            if (expires == null) {
                headers.remove(HttpHeaders.EXPIRES);
            } else {
                headers.putSingle(HttpHeaders.EXPIRES, expires);
            }
            return this;
        }


        @Override
        public ResponseBuilder header(String name, Object value) {
            if (value == null) {
                headers.remove(name);
            } else {
                if (HEADER_TO_ENUM.get(new CaselessStringWrapper(name)) != null) {
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
                headers.remove(HttpHeaders.CONTENT_LANGUAGE);
            } else {
                headers.putSingle(HttpHeaders.CONTENT_LANGUAGE, language);
            }
            return this;
        }


        @Override
        public ResponseBuilder language(Locale language) {
            if (language == null) {
                headers.remove(HttpHeaders.CONTENT_LANGUAGE);
            } else {
                headers.putSingle(HttpHeaders.CONTENT_LANGUAGE, language);
            }
            return this;
        }


        @Override
        public ResponseBuilder lastModified(Date lastModified) {
            if (lastModified == null) {
                headers.remove(HttpHeaders.LAST_MODIFIED);
            } else {
                headers.putSingle(HttpHeaders.LAST_MODIFIED, lastModified);
            }
            return this;
        }


        @Override
        public ResponseBuilder location(URI location) {
            if (location == null) {
                headers.remove(HttpHeaders.LOCATION);
            } else {
                headers.putSingle(HttpHeaders.LOCATION, location);
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
                headers.remove(HttpHeaders.ETAG);
            } else {
                headers.putSingle(HttpHeaders.ETAG, tag);
            }
            return this;
        }


        @Override
        public ResponseBuilder tag(String tag) {
            if (tag == null) {
                headers.remove(HttpHeaders.ETAG);
            } else {
                headers.putSingle(HttpHeaders.ETAG, tag);
            }
            return this;
        }

        @Override
        public ResponseBuilder variants(Variant... variants) {
            return variants(variants == null ? null : Arrays.asList(variants));
        }


        @Override
        public ResponseBuilder type(MediaType type) {
            if (type == null) {
                headers.remove(HttpHeaders.CONTENT_TYPE);
            } else {
                headers.putSingle(HttpHeaders.CONTENT_TYPE, type);
            }
            return this;
        }


        @Override
        public ResponseBuilder type(String type) {
            if (type == null) {
                headers.remove(HttpHeaders.CONTENT_TYPE);
            } else {
                headers.putSingle(HttpHeaders.CONTENT_TYPE, type);
            }
            return this;
        }


        @Override
        public ResponseBuilder variant(Variant variant) {
            if (variant == null) {
                variant = new Variant(null, (String)null, null);
            }
            type(variant.getMediaType());
            language(variant.getLanguage());
            if (variant.getEncoding() != null) {
                header(HttpHeaders.CONTENT_ENCODING, variant.getEncoding());
            }
            return this;
        }


        @Override
        public ResponseBuilder variants(List<Variant> variants) {
            if (variants == null) {
                headers.remove(HttpHeaders.VARY);
                return this;
            }
            if (variants.isEmpty()) {
                return this;
            }

            boolean acceptMediaType = variants.get(0).getMediaType() != null;
            boolean acceptLanguage = variants.get(0).getLanguage() != null;
            boolean acceptEncoding = variants.get(0).getEncoding() != null;

            for (Variant v : variants) {
                acceptMediaType |= v.getMediaType() != null;
                acceptLanguage |= v.getLanguage() != null;
                acceptEncoding |= v.getEncoding() != null;
            }

            StringBuilder sb = new StringBuilder();
            if (acceptMediaType) {
                sb.append(HttpHeaders.ACCEPT);
            }
            if (acceptLanguage) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(HttpHeaders.ACCEPT_LANGUAGE);
            }
            if (acceptEncoding) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(HttpHeaders.ACCEPT_ENCODING);
            }
            if (sb.length() > 0) {
                header(HttpHeaders.VARY, sb.toString());
            }
            return this;
        }

        @Override
        public ResponseBuilder links(Link... links) {
            if (links == null) {
               headers.remove(HttpHeaders.LINK);
            } else {
                Collections.addAll(headers.getList(HttpHeaders.LINK), links);
            }
            return this;
        }

        @Override
        public ResponseBuilder link(URI uri, String rel) {
            headers.getList(HttpHeaders.LINK).add(Link.fromUri(uri).rel(rel));
            return this;
        }

        @Override
        public ResponseBuilder link(String uri, String rel) {
            return link(URI.create(uri), rel);
        }
    }
}
