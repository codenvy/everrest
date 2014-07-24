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

import javax.ws.rs.core.NewCookie;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: NewCookieHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public class NewCookieHeaderDelegate extends AbstractHeaderDelegate<NewCookie> {
    /** {@inheritDoc} */
    @Override
    public Class<NewCookie> support() {
        return NewCookie.class;
    }

    /** {@inheritDoc} */
    public NewCookie fromString(String header) {
        throw new UnsupportedOperationException("NewCookie used only for response headers.");
    }

    /** {@inheritDoc} */
    public String toString(NewCookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append('=').append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getValue()));

        sb.append(';').append("Version=").append(cookie.getVersion());

        if (cookie.getComment() != null) {
            sb.append(';').append("Comment=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getComment()));
        }

        if (cookie.getDomain() != null) {
            sb.append(';').append("Domain=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getDomain()));
        }

        if (cookie.getPath() != null) {
            sb.append(';').append("Path=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getPath()));
        }

        if (cookie.getMaxAge() != -1) {
            sb.append(';').append("Max-Age=").append(HeaderHelper.addQuotesIfHasWhitespace("" + cookie.getMaxAge()));
        }

        if (cookie.isSecure()) {
            sb.append(';').append("Secure");
        }

        return sb.toString();
    }
}
