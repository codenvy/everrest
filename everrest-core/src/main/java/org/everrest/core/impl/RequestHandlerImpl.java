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

import org.everrest.core.ApplicationContext;
import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.RequestFilter;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.UnhandledException;
import org.everrest.core.tools.ErrorPages;
import org.everrest.core.util.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author andrew00x
 */
public class RequestHandlerImpl implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RequestHandlerImpl.class);

    private final RequestDispatcher dispatcher;
    private final ProviderBinder providers;

    public RequestHandlerImpl(RequestDispatcher dispatcher, ProviderBinder providers) {
        checkNotNull(dispatcher);
        checkNotNull(providers);
        this.dispatcher = dispatcher;
        this.providers = providers;
    }

    @Override
    public void handleRequest(GenericContainerRequest request, GenericContainerResponse response) throws IOException {
        final ApplicationContext context = ApplicationContext.getCurrent();

        try {
            for (RequestFilter filter : providers.getRequestFilters(context.getPath())) {
                filter.doFilter(request);
            }
            dispatcher.dispatch(request, response);
            setupInternalResponseHeaders(response.getStatus(), response.getHttpHeaders());
            for (ResponseFilter filter : providers.getResponseFilters(context.getPath())) {
                filter.doFilter(response);
            }
        } catch (Exception e) {
            if (e instanceof WebApplicationException) {
                handleWebApplicationException((WebApplicationException)e, response);
            } else if (e instanceof InternalException) {
                handleInternalException((InternalException)e, response);
            } else {
                throw new UnhandledException(e);
            }
        }

        response.writeResponse();
    }

    @SuppressWarnings({"unchecked"})
    private void handleWebApplicationException(WebApplicationException webApplicationException, GenericContainerResponse response) {
        LOG.debug("WebApplicationException occurs", webApplicationException);
        ErrorPages errorPages = (ErrorPages)EnvironmentContext.getCurrent().get(ErrorPages.class);

        Response errorResponse = webApplicationException.getResponse();
        int errorStatus = errorResponse.getStatus();
        Throwable cause = webApplicationException.getCause();

        propagateErrorIfHaveErrorPage(webApplicationException, errorPages);
        propagateErrorIfHaveErrorPage(cause, errorPages);
        propagateErrorIfHaveErrorPage(errorStatus, errorPages);

        if (Tracer.isTracingEnabled()) {
            Tracer.trace("WebApplicationException occurs, cause = (%s)", cause);
        }

        if (errorResponse.hasEntity()) {
            setupInternalResponseHeaders(errorStatus, errorResponse.getMetadata());
        } else {
            ExceptionMapper exceptionMapper = providers.getExceptionMapper(WebApplicationException.class);
            if (exceptionMapper != null) {
                if (Tracer.isTracingEnabled()) {
                    Tracer.trace("Found ExceptionMapper for WebApplicationException = (%s)", exceptionMapper);
                }
                errorResponse = exceptionMapper.toResponse(webApplicationException);
            } else if (cause != null) {
                if (isNullOrEmpty(cause.getMessage())) {
                    errorResponse = createErrorResponse(errorStatus, cause.toString());
                } else {
                    errorResponse = createErrorResponse(errorStatus, cause.getMessage());
                }
            } else if (!isNullOrEmpty(webApplicationException.getMessage())) {
                errorResponse = createErrorResponse(errorStatus, webApplicationException.getMessage());
            }
        }
        response.setResponse(errorResponse);
    }

    @SuppressWarnings({"unchecked"})
    private void handleInternalException(InternalException internalException, GenericContainerResponse response) {
        LOG.debug("InternalException occurs", internalException);
        ErrorPages errorPages = (ErrorPages)EnvironmentContext.getCurrent().get(ErrorPages.class);

        Throwable cause = internalException.getCause();

        propagateErrorIfHaveErrorPage(internalException, errorPages);
        propagateErrorIfHaveErrorPage(cause, errorPages);

        if (Tracer.isTracingEnabled()) {
            Tracer.trace("InternalException occurs, cause = (%s)", cause);
        }

        ExceptionMapper exceptionMapper = providers.getExceptionMapper(cause.getClass());
        if (exceptionMapper != null) {
            if (Tracer.isTracingEnabled()) {
                Tracer.trace("Found ExceptionMapper for %s = (%s)", cause.getClass(), exceptionMapper);
            }
            response.setResponse(exceptionMapper.toResponse(cause));
        } else {
            throw new UnhandledException(cause);
        }
    }

    private void propagateErrorIfHaveErrorPage(Throwable error, ErrorPages errorPages) {
        if (errorPages != null && error != null && errorPages.hasErrorPage(error)) {
            throw new UnhandledException(error);
        }
    }

    private void propagateErrorIfHaveErrorPage(int errorStatus, ErrorPages errorPages) {
        if (errorPages != null && errorPages.hasErrorPage(errorStatus)) {
            throw new UnhandledException(errorStatus);
        }
    }

    @Deprecated
    private void setupInternalResponseHeaders(int status, MultivaluedMap<String, Object> responseHeaders) {
        if (responseHeaders.getFirst(ExtHttpHeaders.JAXRS_BODY_PROVIDED) == null) {
            String jaxrsHeader = getJaxrsHeader(status);
            if (jaxrsHeader != null) {
                responseHeaders.putSingle(ExtHttpHeaders.JAXRS_BODY_PROVIDED, jaxrsHeader);
            }
        }
    }

    @Override
    public ProviderBinder getProviders() {
        return providers;
    }

    @Override
    public ResourceBinder getResources() {
        return dispatcher.getResources();
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
        return null;
    }
}
