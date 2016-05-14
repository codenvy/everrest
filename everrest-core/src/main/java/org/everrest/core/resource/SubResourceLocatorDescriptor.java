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
 * Describe sub-resource locator. Sub-resource locator is {@link java.lang.reflect.Method} of resource class which has own {@link Path}
 * annotation and has not {@link javax.ws.rs.HttpMethod} annotation. This method can't handle request by self but produce object and this
 * object can handle request or maybe has other resource locators.
 */
public interface SubResourceLocatorDescriptor extends GenericResourceMethod {
    /** @return {@link PathValue} */
    PathValue getPathValue();

    /** @return {@link UriPattern} */
    UriPattern getUriPattern();
}
