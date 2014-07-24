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

import javax.ws.rs.core.Cookie;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CookieHeaderDelegate extends AbstractHeaderDelegate<Cookie> {
    /** {@inheritDoc} */
    @Override
    public Class<Cookie> support() {
        return Cookie.class;
    }

    /** {@inheritDoc} */
    public Cookie fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        List<Cookie> l = HeaderHelper.parseCookies(header);
        if (l.size() > 0) // waiting for one cookie
        {
            return l.get(0);
        }

        return null;
    }

    /** {@inheritDoc} */
    public String toString(Cookie cookie) {
        StringBuilder sb = new StringBuilder();

        sb.append("$Version=").append(cookie.getVersion()).append(';');

        sb.append(cookie.getName()).append('=').append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getValue()));

        if (cookie.getDomain() != null) {
            sb.append(';').append("$Domain=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getDomain()));
        }

        if (cookie.getPath() != null) {
            sb.append(';').append("$Path=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getPath()));
        }

        return sb.toString();
    }
}
