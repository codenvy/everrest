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
package org.everrest.core.impl.header;

import org.everrest.core.header.AbstractHeaderDelegate;

import java.net.URI;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class URIHeaderDelegate extends AbstractHeaderDelegate<URI> {
    /** {@inheritDoc} */
    @Override
    public Class<URI> support() {
        return URI.class;
    }

    /** {@inheritDoc} */
    public URI fromString(String header) {
        return URI.create(header);
    }

    /** {@inheritDoc} */
    public String toString(URI uri) {
        return uri.toASCIIString();
    }
}
