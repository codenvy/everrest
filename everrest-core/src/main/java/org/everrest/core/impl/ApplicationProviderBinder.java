/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.everrest.core.ApplicationContext;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.method.MethodInvokerFilter;

/**
 * Provider binder for concrete JAX-RS application. Set of providers from this binder always take
 * preference over providers embedded to EverRest framework. For example if
 * ApplicationProviderBinder is able to provide MessageBodyWriter or MessageBodyReader for media
 * type 'application/xml' then such reader/writer will be in use for all resources from the same
 * Application.
 *
 * @author andrew00x
 * @see javax.ws.rs.core.Application
 */
public class ApplicationProviderBinder extends ProviderBinder {
  @Override
  public List<MediaType> getAcceptableWriterMediaTypes(
      Class<?> type, Type genericType, Annotation[] annotations) {
    List<MediaType> mediaTypes = doGetAcceptableWriterMediaTypes(type, genericType, annotations);
    mediaTypes.addAll(getDefaults().getAcceptableWriterMediaTypes(type, genericType, annotations));
    return mediaTypes;
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
    ExceptionMapper<T> exceptionMapper = doGetExceptionMapper(type);
    if (exceptionMapper == null) {
      exceptionMapper = getDefaults().getExceptionMapper(type);
    }
    return exceptionMapper;
  }

  @Override
  public <T> MessageBodyReader<T> getMessageBodyReader(
      Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    MessageBodyReader<T> reader = doGetMessageBodyReader(type, genericType, annotations, mediaType);
    if (reader == null) {
      reader = getDefaults().getMessageBodyReader(type, genericType, annotations, mediaType);
    }
    return reader;
  }

  @Override
  public <T> MessageBodyWriter<T> getMessageBodyWriter(
      Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    MessageBodyWriter<T> writer = doGetMessageBodyWriter(type, genericType, annotations, mediaType);
    if (writer == null) {
      writer = getDefaults().getMessageBodyWriter(type, genericType, annotations, mediaType);
    }
    return writer;
  }

  @Override
  public List<MethodInvokerFilter> getMethodInvokerFilters(String path) {
    ApplicationContext context = ApplicationContext.getCurrent();
    List<MethodInvokerFilter> filters =
        doGetMatchedFilters(path, invokerFilters)
            .stream()
            .map(factory -> (MethodInvokerFilter) factory.getInstance(context))
            .collect(toList());
    filters.addAll(getDefaults().getMethodInvokerFilters(path));
    return filters;
  }

  @Override
  public List<RequestFilter> getRequestFilters(String path) {
    ApplicationContext context = ApplicationContext.getCurrent();
    List<RequestFilter> filters =
        doGetMatchedFilters(path, requestFilters)
            .stream()
            .map(factory -> (RequestFilter) factory.getInstance(context))
            .collect(toList());
    filters.addAll(getDefaults().getRequestFilters(path));
    return filters;
  }

  @Override
  public List<ResponseFilter> getResponseFilters(String path) {
    ApplicationContext context = ApplicationContext.getCurrent();
    List<ResponseFilter> filters =
        doGetMatchedFilters(path, responseFilters)
            .stream()
            .map(factory -> (ResponseFilter) factory.getInstance(context))
            .collect(toList());
    filters.addAll(getDefaults().getResponseFilters(path));
    return filters;
  }

  private ProviderBinder getDefaults() {
    return ProviderBinder.getInstance();
  }
}
