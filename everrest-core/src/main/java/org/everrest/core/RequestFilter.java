/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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
