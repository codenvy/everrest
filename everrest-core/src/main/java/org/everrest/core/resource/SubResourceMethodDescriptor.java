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

import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Path;

/**
 * Describe sub-resource method. Sub-resource method is {@link java.lang.reflect.Method} of resource class which has own {@link Path}
 * annotation and {@link javax.ws.rs.HttpMethod} annotation. This method can't handle request directly.
 */
public interface SubResourceMethodDescriptor extends ResourceMethodDescriptor {
    /** @return {@link PathValue} */
    PathValue getPathValue();

    /** @return {@link UriPattern} */
    UriPattern getUriPattern();
}
