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

import org.everrest.core.util.CaselessMultivaluedMap;
import org.everrest.core.util.CaselessStringWrapper;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class ResponseImpl extends Response {
    /** HTTP status. */
    private final int status;

    /** Entity. Entity will be written as response message body. */
    private final Object entity;

    /** HTTP headers. */
    private final MultivaluedMap<String, Object> headers;

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
    ResponseImpl(int status, Object entity, MultivaluedMap<String, Object> headers) {
        this.status = status;
        this.entity = entity;
        this.headers = headers;
    }


    @Override
    public Object getEntity() {
        return entity;
    }


    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        return headers;
    }


    @Override
    public int getStatus() {
        return status;
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

        private static final Map<CaselessStringWrapper, HEADERS> HEADER_TO_ENUM =
                new HashMap<CaselessStringWrapper, HEADERS>();

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

        /** HTTP headers. */
        private final MultivaluedMap<String, Object> headers = new CaselessMultivaluedMap<Object>();

        /** HTTP cookies, Set-Cookie header. */
        private final Map<String, NewCookie> cookies = new HashMap<String, NewCookie>();

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
            Response response = new ResponseImpl(status, entity, m);
            reset();
            return response;
        }

        /** Set ResponseBuilder to default state. */
        private void reset() {
            status = DEFAULT_HTTP_STATUS;
            entity = null;
            headers.clear();
            cookies.clear();
        }


        @Override
        public ResponseBuilder cacheControl(CacheControl cacheControl) {
            headers.putSingle(HttpHeaders.CACHE_CONTROL, cacheControl);
            return this;
        }


        @Override
        public ResponseBuilder clone() {
            return new ResponseBuilderImpl(this);
        }


        @Override
        public ResponseBuilder contentLocation(URI location) {
            headers.putSingle(HttpHeaders.CONTENT_LOCATION, location);
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
        public ResponseBuilder expires(Date expires) {
            headers.putSingle(HttpHeaders.EXPIRES, expires);
            return this;
        }


        @Override
        public ResponseBuilder header(String name, Object value) {
            if (HEADER_TO_ENUM.get(new CaselessStringWrapper(name)) != null) {
                headers.putSingle(name, value);
            } else {
                if (value == null) {
                    headers.remove(name);
                } else {
                    headers.add(name, value);
                }
            }
            return this;
        }


        @Override
        public ResponseBuilder language(String language) {
            headers.putSingle(HttpHeaders.CONTENT_LANGUAGE, language);
            return this;
        }


        @Override
        public ResponseBuilder language(Locale language) {
            headers.putSingle(HttpHeaders.CONTENT_LANGUAGE, language);
            return this;
        }


        @Override
        public ResponseBuilder lastModified(Date lastModified) {
            headers.putSingle(HttpHeaders.LAST_MODIFIED, lastModified);
            return this;
        }


        @Override
        public ResponseBuilder location(URI location) {
            headers.putSingle(HttpHeaders.LOCATION, location);
            return this;
        }


        @Override
        public ResponseBuilder status(int status) {
            this.status = status;
            return this;
        }


        @Override
        public ResponseBuilder tag(EntityTag tag) {
            headers.putSingle(HttpHeaders.ETAG, tag);
            return this;
        }


        @Override
        public ResponseBuilder tag(String tag) {
            headers.putSingle(HttpHeaders.ETAG, tag);
            return this;
        }


        @Override
        public ResponseBuilder type(MediaType type) {
            headers.putSingle(HttpHeaders.CONTENT_TYPE, type);
            return this;
        }


        @Override
        public ResponseBuilder type(String type) {
            headers.putSingle(HttpHeaders.CONTENT_TYPE, type);
            return this;
        }


        @Override
        public ResponseBuilder variant(Variant variant) {
            type(variant.getMediaType());
            language(variant.getLanguage());
            if (variant.getEncoding() != null) {
                header(HttpHeaders.CONTENT_ENCODING, variant.getEncoding());
            }
            return this;
        }


        @Override
        public ResponseBuilder variants(List<Variant> variants) {
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
    }

}
