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

import java.util.Date;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class DateHeaderDelegate extends AbstractHeaderDelegate<Date> {
    /** {@inheritDoc} */
    @Override
    public Class<Date> support() {
        return Date.class;
    }

    /**
     * Parse date header, header string must be in one of HTTP-date format see
     * {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1" >HTTP/1.1 documentation</a>}
     * otherwise IllegalArgumentException will be thrown. {@inheritDoc}
     */
    public Date fromString(String header) {
        return HeaderHelper.parseDateHeader(header);
    }

    /** Represents {@link Date} as String in format of RFC 1123 {@inheritDoc} . */
    public String toString(Date date) {
        return HeaderHelper.formatDate(date);
    }
}
