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

import javax.ws.rs.Path;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.uri.UriPattern;

/**
 * Describe sub-resource locator. Sub-resource locator is {@link java.lang.reflect.Method} of
 * resource class which has own {@link Path} annotation and has not {@link javax.ws.rs.HttpMethod}
 * annotation. This method can't handle request by self but produce object and this object can
 * handle request or maybe has other resource locators.
 */
public interface SubResourceLocatorDescriptor extends GenericResourceMethod {
  /** @return {@link PathValue} */
  PathValue getPathValue();

  /** @return {@link UriPattern} */
  UriPattern getUriPattern();
}
