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
package org.everrest.core.method;

import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.WebApplicationException;

/**
 * Can be used for check is {@link GenericMethodResource} can be invoked. For example can be checked permission to invoke method according
 * to annotation JSR-250.
 *
 * @author andrew00x
 */
public interface MethodInvokerFilter {

    /**
     * Check does supplied method can be invoked.
     *
     * @param genericMethodResource
     *         See {@link GenericMethodResource}
     * @param params
     *         actual method parameters that were created from request
     * @throws WebApplicationException
     *         if method can not be invoked cause current environment context, e.g. for current user, with current request attributes, etc.
     *         Actual context can be obtained as next {@link ApplicationContextImpl#getCurrent()}. WebApplicationException should contain
     *         Response with corresponded status and message.
     */
    void accept(GenericMethodResource genericMethodResource, Object[] params) throws WebApplicationException;

}
