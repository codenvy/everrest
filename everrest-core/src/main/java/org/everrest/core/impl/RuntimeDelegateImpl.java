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

import org.everrest.core.impl.header.AcceptLanguageHeaderDelegate;
import org.everrest.core.impl.header.AcceptMediaTypeHeaderDelegate;
import org.everrest.core.impl.header.CacheControlHeaderDelegate;
import org.everrest.core.impl.header.CookieHeaderDelegate;
import org.everrest.core.impl.header.DateHeaderDelegate;
import org.everrest.core.impl.header.EntityTagHeaderDelegate;
import org.everrest.core.impl.header.LinkHeaderDelegate;
import org.everrest.core.impl.header.LocaleHeaderDelegate;
import org.everrest.core.impl.header.MediaTypeHeaderDelegate;
import org.everrest.core.impl.header.NewCookieHeaderDelegate;
import org.everrest.core.impl.header.RangesHeaderDelegate;
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

import static com.google.common.base.Preconditions.checkArgument;

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
        addHeaderDelegate(new MediaTypeHeaderDelegate());
        addHeaderDelegate(new CacheControlHeaderDelegate());
        addHeaderDelegate(new CookieHeaderDelegate());
        addHeaderDelegate(new NewCookieHeaderDelegate());
        addHeaderDelegate(new EntityTagHeaderDelegate());
        addHeaderDelegate(new DateHeaderDelegate());
        addHeaderDelegate(new AcceptLanguageHeaderDelegate());
        addHeaderDelegate(new AcceptMediaTypeHeaderDelegate());
        addHeaderDelegate(new StringHeaderDelegate());
        addHeaderDelegate(new URIHeaderDelegate());
        addHeaderDelegate(new LocaleHeaderDelegate());
        addHeaderDelegate(new RangesHeaderDelegate());
        addHeaderDelegate(new LinkHeaderDelegate());
    }

    public void addHeaderDelegate(HeaderDelegate<?> headerDelegate) {
        headerDelegates.put(getHeaderType(headerDelegate), headerDelegate);
    }

    private Class<?> getHeaderType(HeaderDelegate<?> headerDelegate) {
        Class<?> typeSupportedByHeaderDelegate = null;
        Class<?> headerDelegateClass = headerDelegate.getClass();
        while (headerDelegateClass != null && typeSupportedByHeaderDelegate == null) {
            for (Type genericType : headerDelegateClass.getGenericInterfaces()) {
                if (genericType instanceof ParameterizedType) {
                    final ParameterizedType parameterizedType = (ParameterizedType)genericType;
                    final Type rawType = parameterizedType.getRawType();
                    if (HeaderDelegate.class == rawType) {
                        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length == 1) {
                            if (actualTypeArguments[0] instanceof Class) {
                                typeSupportedByHeaderDelegate = (Class)actualTypeArguments[0];
                            }
                        }
                    }
                }
            }
            headerDelegateClass = headerDelegateClass.getSuperclass();
        }
        if (typeSupportedByHeaderDelegate == null) {
            throw new IllegalArgumentException(String.format("Unable determine type of headers processed by %s", headerDelegate));
        }
        return typeSupportedByHeaderDelegate;
    }

    /** End Points is not supported. {@inheritDoc} */
    @Override
    public <T> T createEndpoint(Application applicationConfig, Class<T> type) {
        throw new UnsupportedOperationException("End Points is not supported");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        checkArgument(type != null, "Null type is not supported");
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
