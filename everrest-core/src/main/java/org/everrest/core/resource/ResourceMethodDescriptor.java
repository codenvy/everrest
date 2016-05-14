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
package org.everrest.core.resource;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Describe resource method. Resource method is method of resource class which has annotation {@link javax.ws.rs.HttpMethod},
 * e.g. {@link javax.ws.rs.GET} and has not {@link javax.ws.rs.Path} annotation.
 */
public interface ResourceMethodDescriptor extends GenericResourceMethod {
    /**
     * Get HTTP method name.
     *
     * @return HTTP method name
     */
    String getHttpMethod();

    /**
     * Get list of {@link MediaType} which current method consumes.
     *
     * @return list of media types
     */
    List<MediaType> consumes();

    /**
     * Get list of {@link MediaType} which current method produces.
     *
     * @return list of media types
     */
    List<MediaType> produces();
}
