/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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
