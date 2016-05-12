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

import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.uri.UriPattern;

/**
 * Description of filter.
 *
 * @author andrew00x
 * @see Filter
 * @see RequestFilter
 * @see ResponseFilter
 * @see MethodInvokerFilter
 */
public interface FilterDescriptor extends ObjectModel {

    /** @return See {@link PathValue} */
    PathValue getPathValue();

    /**
     * UriPattern build in same manner as for resources. For detail see section
     * 3.4 URI Templates in JAX-RS specification.
     *
     * @return See {@link UriPattern}
     */
    UriPattern getUriPattern();

}
