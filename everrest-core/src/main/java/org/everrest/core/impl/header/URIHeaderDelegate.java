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

import javax.ws.rs.ext.RuntimeDelegate;
import java.net.URI;

/**
 * @author andrew00x
 */
public class URIHeaderDelegate implements RuntimeDelegate.HeaderDelegate<URI> {

    @Override
    public URI fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }
        return URI.create(header);
    }


    @Override
    public String toString(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        return uri.toASCIIString();
    }
}
