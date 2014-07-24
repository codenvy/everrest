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

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CookieTest extends BaseTest {

    public void testToString() {
        Cookie cookie = new Cookie("name", "andrew");
        assertEquals("$Version=1;name=andrew", cookie.toString());

        cookie = new Cookie("name", "andrew", "/exo", "exo.com");
        assertEquals("$Version=1;name=andrew;$Domain=exo.com;$Path=/exo", cookie.toString());
    }

    public void testValueOf() {
        String cookieHeader = "$Version=1;company=exo;$Path=/exo,$Domain=exo.com;";
        Cookie c = Cookie.valueOf(cookieHeader);
        assertEquals(c.getVersion(), 1);
        assertEquals(c.getName(), "company");
        assertEquals(c.getValue(), "exo");
        assertEquals(c.getPath(), "/exo");
        assertEquals(c.getDomain(), "exo.com");
    }

    public void testFromString() {
        String cookieHeader = "name=andrew";
        List<Cookie> cookies = HeaderHelper.parseCookies(cookieHeader);
        assertEquals(cookies.size(), 1);
        Cookie c = cookies.get(0);
        assertEquals(c.getVersion(), 0);
        assertEquals(c.getName(), "name");
        assertEquals(c.getValue(), "andrew");

        cookieHeader = "company=exo,name=andrew";
        cookies = HeaderHelper.parseCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 0);
        assertEquals(c.getName(), "company");
        assertEquals(c.getValue(), "exo");
        c = cookies.get(1);
        assertEquals(c.getVersion(), 0);
        assertEquals(c.getName(), "name");
        assertEquals(c.getValue(), "andrew");

        cookieHeader = "company=exo;name=andrew";
        cookies = HeaderHelper.parseCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 0);
        assertEquals(c.getName(), "company");
        assertEquals(c.getValue(), "exo");
        c = cookies.get(1);
        assertEquals(c.getVersion(), 0);
        assertEquals(c.getName(), "name");
        assertEquals(c.getValue(), "andrew");

        cookieHeader = "$Version=1;company=exo;$Path=/exo,$Domain=exo.com;name=andrew";
        cookies = HeaderHelper.parseCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 1);
        assertEquals(c.getName(), "company");
        assertEquals(c.getValue(), "exo");
        assertEquals(c.getPath(), "/exo");
        assertEquals(c.getDomain(), "exo.com");
        c = cookies.get(1);
        assertEquals(c.getVersion(), 1);
        assertEquals(c.getName(), "name");
        assertEquals(c.getValue(), "andrew");

        cookieHeader = "$Version=1;  company=exo;  $Path=/exo, $Domain=exo.com;name=andrew,  $Domain=exo.org";
        cookies = HeaderHelper.parseCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 1);
        assertEquals(c.getName(), "company");
        assertEquals(c.getValue(), "exo");
        assertEquals(c.getPath(), "/exo");
        assertEquals(c.getDomain(), "exo.com");
        c = cookies.get(1);
        assertEquals(c.getVersion(), 1);
        assertEquals(c.getName(), "name");
        assertEquals(c.getValue(), "andrew");
        assertEquals(c.getDomain(), "exo.org");
    }

    public void testToString2() {
        // NewCookie
        NewCookie cookie = new NewCookie("name", "andrew");
        assertEquals("name=andrew;Version=1", cookie.toString());

        cookie = new NewCookie("name", "andrew", "/exo", "exo.com", 0, "sample comment", 1200, true);
        assertEquals("name=andrew;Version=0;Comment=\"sample comment\";Domain=exo.com;Path=/exo;Max-Age=1200;Secure",
                     cookie.toString());
    }

}
