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

import org.everrest.core.impl.header.AcceptLanguageHeaderDelegate;
import org.everrest.core.impl.header.AcceptMediaTypeHeaderDelegate;
import org.everrest.core.impl.header.CacheControlHeaderDelegate;
import org.everrest.core.impl.header.CookieHeaderDelegate;
import org.everrest.core.impl.header.DateHeaderDelegate;
import org.everrest.core.impl.header.EntityTagHeaderDelegate;
import org.everrest.core.impl.header.LocaleHeaderDelegate;
import org.everrest.core.impl.header.MediaTypeHeaderDelegate;
import org.everrest.core.impl.header.NewCookieHeaderDelegate;
import org.everrest.core.impl.header.RangeHeaderDelegate;
import org.everrest.core.impl.header.StringHeaderDelegate;
import org.everrest.core.impl.header.URIHeaderDelegate;
import org.everrest.core.impl.uri.LinkBuilderImpl;
import org.everrest.core.impl.uri.UriBuilderImpl;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
public class RuntimeDelegateImpl extends RuntimeDelegate {
    /** HeaderDelegate cache. */
    private final Map<Class<?>, HeaderDelegate> headerDelegates = new HashMap<>();

    /**
     * Should be used only once for initialize.
     *
     * @see RuntimeDelegate#setInstance(RuntimeDelegate)
     * @see RuntimeDelegate#getInstance()
     */
    public RuntimeDelegateImpl() {
        init();
    }

    private void init() {
        // JSR-311
        addHeaderDelegate(new MediaTypeHeaderDelegate());
        addHeaderDelegate(new CacheControlHeaderDelegate());
        addHeaderDelegate(new CookieHeaderDelegate());
        addHeaderDelegate(new NewCookieHeaderDelegate());
        addHeaderDelegate(new EntityTagHeaderDelegate());
        addHeaderDelegate(new DateHeaderDelegate());
        // external
        addHeaderDelegate(new AcceptLanguageHeaderDelegate());
        addHeaderDelegate(new AcceptMediaTypeHeaderDelegate());
        addHeaderDelegate(new StringHeaderDelegate());
        addHeaderDelegate(new URIHeaderDelegate());
        addHeaderDelegate(new LocaleHeaderDelegate());
        addHeaderDelegate(new RangeHeaderDelegate());
    }

    public void addHeaderDelegate(HeaderDelegate<?> header) {
        headerDelegates.put(getHeaderType(header), header);
    }

    private Class<?> getHeaderType(HeaderDelegate<?> headerDelegate) {
        Class<?> eventType = null;
        Class<?> clazz = headerDelegate.getClass();
        while (clazz != null && eventType == null) {
            for (Type type : clazz.getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    final ParameterizedType parameterizedType = (ParameterizedType)type;
                    final Type rawType = parameterizedType.getRawType();
                    if (HeaderDelegate.class == rawType) {
                        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length == 1) {
                            if (typeArguments[0] instanceof Class) {
                                eventType = (Class)typeArguments[0];
                            }
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (eventType == null) {
            throw new IllegalArgumentException(String.format("Unable determine type of headers processed by %s", headerDelegate));
        }
        return eventType;
    }

    /** End Points is not supported. {@inheritDoc} */
    @Override
    public <T> T createEndpoint(Application applicationConfig, Class<T> type) {
        throw new UnsupportedOperationException("End Points is not supported");
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        return headerDelegates.get(type);
    }


    @Override
    public Link.Builder createLinkBuilder() {
        return new LinkBuilderImpl();
    }


    @Override
    public ResponseBuilder createResponseBuilder() {
        return new ResponseImpl.ResponseBuilderImpl();
    }


    @Override
    public UriBuilder createUriBuilder() {
        return new UriBuilderImpl();
    }


    @Override
    public VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilderImpl();
    }
}
