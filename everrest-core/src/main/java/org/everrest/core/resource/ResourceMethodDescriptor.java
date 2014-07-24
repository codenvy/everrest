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
package org.everrest.core.resource;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Describe resource method. Resource method is method of resource class which
 * has annotation {@link javax.ws.rs.HttpMethod}, e.g. {@link javax.ws.rs.GET}
 * and has not {@link javax.ws.rs.Path} annotation.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResourceMethodDescriptor.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public interface ResourceMethodDescriptor extends ResourceDescriptor, GenericMethodResource {

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
