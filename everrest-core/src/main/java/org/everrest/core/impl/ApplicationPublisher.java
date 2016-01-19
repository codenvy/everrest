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

import org.everrest.core.Filter;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.method.MethodInvokerFilter;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ApplicationPublisher {
    protected final ResourceBinder resources;
    protected final ProviderBinder providers;

    public ApplicationPublisher(ResourceBinder resources, ProviderBinder providers) {
        this.resources = resources;
        this.providers = providers;
    }

    public void publish(Application application) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Set<Class<?>> appClasses = application.getClasses();
        if (appClasses != null) {
            classes.addAll(appClasses);
        }
        if (application instanceof EverrestApplication) {
            EverrestApplication everrest = (EverrestApplication)application;
            for (Map.Entry<String, Class<?>> e : everrest.getResourceClasses().entrySet()) {
                Class<?> clazz = e.getValue();
                addResource(e.getKey(), clazz);
                classes.remove(clazz);
            }
            for (Map.Entry<String, Object> e : everrest.getResourceSingletons().entrySet()) {
                addResource(e.getKey(), e.getValue());
            }
            for (ObjectFactory<? extends ObjectModel> factory : everrest.getFactories()) {
                addFactory(factory);
                classes.remove(factory.getObjectModel().getObjectClass());
            }
        }
        for (Class<?> clazz : classes) {
            addPerRequest(clazz);
        }
        Set<Object> singletons = application.getSingletons();
        if (singletons != null) {
            for (Object instance : singletons) {
                addSingleton(instance);
            }
        }
    }

    private void addResource(String uriPattern, Class<?> resourceClass) {
        resources.addResource(uriPattern, resourceClass, null);
    }

    private void addResource(String uriPattern, Object resource) {
        resources.addResource(uriPattern, resource, null);
    }

    private void addSingleton(Object instance) {
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

    @SuppressWarnings({"unchecked"})
    private void addPerRequest(Class clazz) {
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

    @SuppressWarnings({"unchecked"})
    private void addFactory(ObjectFactory factory) {
        Class clazz = factory.getObjectModel().getObjectClass();
        if (clazz.getAnnotation(Provider.class) != null) {
            // per-request provider
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
            // per-request filter
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
            // per-request resource
            resources.addResource(factory);
        }
    }
}
