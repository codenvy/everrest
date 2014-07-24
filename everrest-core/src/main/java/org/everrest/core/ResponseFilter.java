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
 * Process the original {@link GenericContainerResponse} before pass it for
 * serialization to environment, e. g. servlet container. NOTE this filter must
 * not be used directly, it is part of REST framework.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ResponseFilter {

    /**
     * Can modify original response.
     *
     * @param response
     *         the response from resource
     */
    void doFilter(GenericContainerResponse response);

}
