/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import org.everrest.core.Filter;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.method.MethodInvokerFilter;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author andrew00x
 */
public class RestComponentResolver {
    private ResourceBinder resources;
    private ProviderBinder providers;

    public RestComponentResolver(ResourceBinder resources, ProviderBinder providers) {
        this.resources = resources;
        this.providers = providers;
    }

    @SuppressWarnings({"unchecked"})
    public void addSingleton(Object instance) {
        Class clazz = instance.getClass();
        if (clazz.getAnnotation(Provider.class) != null) {
            if (instance instanceof ContextResolver) {
                providers.addContextResolver((ContextResolver)instance);
            }
            if (instance instanceof ExceptionMapper) {
                providers.addExceptionMapper((ExceptionMapper)instance);
            }
            if (instance instanceof MessageBodyReader) {
                providers.addMessageBodyReader((MessageBodyReader)instance);
            }
            if (instance instanceof MessageBodyWriter) {
                providers.addMessageBodyWriter((MessageBodyWriter)instance);
            }
        } else if (clazz.getAnnotation(Filter.class) != null) {
            if (instance instanceof MethodInvokerFilter) {
                providers.addMethodInvokerFilter((MethodInvokerFilter)instance);
            }
            if (instance instanceof RequestFilter) {
                providers.addRequestFilter((RequestFilter)instance);
            }
            if (instance instanceof ResponseFilter) {
                providers.addResponseFilter((ResponseFilter)instance);
            }
        } else if (clazz.getAnnotation(Path.class) != null) {
            resources.addResource(instance, null);
        }
    }

    @SuppressWarnings({"unchecked"})
    public void addPerRequest(Class clazz) {
        if (clazz.getAnnotation(Provider.class) != null) {
            if (ContextResolver.class.isAssignableFrom(clazz)) {
                providers.addContextResolver(clazz);
            }
            if (ExceptionMapper.class.isAssignableFrom(clazz)) {
                providers.addExceptionMapper(clazz);
            }
            if (MessageBodyReader.class.isAssignableFrom(clazz)) {
                providers.addMessageBodyReader(clazz);
            }
            if (MessageBodyWriter.class.isAssignableFrom(clazz)) {
                providers.addMessageBodyWriter(clazz);
            }
        } else if (clazz.getAnnotation(Filter.class) != null) {
            if (MethodInvokerFilter.class.isAssignableFrom(clazz)) {
                providers.addMethodInvokerFilter(clazz);
            }
            if (RequestFilter.class.isAssignableFrom(clazz)) {
                providers.addRequestFilter(clazz);
            }
            if (ResponseFilter.class.isAssignableFrom(clazz)) {
                providers.addResponseFilter(clazz);
            }
        } else if (clazz.getAnnotation(Path.class) != null) {
            resources.addResource(clazz, null);
        }
    }

    @SuppressWarnings({"unchecked"})
    public void addFactory(ObjectFactory factory) {
        Class clazz = factory.getObjectModel().getObjectClass();
        if (clazz.getAnnotation(Provider.class) != null) {
            if (ContextResolver.class.isAssignableFrom(clazz)) {
                providers.addContextResolver(factory);
            }
            if (ExceptionMapper.class.isAssignableFrom(clazz)) {
                providers.addExceptionMapper(factory);
            }
            if (MessageBodyReader.class.isAssignableFrom(clazz)) {
                providers.addMessageBodyReader(factory);
            }
            if (MessageBodyWriter.class.isAssignableFrom(clazz)) {
                providers.addMessageBodyWriter(factory);
            }
        } else if (clazz.getAnnotation(Filter.class) != null) {
            if (MethodInvokerFilter.class.isAssignableFrom(clazz)) {
                providers.addMethodInvokerFilter(factory);
            }
            if (RequestFilter.class.isAssignableFrom(clazz)) {
                providers.addRequestFilter(factory);
            }
            if (ResponseFilter.class.isAssignableFrom(clazz)) {
                providers.addResponseFilter(factory);
            }
        } else if (clazz.getAnnotation(Path.class) != null) {
            resources.addResource(factory);
        }
    }

}
