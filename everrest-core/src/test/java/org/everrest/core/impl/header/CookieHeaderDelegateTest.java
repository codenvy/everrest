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

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Cookie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        assertEquals("$Version=1;foo=bar;$Domain=andrew.com;$Path=/aaa", cookieHeaderDelegate.toString(cookie));
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