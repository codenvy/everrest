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

import static org.everrest.core.impl.header.HeaderHelper.addQuotesIfHasWhitespace;

import com.google.common.collect.Iterables;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.util.List;

public class CookieHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Cookie> {

  @Override
  public Cookie fromString(String header) {
    if (header == null) {
      throw new IllegalArgumentException();
    }

    List<Cookie> cookies = HeaderHelper.parseCookies(header);
    return Iterables.getFirst(cookies, null);
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
