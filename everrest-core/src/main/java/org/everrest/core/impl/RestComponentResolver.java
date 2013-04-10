/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl;

import org.everrest.core.Filter;
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
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class RestComponentResolver {

    private ResourceBinder resources;

    private ProviderBinder providers;

    public RestComponentResolver(ResourceBinder resources, ProviderBinder providers) {
        this.resources = resources;
        this.providers = providers;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addSingleton(Object instance) {
        Class clazz = instance.getClass();
        if (clazz.getAnnotation(Provider.class) != null) {
            // singleton provider
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
            // singleton filter
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
            // singleton resource
            resources.addResource(instance, null);
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addPerRequest(Class clazz) {
        if (clazz.getAnnotation(Provider.class) != null) {
            // per-request provider
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
            // per-request filter
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
            // per-request resource
            resources.addResource(clazz, null);
        }

    }

}
