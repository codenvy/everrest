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

import org.everrest.core.uri.UriPattern;

import java.util.TreeMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SubResourceLocatorMap extends TreeMap<UriPattern, SubResourceLocatorDescriptor> {
    private static final long serialVersionUID = 89058515637607594L;

    public SubResourceLocatorMap() {
        super(UriPattern.URIPATTERN_COMPARATOR);
    }
}