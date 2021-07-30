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

import static com.google.common.base.Preconditions.checkArgument;
import static org.everrest.core.impl.header.HeaderHelper.addQuotesIfHasWhitespace;
import static org.everrest.core.impl.header.HeaderHelper.parseNewCookie;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;

/** @author andrew00x */
public class NewCookieHeaderDelegate implements RuntimeDelegate.HeaderDelegate<NewCookie> {

  @Override
  public NewCookie fromString(String header) {
    checkArgument(header != null);
    return parseNewCookie(header);
  }

  @Override
  public String toString(NewCookie cookie) {
    checkArgument(cookie != null);
    StringBuilder sb = new StringBuilder();
    sb.append(cookie.getName()).append('=').append(addQuotesIfHasWhitespace(cookie.getValue()));

    sb.append(';').append("Version=").append(cookie.getVersion());

    if (cookie.getComment() != null) {
      sb.append(';').append("Comment=").append(addQuotesIfHasWhitespace(cookie.getComment()));
    }

    if (cookie.getDomain() != null) {
      sb.append(';').append("Domain=").append(addQuotesIfHasWhitespace(cookie.getDomain()));
    }

    if (cookie.getPath() != null) {
      sb.append(';').append("Path=").append(addQuotesIfHasWhitespace(cookie.getPath()));
    }

    if (cookie.getMaxAge() != -1) {
      sb.append(';')
          .append("Max-Age=")
          .append(addQuotesIfHasWhitespace(Integer.toString(cookie.getMaxAge())));
    }

    if (cookie.getExpiry() != null) {
      sb.append(';').append("Expires=");
      sb.append(HeaderHelper.formatDate(cookie.getExpiry()));
    }

    if (cookie.isSecure()) {
      sb.append(';').append("Secure");
    }

    if (cookie.isHttpOnly()) {
      sb.append(';').append("HttpOnly");
    }

    return sb.toString();
  }
}
