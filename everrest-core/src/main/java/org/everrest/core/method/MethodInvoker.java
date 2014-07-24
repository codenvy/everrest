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

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.resource.GenericMethodResource;

/**
 * Invoke resource methods.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see GenericMethodResource
 */
public interface MethodInvoker {

    /**
     * Invoke supplied method and return result of method invoking.
     *
     * @param resource
     *         object that contains method
     * @param genericMethodResource
     *         See {@link GenericMethodResource}
     * @param context
     *         See {@link ApplicationContextImpl}
     * @return result of method invoking
     */
    Object invokeMethod(Object resource, GenericMethodResource genericMethodResource, ApplicationContext context);

}
