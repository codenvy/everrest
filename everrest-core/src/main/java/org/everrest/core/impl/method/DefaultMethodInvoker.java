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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.Parameter;
import org.everrest.core.impl.InternalException;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.util.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.everrest.core.impl.header.HeaderHelper.getContentLengthLong;

/**
 * Invoker for Resource Method, Sub-Resource Method and SubResource Locator.
 *
 * @author andrew00x
 */
public class DefaultMethodInvoker implements MethodInvoker {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMethodInvoker.class);

    private final ParameterResolverFactory parameterResolverFactory;

    public DefaultMethodInvoker(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @Override
    public final Object invokeMethod(Object resource, GenericResourceMethod methodResource, ApplicationContext context) {
        Object[] params = makeMethodParameters(methodResource, context);
        beforeInvokeMethod(resource, methodResource, params, context);
        return invokeMethod(resource, methodResource, params, context);
    }

    @SuppressWarnings({"unchecked"})
    private Object[] makeMethodParameters(GenericResourceMethod resourceMethod, ApplicationContext context) {
        Object[] params = new Object[resourceMethod.getMethodParameters().size()];
        int i = 0;
        for (Parameter methodParameter : resourceMethod.getMethodParameters()) {
            Annotation methodParameterAnnotation = methodParameter.getAnnotation();
            if (methodParameterAnnotation != null) {
                ParameterResolver<?> parameterResolver = parameterResolverFactory.createParameterResolver(methodParameterAnnotation);
                try {
                    params[i++] = parameterResolver.resolve(methodParameter, context);
                } catch (Exception e) {
                    String errorMsg = String.format("Not able resolve method parameter %s", methodParameter);
                    Class<?> annotationType = methodParameterAnnotation.annotationType();
                    if (annotationType == MatrixParam.class || annotationType == QueryParam.class || annotationType == PathParam.class) {
                        throw new WebApplicationException(e, Response.status(NOT_FOUND).entity(errorMsg).type(TEXT_PLAIN).build());
                    }
                    throw new WebApplicationException(e, Response.status(BAD_REQUEST).entity(errorMsg).type(TEXT_PLAIN).build());
                }
            } else {
                InputStream entityStream = context.getContainerRequest().getEntityStream();
                if (entityStream == null) {
                    params[i++] = null;
                } else {
                    MediaType contentType = context.getContainerRequest().getMediaType();

                    MessageBodyReader entityReader =
                            context.getProviders().getMessageBodyReader(methodParameter.getParameterClass(), methodParameter.getGenericType(),
                                                                        methodParameter.getAnnotations(), contentType);
                    if (entityReader == null) {
                        long contentLength = 0;
                        try {
                            contentLength = getContentLengthLong(context.getContainerRequest().getRequestHeaders());
                        } catch (NumberFormatException ignored) {
                        }
                        if (contentType == null && contentLength == 0) {
                            params[i++] = null;
                        } else {
                            String msg = String.format("Media type %s is not supported. There is no corresponded entity reader for type %s",
                                                       contentType, methodParameter.getParameterClass());
                            LOG.debug(msg);
                            throw new WebApplicationException(Response.status(UNSUPPORTED_MEDIA_TYPE).entity(msg).type(TEXT_PLAIN).build());
                        }
                    } else {
                        try {
                            if (Tracer.isTracingEnabled()) {
                                Tracer.trace(String.format("Matched MessageBodyReader for type %s, media type %s = (%s)",
                                                           methodParameter.getParameterClass(), contentType, entityReader));
                            }

                            MultivaluedMap<String, String> headers = context.getContainerRequest().getRequestHeaders();
                            params[i++] = entityReader.readFrom(methodParameter.getParameterClass(), methodParameter.getGenericType(),
                                                                methodParameter.getAnnotations(), contentType, headers, entityStream);
                        } catch (Exception e) {
                            LOG.debug(e.getMessage(), e);
                            if (e instanceof WebApplicationException) {
                                throw (WebApplicationException)e;
                            }
                            if (e instanceof InternalException) {
                                throw (InternalException)e;
                            }
                            throw new InternalException(e);
                        }
                    }
                }
            }
        }
        return params;
    }

    protected void beforeInvokeMethod(Object resource, GenericResourceMethod methodResource, Object[] params, ApplicationContext context) {
        for (MethodInvokerFilter filter : context.getProviders().getMethodInvokerFilters(context.getPath())) {
            filter.accept(methodResource, params);
        }
    }

    public Object invokeMethod(Object resource, GenericResourceMethod methodResource, Object[] params, ApplicationContext context) {
        try {
            return methodResource.getMethod().invoke(resource, params);
        } catch (IllegalArgumentException | IllegalAccessException unexpectedException) {
            throw new InternalException(unexpectedException);
        } catch (InvocationTargetException invocationException) {
            LOG.debug(invocationException.getMessage(), invocationException);

            Throwable cause = invocationException.getCause();

            if (cause instanceof WebApplicationException) {
                throw (WebApplicationException)cause;
            }

            if (cause instanceof InternalException) {
                throw (InternalException)cause;
            }

            throw new InternalException(cause);
        }
    }
}
