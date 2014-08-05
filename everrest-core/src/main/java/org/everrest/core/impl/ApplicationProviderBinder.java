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

import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Provider binder for concrete JAX-RS application. Set of providers from this binder always take preference over
 * providers embedded to EverRest framework. For example if ApplicationProviderBinder is able to provide
 * MessageBodyWriter or MessageBodyReader for media type 'application/xml' then such reader/writer will be in use for
 * all resources from the same Application.
 *
 * @author andrew00x
 * @see javax.ws.rs.core.Application
 */
public class ApplicationProviderBinder extends ProviderBinder {

    public ApplicationProviderBinder() {
        super();
    }

    @Override
    protected void init() {
        // Do not add default providers.
    }

    @Override
    public List<MediaType> getAcceptableWriterMediaTypes(Class<?> type, Type genericType, Annotation[] annotations) {
        List<MediaType> l = doGetAcceptableWriterMediaTypes(type, genericType, annotations);
        l.addAll(getDefaults().getAcceptableWriterMediaTypes(type, genericType, annotations));
        return l;
    }

    @Override
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
        ContextResolver<T> resolver = doGetContextResolver(contextType, mediaType);
        if (resolver == null) {
            resolver = getDefaults().getContextResolver(contextType, mediaType);
        }
        return resolver;
    }

    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        ExceptionMapper<T> excMapper = doGetExceptionMapper(type);
        if (excMapper == null) {
            excMapper = getDefaults().getExceptionMapper(type);
        }
        return excMapper;
    }

    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        MessageBodyReader<T> reader = doGetMessageBodyReader(type, genericType, annotations, mediaType);
        if (reader == null) {
            reader = getDefaults().getMessageBodyReader(type, genericType, annotations, mediaType);
        }
        return reader;
    }

    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        MessageBodyWriter<T> writer = doGetMessageBodyWriter(type, genericType, annotations, mediaType);
        if (writer == null) {
            writer = getDefaults().getMessageBodyWriter(type, genericType, annotations, mediaType);
        }
        return writer;
    }

    @Override
    public List<ObjectFactory<FilterDescriptor>> getMethodInvokerFilters(String path) {
        List<ObjectFactory<FilterDescriptor>> l = doGetMatchedFilters(path, invokerFilters);
        l.addAll(getDefaults().getMethodInvokerFilters(path));
        return l;
    }

    @Override
    public List<ObjectFactory<FilterDescriptor>> getRequestFilters(String path) {
        List<ObjectFactory<FilterDescriptor>> l = doGetMatchedFilters(path, requestFilters);
        l.addAll(getDefaults().getRequestFilters(path));
        return l;
    }

    @Override
    public List<ObjectFactory<FilterDescriptor>> getResponseFilters(String path) {
        List<ObjectFactory<FilterDescriptor>> l = doGetMatchedFilters(path, responseFilters);
        l.addAll(getDefaults().getResponseFilters(path));
        return l;
    }

    private ProviderBinder getDefaults() {
        return ProviderBinder.getInstance();
    }
}
