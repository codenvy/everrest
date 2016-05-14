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
package org.everrest.core.impl.async;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.method.DefaultMethodInvoker;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.resource.ResourceMethodDescriptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Invoker for Resource and Sub-Resource methods. This invoker does not process methods by oneself but post asynchronous job in
 * AsynchronousJobPool. As result method {@link #invokeMethod(Object, GenericResourceMethod, Object[], ApplicationContext)} returns status
 * 202 if job successfully added in AsynchronousJobPool or response with error status (500) if job can't be accepted by AsynchronousJobPool
 * (e.g. if pool is overloaded). If job is accepted for execution then response includes a pointer to a result in Location header and in
 * entity.
 *
 * @author andrew00x
 */
public class AsynchronousMethodInvoker extends DefaultMethodInvoker {
    private final AsynchronousJobPool pool;

    public AsynchronousMethodInvoker(AsynchronousJobPool pool, ParameterResolverFactory parameterResolverFactory) {
        super(parameterResolverFactory);
        this.pool = pool;
    }

    @Override
    public Object invokeMethod(Object resource,
                               GenericResourceMethod methodResource,
                               Object[] params,
                               ApplicationContext context) {
        try {
            // NOTE. Parameter methodResource never is SubResourceLocatorDescriptor.
            // Resource locators can't be processed in asynchronous mode since it is not end point of request.
            return pool.addJob(resource, (ResourceMethodDescriptor)methodResource, params);
        } catch (AsynchronousJobRejectedException e) {
            throw new WebApplicationException(Response.serverError().entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build());
        }
    }
}
