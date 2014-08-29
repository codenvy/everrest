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

import org.everrest.core.ApplicationContext;
import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResponseFilter;
import org.everrest.core.UnhandledException;
import org.everrest.core.tools.ErrorPages;
import org.everrest.core.util.Logger;
import org.everrest.core.util.Tracer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

/**
 * @author andrew00x
 */
public class RequestHandlerImpl implements RequestHandler {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(RequestHandlerImpl.class);

    /** See {@link RequestDispatcher}. */
    private final RequestDispatcher dispatcher;

    /**
     * @param dispatcher
     *         RequestDispatcher
     */
    public RequestHandlerImpl(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }


    @Override
    @SuppressWarnings({"unchecked"})
    public void handleRequest(GenericContainerRequest request, GenericContainerResponse response)
            throws UnhandledException, IOException {
        final ApplicationContext context = ApplicationContextImpl.getCurrent();

        try {
            for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getRequestFilters(context.getPath())) {
                ((RequestFilter)factory.getInstance(context)).doFilter(request);
            }

            dispatcher.dispatch(request, response);

            if (response.getHttpHeaders().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED) == null) {
                String jaxrsHeader = getJaxrsHeader(response.getStatus());
                if (jaxrsHeader != null) {
                    response.getHttpHeaders().putSingle(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
                }
            }

            for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getResponseFilters(context.getPath())) {
                ((ResponseFilter)factory.getInstance(context)).doFilter(response);
            }
        } catch (Exception e) {
            ErrorPages errorPages = (ErrorPages)EnvironmentContext.getCurrent().get(ErrorPages.class);

            if (e instanceof WebApplicationException) {
                Response errorResponse = ((WebApplicationException)e).getResponse();

                int errorStatus = errorResponse.getStatus();
                Throwable cause = e.getCause();
                // Should be some of 4xx status.
                if (errorStatus < 500) {
                    // Warn about error in debug mode only.
                    if (LOG.isDebugEnabled() && cause != null) {
                        LOG.debug("WebApplicationException occurs.", cause);
                    }
                } else {
                    if (cause != null) {
                        LOG.error("WebApplicationException occurs.", cause);
                    }
                }

                if (Tracer.isTracingEnabled()) {
                    Tracer.trace("WebApplicationException occurs, cause = (" + cause + ")");
                }

                if (errorPages != null
                    && (errorPages.hasErrorPage(errorStatus) || (cause != null && errorPages.hasErrorPage(cause.getClass().getName())))) {
                    // If error-page configured in web.xml let this page process error.
                    throw new UnhandledException(e.getCause());
                }

                ExceptionMapper exceptionMapper = context.getProviders().getExceptionMapper(WebApplicationException.class);
                if (errorResponse.getEntity() == null) {
                    if (exceptionMapper != null) {
                        if (Tracer.isTracingEnabled()) {
                            Tracer.trace("Found ExceptionMapper for WebApplicationException = (" + exceptionMapper + ")");
                        }

                        errorResponse = exceptionMapper.toResponse(e);
                    } else if (e.getMessage() != null) {
                        errorResponse = createErrorResponse(errorStatus, e.getMessage());
                    }
                } else {
                    if (errorResponse.getMetadata().getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED) == null) {
                        String jaxrsHeader = getJaxrsHeader(errorStatus);
                        if (jaxrsHeader != null) {
                            errorResponse.getMetadata().putSingle(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
                        }
                    }
                }

                response.setResponse(errorResponse);
            } else if (e instanceof InternalException) {
                Throwable cause = e.getCause();

                if (Tracer.isTracingEnabled()) {
                    Tracer.trace("InternalException occurs, cause = (" + cause + ")");
                }

                if (errorPages != null && errorPages.hasErrorPage(cause.getClass().getName())) {
                    // If error-page configured in web.xml let this page process error.
                    throw new UnhandledException(e.getCause());
                }

                Class causeClazz = cause.getClass();
                ExceptionMapper exceptionMapper = context.getProviders().getExceptionMapper(causeClazz);
                while (causeClazz != null && exceptionMapper == null) {
                    exceptionMapper = context.getProviders().getExceptionMapper(causeClazz);
                    if (exceptionMapper == null) {
                        causeClazz = causeClazz.getSuperclass();
                    }
                }

                if (exceptionMapper != null) {
                    // Hide error message if exception mapper exists.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("InternalException occurs.", cause);
                    }

                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace("Found ExceptionMapper for " + cause.getClass() + " = (" + exceptionMapper + ")");
                    }

                    response.setResponse(exceptionMapper.toResponse(cause));
                } else {
                    LOG.error("InternalException occurs.", cause);
                    throw new UnhandledException(e.getCause());
                }
            } else {
                throw new UnhandledException(e);
            }
        }

        response.writeResponse();
    }

    /**
     * Create error response with specified status and body message.
     *
     * @param status
     *         response status
     * @param message
     *         response message
     * @return response
     */
    private Response createErrorResponse(int status, String message) {

        ResponseBuilder responseBuilder = Response.status(status);
        responseBuilder.entity(message).type(MediaType.TEXT_PLAIN);
        String jaxrsHeader = getJaxrsHeader(status);
        if (jaxrsHeader != null) {
            responseBuilder.header(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
        }
        return responseBuilder.build();
    }

    private String getJaxrsHeader(int status) {
        if (status >= 400) {
            return "Error-Message";
        }
        // Add required behavior here.
        return null;
    }

}
