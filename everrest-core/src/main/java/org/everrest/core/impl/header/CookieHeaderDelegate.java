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

import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.List;

import static org.everrest.core.impl.header.HeaderHelper.addQuotesIfHasWhitespace;

public class CookieHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Cookie> {

    @Override
    public Cookie fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        List<Cookie> cookies = HeaderHelper.parseCookies(header);
        if (cookies.isEmpty()) {
            return null;
        }
        return cookies.get(0);
    }


    @Override
    public String toString(Cookie cookie) {
        if (cookie == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder();

        sb.append("$Version=").append(cookie.getVersion()).append(';');

        sb.append(cookie.getName()).append('=').append(addQuotesIfHasWhitespace(cookie.getValue()));

        if (cookie.getDomain() != null) {
            sb.append(';').append("$Domain=").append(addQuotesIfHasWhitespace(cookie.getDomain()));
        }

        if (cookie.getPath() != null) {
            sb.append(';').append("$Path=").append(addQuotesIfHasWhitespace(cookie.getPath()));
        }

        return sb.toString();
    }
}
