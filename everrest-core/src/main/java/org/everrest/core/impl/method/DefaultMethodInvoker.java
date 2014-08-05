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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;
import org.everrest.core.impl.InternalException;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.util.Logger;
import org.everrest.core.util.Tracer;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Invoker for Resource Method, Sub-Resource Method and SubResource Locator.
 *
 * @author andrew00x
 */
public class DefaultMethodInvoker implements MethodInvoker {
    @SuppressWarnings({"unchecked"})
    public static Object[] makeMethodParameters(GenericMethodResource methodResource, ApplicationContext context) {
        Object[] params = new Object[methodResource.getMethodParameters().size()];
        int i = 0;
        for (MethodParameter mp : methodResource.getMethodParameters()) {
            Annotation a = mp.getAnnotation();
            if (a != null) {
                ParameterResolver<?> pr = ParameterResolverFactory.createParameterResolver(a);
                try {
                    params[i++] = pr.resolve(mp, context);
                } catch (Exception e) {
                    String msg = "Not able resolve method parameter " + mp;
                    Class<?> ac = a.annotationType();
                    if (ac == MatrixParam.class || ac == QueryParam.class || ac == PathParam.class) {
                        throw new WebApplicationException(e, Response.status(Response.Status.NOT_FOUND).entity(msg)
                                                                     .type(MediaType.TEXT_PLAIN).build());
                    }
                    throw new WebApplicationException(e, Response.status(Response.Status.BAD_REQUEST).entity(msg)
                                                                 .type(MediaType.TEXT_PLAIN).build());
                }
            } else {
                InputStream entityStream = context.getContainerRequest().getEntityStream();
                if (entityStream == null) {
                    params[i++] = null;
                } else {
                    MediaType contentType = context.getContainerRequest().getMediaType();

                    MessageBodyReader entityReader =
                            context.getProviders().getMessageBodyReader(mp.getParameterClass(), mp.getGenericType(),
                                                                        mp.getAnnotations(), contentType);
                    if (entityReader == null) {
                        List<String> contentLength =
                                context.getContainerRequest().getRequestHeader(HttpHeaders.CONTENT_LENGTH);
                        int length = 0;
                        if (contentLength != null && contentLength.size() > 0) {
                            try {
                                length = Integer.parseInt(contentLength.get(0));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        if (contentType == null && length == 0) {
                            // If both Content-Length and Content-Type is not set
                            // consider there is no content. In this case we not able
                            // to determine reader required for content but
                            // 'Unsupported Media Type' (415) status looks strange if
                            // there is no content at all.
                            params[i++] = null;
                        } else {
                            String msg =
                                    "Media type " + contentType
                                    + " is not supported. There is no corresponded entity reader for type "
                                    + mp.getParameterClass();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(msg);
                            }
                            throw new WebApplicationException(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                                                                      .entity(msg).type(MediaType.TEXT_PLAIN).build());
                        }
                    } else {
                        try {
                            if (Tracer.isTracingEnabled()) {
                                Tracer.trace("Matched MessageBodyReader for type " + mp.getParameterClass()
                                             + ", media type " + contentType
                                             + " = (" + entityReader + ")");
                            }

                            MultivaluedMap<String, String> headers = context.getContainerRequest().getRequestHeaders();
                            params[i++] =
                                    entityReader.readFrom(mp.getParameterClass(), mp.getGenericType(), mp.getAnnotations(),
                                                          contentType, headers, entityStream);
                        } catch (Exception e) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(e.getMessage(), e);
                            }
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

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(DefaultMethodInvoker.class);


    @Override
    public final Object invokeMethod(Object resource, GenericMethodResource methodResource, ApplicationContext context) {
        beforeInvokeMethod(methodResource, methodResource, context);
        Object[] params = makeMethodParameters(methodResource, context);
        return invokeMethod(resource, methodResource, params, context);
    }

    protected void beforeInvokeMethod(Object resource, GenericMethodResource methodResource, ApplicationContext context) {
        for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getMethodInvokerFilters(context.getPath())) {
            MethodInvokerFilter f = (MethodInvokerFilter)factory.getInstance(context);
            f.accept(methodResource);
        }
    }

    public Object invokeMethod(Object resource, GenericMethodResource methodResource, Object[] params,
                               ApplicationContext context) {
        try {
            return methodResource.getMethod().invoke(resource, params);
        } catch (IllegalArgumentException argExc) {
            // Should not be thrown.
            throw new InternalException(argExc);
        } catch (IllegalAccessException accessExc) {
            // Should not be thrown.
            throw new InternalException(accessExc);
        } catch (InvocationTargetException invExc) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(invExc.getMessage(), invExc);
            }

            // Get cause of exception that method produces.
            Throwable cause = invExc.getCause();

            // If WebApplicationException than it may contain response.
            if (cause instanceof WebApplicationException) {
                throw (WebApplicationException)cause;
            }

            // InternalException may be thrown by some internal service but should never be thrown by custom services.
            if (cause instanceof InternalException) {
                throw (InternalException)cause;
            }

            throw new InternalException(cause);
        }
    }
}
