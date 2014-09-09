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
package org.everrest.guice;

import org.everrest.core.impl.uri.UriBuilderImpl;

import javax.ws.rs.core.UriBuilder;

/**
 * Allows to use service proxy classes which are created by guice for interceptors.
 * @author Max Shaposhnik
 */
public class GuiceUriBuilderImpl extends UriBuilderImpl {

    private static final String PROXY_MARKER = "$EnhancerByGuice$";

    public GuiceUriBuilderImpl() {
        super();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public UriBuilder path(Class resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is null");
        }

        if  (resource.getName().contains(PROXY_MARKER)) {
            return super.path(resource.getSuperclass());
        }
        return super.path(resource);
    }

    @Override
    public UriBuilder path(Class resource, String method) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is null");
        }

        if (resource.getName().contains(PROXY_MARKER)) {
            return super.path(resource.getSuperclass(), method);
        }
        return super.path(resource, method);
    }

    protected GuiceUriBuilderImpl(GuiceUriBuilderImpl cloned) {
        super(cloned);
    }

    @Override
    public UriBuilder clone() {
        return new GuiceUriBuilderImpl(this);
    }



}
