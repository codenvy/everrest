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
package org.everrest.core.impl.header;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Date;

/**
 * @author andrew00x
 */
public class DateHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Date> {

    /**
     * Parses date header, header string must be in one of HTTP-date format see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1"
     * >HTTP/1.1 documentation</a> otherwise IllegalArgumentException will be thrown.
     */
    @Override
    public Date fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }
        return HeaderHelper.parseDateHeader(header);
    }

    /** Represents {@link Date} as String in format of RFC 1123. */
    @Override
    public String toString(Date date) {
        if (date == null) {
            throw new IllegalArgumentException();
        }
        return HeaderHelper.formatDate(date);
    }
}
