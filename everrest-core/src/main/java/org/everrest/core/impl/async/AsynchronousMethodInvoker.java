/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.core.impl.async;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.method.DefaultMethodInvoker;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.resource.ResourceMethodDescriptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Invoker for Resource and Sub-Resource methods. This invoker does not process methods by oneself but post
 * asynchronous
 * job in AsynchronousJobPool. As result method
 * {@link #invokeMethod(Object, GenericMethodResource, Object[], ApplicationContext)} return status 202 if job
 * successfully added in AsynchronousJobPool or response with error status (500) if job can't be accepted by
 * AsynchronousJobPool (e.g. if pool is overloaded). If job is accepted for execution then response includes a pointer
 * to a result in Location header and in entity.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AsynchronousMethodInvoker extends DefaultMethodInvoker {
    private final AsynchronousJobPool pool;

    public AsynchronousMethodInvoker(AsynchronousJobPool pool) {
        this.pool = pool;
    }

    @Override
    public Object invokeMethod(Object resource,
                               GenericMethodResource methodResource,
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
