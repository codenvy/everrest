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
package org.everrest.core.impl.header;

import org.everrest.core.impl.uri.LinkBuilderImpl;

import javax.ws.rs.core.Link;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

public class LinkHeaderDelegate implements HeaderDelegate<Link>{
    @Override
    public Link fromString(String value) {
        return new LinkBuilderImpl().link(value).build();
    }

    @Override
    public String toString(Link value) {
        return value.toString();
    }
}
