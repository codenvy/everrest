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

import org.everrest.core.resource.ResourceMethodDescriptor;

import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface AsynchronousJob {
    Long getJobId();

    long getExpirationDate();

    ResourceMethodDescriptor getResourceMethod();

    boolean isDone();

    boolean cancel();

    /**
     * Get result of job. If job is not done yet this method throws IllegalStateException.
     * Before call this method caller must check is job done or not with method {@link #isDone()} .
     *
     * @return result
     * @throws IllegalStateException
     *         if job is not done yet
     */
    Object getResult() throws IllegalStateException;

    /**
     * The storage for context attributes.
     *
     * @return map never <code>null</code>
     */
    Map<String, Object> getContext();
}