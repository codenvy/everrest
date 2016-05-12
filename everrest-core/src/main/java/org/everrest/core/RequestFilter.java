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
package org.everrest.core;

/**
 * Process the original {@link GenericContainerRequest} before it dispatch by
 * {@link org.everrest.core.impl.RequestDispatcher}. NOTE this method must be not called directly, it
 * is part of REST framework, otherwise {@link ApplicationContext} may contains
 * wrong parameters.
 *
 * @author andrew00x
 */
public interface RequestFilter {
    /**
     * Can modify original request.
     *
     * @param request
     *         the request
     */
    void doFilter(GenericContainerRequest request);
}
