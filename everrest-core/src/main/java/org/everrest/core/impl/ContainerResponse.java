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

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.util.Logger;
import org.everrest.core.util.Tracer;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author andrew00x
 */
public class ContainerResponse implements GenericContainerResponse {
   /* ----------------- Write response helpers ---------------- */

    /**
     * Wrapper for underlying MessageBodyWriter. Need such wrapper to give possibility update HTTP headers but commit them before writing
     * the response body. NotifiesOutputStream wraps original OutputStream for the HTTP body and notify OutputListener about any changes,
     * e.g. write bytes, flush or close. OutputListener processes events and initiates process of commit HTTP headers after getting the
     * first one.
     */
    private static class BodyWriter implements MessageBodyWriter<Object> {
        private final MessageBodyWriter<Object> delegate;
        private final OutputListener            writeListener;
        private static final Logger LOG = Logger.getLogger(BodyWriter.class);

        public BodyWriter(MessageBodyWriter<Object> writer, OutputListener writeListener) {
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
                if(Iterables.any(Throwables.getCausalChain(e), new Predicate<Throwable>() {
                    @Override
                    public boolean apply(Throwable input) {
                        return "org.apache.catalina.connector.ClientAbortException".equals(input.getClass().getName());
                    }
                })){
                    LOG.warn("Client has aborted connection. Response writing omitted.");
                    return;
                }
                throw e;
            }
        }
    }

    /**
     * Use underlying output stream as data stream. Pass all invocations to the back-end stream and notify OutputListener about changes in
     * back-end stream.
     */
    private static class NotifiesOutputStream extends OutputStream {
        OutputStream   delegate;
        OutputListener writeListener;

        public NotifiesOutputStream(OutputStream output, OutputListener writeListener) {
            this.delegate = output;
            this.writeListener = writeListener;
        }

        @Override
        public void write(int b) throws IOException {
            writeListener.onChange(null);
            delegate.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            writeListener.onChange(null);
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            writeListener.onChange(null);
            delegate.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            writeListener.onChange(null);
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            writeListener.onChange(null);
            delegate.close();
        }
    }

    /** Listen any changes in response output stream, e.g. write, flush, close, */
    private static interface OutputListener {
        void onChange(java.util.EventObject event) throws IOException;
    }

   /* --------------------------------------------------------- */

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ContainerResponse.class);

    /** See {@link ContainerResponseWriter}. */
    private ContainerResponseWriter responseWriter;

    /**
     * @param responseWriter
     *         See {@link ContainerResponseWriter}
     */
    public ContainerResponse(ContainerResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    // GenericContainerResponse

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
                GenericEntity ge = (GenericEntity)entity;
                entity = ge.getEntity();
                entityType = ge.getType();
            } else if (entity != null) {
                entityType = entity.getClass();
            }

            Object contentTypeHeader = getHttpHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentTypeHeader instanceof MediaType) {
                contentType = (MediaType)contentTypeHeader;
            } else if (contentTypeHeader != null) {
                contentType = MediaType.valueOf(contentTypeHeader.toString());
            } else {
                contentType = null;
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
            if (Tracer.isTracingEnabled()) {
                Tracer.addTraceHeaders(this);
            }

            responseWriter.writeHeaders(this);
            return;
        }

        ApplicationContext context = ApplicationContextImpl.getCurrent();
        MediaType contentType = getContentType();

        // if content-type is still not preset try determine it
        if (contentType == null || (contentType.isWildcardType() && contentType.isWildcardSubtype())) {
            List<MediaType> availableWriters = context.getProviders().getAcceptableWriterMediaTypes(entity.getClass(), entityType, null);
            MediaType mediaType;
            if (availableWriters.isEmpty() ||
                (availableWriters.size() == 1 && (mediaType = availableWriters.get(0)).isWildcardType() && mediaType.isWildcardSubtype())) {
                // getAcceptableMediaTypes() return list which contains at least one element even HTTP header 'accept' is absent
                contentType = context.getContainerRequest().getAcceptableMediaTypes().get(0);
            } else {
                contentType = context.getContainerRequest().getAcceptableMediaType(availableWriters);
            }

            if (contentType == null || contentType.isWildcardType() || contentType.isWildcardSubtype()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }

            this.contentType = contentType;
            getHttpHeaders().putSingle(HttpHeaders.CONTENT_TYPE, contentType);
        }

        MessageBodyWriter entityWriter = context.getProviders().getMessageBodyWriter(entity.getClass(), entityType, null, contentType);

        if (entityWriter == null) {
            String message = "Not found writer for " + entity.getClass() + " and MIME type " + contentType;
            if (context.getContainerRequest().getMethod().equals(HttpMethod.HEAD)) {
                // just warning here, HEAD method we do not need write entity
                LOG.warn(message);
                getHttpHeaders().putSingle(HttpHeaders.CONTENT_LENGTH, Long.toString(-1));
            } else {
                LOG.error(message);

                Response notAcceptableResponse = Response
                        .status(Response.Status.NOT_ACCEPTABLE)
                        .entity(message)
                        .type(MediaType.TEXT_PLAIN)
                        .build();
                setResponse(notAcceptableResponse);
                // MessageBodyWriter for String is embedded.
                entityWriter = context.getProviders().getMessageBodyWriter(String.class, null, null, contentType);
            }
        } else {
            if (Tracer.isTracingEnabled()) {
                Tracer.trace("Matched MessageBodyWriter for type " + entity.getClass()
                             + ", media type " + contentType
                             + " = (" + entityWriter + ")");
            }

            if (getHttpHeaders().getFirst(HttpHeaders.CONTENT_LENGTH) == null) {
                long contentLength = entityWriter.getSize(entity, entity.getClass(), entityType, null, contentType);
                if (contentLength >= 0) {
                    getHttpHeaders().putSingle(HttpHeaders.CONTENT_LENGTH, Long.toString(contentLength));
                }
            }
        }

        if (context.getContainerRequest().getMethod().equals(HttpMethod.HEAD)) {
            entity = null;
        }

        OutputListener headersWriter = new OutputListener() {
            private boolean done;

            @Override
            public void onChange(java.util.EventObject event) throws IOException {
                if (!done) {
                    done = true;
                    responseWriter.writeHeaders(ContainerResponse.this);
                }
            }
        };

        if (Tracer.isTracingEnabled()) {
            Tracer.addTraceHeaders(this);
        }

        responseWriter.writeBody(this, new BodyWriter(entityWriter, headersWriter));
        headersWriter.onChange(null); // Be sure headers were written.
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
        return headers;
    }


    @Override
    public int getStatus() {
        return status;
    }
}
