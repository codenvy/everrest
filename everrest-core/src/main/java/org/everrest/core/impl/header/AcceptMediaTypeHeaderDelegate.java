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

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AcceptMediaTypeHeaderDelegate.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class AcceptMediaTypeHeaderDelegate extends AbstractHeaderDelegate<AcceptMediaType> {
    /** {@inheritDoc} */
    @Override
    public Class<AcceptMediaType> support() {
        return AcceptMediaType.class;
    }

    /** {@inheritDoc} */
    public AcceptMediaType fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        MediaType mediaType = MediaType.valueOf(header);

        return new AcceptMediaType(mediaType.getType(), mediaType.getSubtype(), mediaType.getParameters());

    }

    /** {@inheritDoc} */
    public String toString(AcceptMediaType acceptedMediaType) {
        throw new UnsupportedOperationException("Accepted media type header used only for request.");
    }
}
