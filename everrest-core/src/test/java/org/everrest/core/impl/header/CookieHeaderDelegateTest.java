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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.ws.rs.core.Cookie;
import org.junit.Before;
import org.junit.Test;

public class CookieHeaderDelegateTest {

  private CookieHeaderDelegate cookieHeaderDelegate;

  @Before
  public void setUp() throws Exception {
    cookieHeaderDelegate = new CookieHeaderDelegate();
  }

  @Test
  public void parsesCookieHeader() throws Exception {
    Cookie cookie = cookieHeaderDelegate.fromString("foo=bar");
    assertEquals(new Cookie("foo", "bar"), cookie);
  }

  @Test
  public void parsesCookieHeaderAndReturnsFirstIfMoreThanOneIsParsed() throws Exception {
    Cookie cookie = cookieHeaderDelegate.fromString("foo=bar;xxx=yyy");
    assertEquals(new Cookie("foo", "bar"), cookie);
  }

  @Test
  public void returnsNullIfHeaderDoesNotContainsCookie() throws Exception {
    Cookie cookie = cookieHeaderDelegate.fromString("");
    assertNull(cookie);
  }

  @Test
  public void testToString() throws Exception {
    Cookie cookie = new Cookie("foo", "bar", "/aaa", "andrew.com");
    assertEquals(
        "$Version=1;foo=bar;$Domain=andrew.com;$Path=/aaa", cookieHeaderDelegate.toString(cookie));
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenCookieHeaderIsNull() throws Exception {
    cookieHeaderDelegate.fromString(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenCookieIsNull() throws Exception {
    cookieHeaderDelegate.toString(null);
  }
}
