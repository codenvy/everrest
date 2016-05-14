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
import com.google.common.base.Throwables;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.everrest.core.util.CaselessMultivaluedMap;
import org.everrest.core.util.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.EventObject;
import java.util.List;

import static javax.ws.rs.HttpMethod.HEAD;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;

/**
 * @author andrew00x
 */
public class ContainerResponse implements GenericContainerResponse {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerResponse.class);

    /**
     * Wrapper for underlying MessageBodyWriter. Need such wrapper to give possibility update HTTP headers but commit them before writing
     * the response body. NotifiesOutputStream wraps original OutputStream for the HTTP body and notify OutputListener about any changes,
     * e.g. write bytes, flush or close. OutputListener processes events and initiates process of commit HTTP headers after getting the
     * first one.
     */
    private static class BodyWriter implements MessageBodyWriter<Object> {
        private final MessageBodyWriter<Object> delegate;
        private final OutputListener            writeListener;

        BodyWriter(MessageBodyWriter<Object> writer, OutputListener writeListener) {
            this.delegate = writer;
            this.writeListener = writeListener;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return delegate.isWriteable(type, genericType, annotations, mediaType);
        }

        @Override
        public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return delegate.getSize(t, type, genericType, annotations, mediaType);
        }

        @Override
        public void writeTo(Object t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {

            try {
                delegate.writeTo(t, type, genericType, annotations, mediaType, httpHeaders,
                                 new NotifiesOutputStream(entityStream, writeListener));
            } catch (Exception e) {
                if (Throwables.getCausalChain(e).stream().anyMatch(throwable -> "org.apache.catalina.connector.ClientAbortException".equals(throwable.getClass().getName()))) {
                    LOG.warn("Client has aborted connection. Response writing omitted.");
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Use underlying output stream as data stream. Pass all invocations to the back-end stream and notify OutputListener about changes in
     * back-end stream.
     */
    private static class NotifiesOutputStream extends FilterOutputStream {
        OutputListener writeListener;

        NotifiesOutputStream(OutputStream output, OutputListener writeListener) {
            super(output);
            this.writeListener = writeListener;
        }

        @Override
        public void write(int b) throws IOException {
            writeListener.onChange(new EventObject(this));
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            writeListener.onChange(new EventObject(this));
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            writeListener.onChange(new EventObject(this));
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            writeListener.onChange(new EventObject(this));
            out.flush();
        }

        @Override
        public void close() throws IOException {
            writeListener.onChange(new EventObject(this));
            out.close();
        }
    }

    /** Listen any changes in response output stream, e.g. write, flush, close, */
    private interface OutputListener {
        void onChange(EventObject event) throws IOException;
    }

    /** HTTP status. */
    private int status;
    /** Entity type. */
    private Type entityType;
    /** Entity. */
    private Object entity;
    /** HTTP response headers. */
    private MultivaluedMap<String, Object> headers;
    /** Response entity content-type. */
    private MediaType contentType;
    /** See {@link Response}, {@link ResponseBuilder}. */
    private Response response;
    /** See {@link ContainerResponseWriter}. */
    private ContainerResponseWriter responseWriter;

    /**
     * @param responseWriter
     *         See {@link ContainerResponseWriter}
     */
    public ContainerResponse(ContainerResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void setResponse(Response response) {
        this.response = response;

        if (response == null) {
            status = 0;
            entity = null;
            entityType = null;
            headers = null;
            contentType = null;
        } else {
            status = response.getStatus();
            headers = response.getMetadata();
            entity = response.getEntity();

            if (entity instanceof GenericEntity) {
                GenericEntity genericEntity = (GenericEntity)entity;
                entity = genericEntity.getEntity();
                entityType = genericEntity.getType();
            } else if (entity != null) {
                entityType = entity.getClass();
            }

            if (headers != null) {
                Object contentTypeHeader = headers.getFirst(CONTENT_TYPE);
                if (contentTypeHeader instanceof MediaType) {
                    contentType = (MediaType)contentTypeHeader;
                } else if (contentTypeHeader != null) {
                    contentType = MediaType.valueOf(HeaderHelper.getHeaderAsString(contentTypeHeader));
                } else {
                    contentType = null;
                }
            }
        }
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeResponse() throws IOException {
        if (entity == null) {
            writeResponseWithoutEntity();
            return;
        }

        ApplicationContext context = ApplicationContext.getCurrent();
        MediaType contentType = getContentType();

        if (isNullOrWildcard(contentType)) {
            List<MediaType> acceptableWriterMediaTypes = context.getProviders().getAcceptableWriterMediaTypes(entity.getClass(), entityType, null);
            if (isEmptyOrContainsSingleWildcardMediaType(acceptableWriterMediaTypes)) {
                contentType = context.getContainerRequest().getAcceptableMediaTypes().get(0);
            } else {
                contentType = context.getContainerRequest().getAcceptableMediaType(acceptableWriterMediaTypes);
            }

            if (isNullOrWildcard(contentType)) {
                contentType = APPLICATION_OCTET_STREAM_TYPE;
            }

            this.contentType = contentType;
            getHttpHeaders().putSingle(CONTENT_TYPE, contentType);
        }

        MessageBodyWriter entityWriter = context.getProviders().getMessageBodyWriter(entity.getClass(), entityType, null, contentType);

        if (entityWriter == null) {
            String message = String.format("Not found writer for %s and MIME type %s", entity.getClass(), contentType);
            if (HEAD.equals(context.getContainerRequest().getMethod())) {
                LOG.warn(message);
                getHttpHeaders().putSingle(CONTENT_LENGTH, Long.toString(-1));
            } else {
                LOG.error(message);
                setResponse(Response.status(NOT_ACCEPTABLE)
                                    .entity(message)
                                    .type(TEXT_PLAIN)
                                    .build());
                entityWriter = new StringEntityProvider();
            }
        } else {
            if (Tracer.isTracingEnabled()) {
                Tracer.trace("Matched MessageBodyWriter for type %s, media type %s = (%s)", entity.getClass(), contentType, entityWriter);
            }

            if (getHttpHeaders().getFirst(CONTENT_LENGTH) == null) {
                long contentLength = entityWriter.getSize(entity, entity.getClass(), entityType, null, contentType);
                if (contentLength >= 0) {
                    getHttpHeaders().putSingle(CONTENT_LENGTH, Long.toString(contentLength));
                }
            }
        }

        if (context.getContainerRequest().getMethod().equals(HEAD)) {
            writeResponseWithoutEntity();
            return;
        }

        OutputListener headersWriter = new OutputListener() {
            private boolean done;

            @Override
            public void onChange(EventObject event) throws IOException {
                if (done) {
                    return;
                }
                done = true;
                responseWriter.writeHeaders(ContainerResponse.this);
            }
        };

        if (Tracer.isTracingEnabled()) {
            Tracer.addTraceHeaders(this);
        }

        responseWriter.writeBody(this, new BodyWriter(entityWriter, headersWriter));
    }

    private void writeResponseWithoutEntity() throws IOException {
        if (Tracer.isTracingEnabled()) {
            Tracer.addTraceHeaders(this);
        }

        responseWriter.writeHeaders(this);
    }

    private boolean isEmptyOrContainsSingleWildcardMediaType(List<MediaType> acceptableWriterMediaTypes) {
        if (acceptableWriterMediaTypes.isEmpty()) {
            return true;
        }
        if (acceptableWriterMediaTypes.size() == 1) {
            MediaType mediaType = acceptableWriterMediaTypes.get(0);
            if (mediaType.isWildcardType() && mediaType.isWildcardSubtype()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNullOrWildcard(MediaType contentType) {
        return contentType == null || contentType.isWildcardType() || contentType.isWildcardSubtype();
    }

    @Override
    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public Type getEntityType() {
        return entityType;
    }

    @Override
    public Object getEntity() {
        return entity;
    }

    @Override
    public MultivaluedMap<String, Object> getHttpHeaders() {
        if (headers == null) {
            headers = new CaselessMultivaluedMap<>();
        }
        return headers;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("Status", status)
                          .add("Content type", contentType)
                          .add("Entity type", entityType)
                          .omitNullValues()
                          .toString();
    }
}
