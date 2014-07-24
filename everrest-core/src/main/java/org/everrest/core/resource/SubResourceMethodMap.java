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

import org.everrest.core.uri.UriPattern;

import java.util.TreeMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SubResourceMethodMap extends TreeMap<UriPattern, ResourceMethodMap<SubResourceMethodDescriptor>> {
    private static final long serialVersionUID = 4083992147354775165L;

    public SubResourceMethodMap() {
        super(UriPattern.URIPATTERN_COMPARATOR);
    }

    public ResourceMethodMap<SubResourceMethodDescriptor> getMethodMap(UriPattern uriPattern) {
        ResourceMethodMap<SubResourceMethodDescriptor> m = get(uriPattern);
        if (m == null) {
            m = new ResourceMethodMap<SubResourceMethodDescriptor>();
            put(uriPattern, m);
        }
        return m;
    }

    public void sort() {
        for (ResourceMethodMap<SubResourceMethodDescriptor> srmd : values()) {
            srmd.sort();
        }
    }
}
