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
package org.everrest.core.method;

import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.WebApplicationException;

/**
 * Can be used for check is {@link GenericMethodResource} can be invoked. For
 * example can be checked permission to invoke method according to annotation
 * JSR-250.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface MethodInvokerFilter {

    /**
     * Check does supplied method can be invoked.
     *
     * @param genericMethodResource
     *         See {@link GenericMethodResource}
     * @throws WebApplicationException
     *         if method can not be invoked cause current
     *         environment context, e.g. for current user, with current request
     *         attributes, etc. Actual context can be obtained as next
     *         {@link ApplicationContextImpl#getCurrent()}.
     *         WebApplicationException should contain Response with corresponded
     *         status and message.
     */
    void accept(GenericMethodResource genericMethodResource) throws WebApplicationException;

}
