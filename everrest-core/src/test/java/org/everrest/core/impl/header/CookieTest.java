/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.List;

/**
 * @author andrew00x
 */
public class CookieTest {

    @Test
    public void testToString() {
        Cookie cookie = new Cookie("name", "andrew");
        Assert.assertEquals("$Version=1;name=andrew", cookie.toString());

        cookie = new Cookie("name", "andrew", "/exo", "exo.com");
        Assert.assertEquals("$Version=1;name=andrew;$Domain=exo.com;$Path=/exo", cookie.toString());
    }

    @Test
    public void testValueOf() {
        String cookieHeader = "$Version=1;company=exo;$Path=/exo,$Domain=exo.com;";
        Cookie c = Cookie.valueOf(cookieHeader);
        Assert.assertEquals(c.getVersion(), 1);
        Assert.assertEquals(c.getName(), "company");
        Assert.assertEquals(c.getValue(), "exo");
        Assert.assertEquals(c.getPath(), "/exo");
        Assert.assertEquals(c.getDomain(), "exo.com");
    }

    @Test
    public void testFromStringSimple() {
        String cookieHeader = "name=andrew";
        List<Cookie> cookies = HeaderHelper.parseCookies(cookieHeader);
        Assert.assertEquals(cookies.size(), 1);
        Cookie c = cookies.get(0);
        Assert.assertEquals(c.getVersion(), 0);
        Assert.assertEquals(c.getName(), "name");
        Assert.assertEquals(c.getValue(), "andrew");
    }

    @Test
    public void testFromStringList() {
        String cookieHeader = "company=exo,name=andrew";
        List<Cookie> cookies = HeaderHelper.parseCookies(cookieHeader);
        Assert.assertEquals(cookies.size(), 2);
        Cookie c = cookies.get(0);
        Assert.assertEquals(c.getVersion(), 0);
        Assert.assertEquals(c.getName(), "company");
        Assert.assertEquals(c.getValue(), "exo");
        c = cookies.get(1);
        Assert.assertEquals(c.getVersion(), 0);
        Assert.assertEquals(c.getName(), "name");
        Assert.assertEquals(c.getValue(), "andrew");
    }

    @Test
    public void testFromStringList2() {
        String cookieHeader = "company=exo;name=andrew";
        List<Cookie> cookies = HeaderHelper.parseCookies(cookieHeader);
        Assert.assertEquals(cookies.size(), 2);
        Cookie c = cookies.get(0);
        Assert.assertEquals(c.getVersion(), 0);
        Assert.assertEquals(c.getName(), "company");
        Assert.assertEquals(c.getValue(), "exo");
        c = cookies.get(1);
        Assert.assertEquals(c.getVersion(), 0);
        Assert.assertEquals(c.getName(), "name");
        Assert.assertEquals(c.getValue(), "andrew");
    }

    @Test
    public void testFromStringComplex() {
        String cookieHeader = "$Version=1;company=exo;$Path=/exo,$Domain=exo.com;name=andrew";
        List<Cookie> cookies = HeaderHelper.parseCookies(cookieHeader);
        Assert.assertEquals(cookies.size(), 2);
        Cookie c = cookies.get(0);
        Assert.assertEquals(c.getVersion(), 1);
        Assert.assertEquals(c.getName(), "company");
        Assert.assertEquals(c.getValue(), "exo");
        Assert.assertEquals(c.getPath(), "/exo");
        Assert.assertEquals(c.getDomain(), "exo.com");
        c = cookies.get(1);
        Assert.assertEquals(c.getVersion(), 1);
        Assert.assertEquals(c.getName(), "name");
        Assert.assertEquals(c.getValue(), "andrew");
    }

    @Test
    public void testFromStringComplexWithWhitespaces() {
        String cookieHeader = "$Version=1;  company=exo;  $Path=/exo, $Domain=exo.com;name=andrew,  $Domain=exo.org";
        List<Cookie> cookies = HeaderHelper.parseCookies(cookieHeader);
        Assert.assertEquals(cookies.size(), 2);
        Cookie c = cookies.get(0);
        Assert.assertEquals(c.getVersion(), 1);
        Assert.assertEquals(c.getName(), "company");
        Assert.assertEquals(c.getValue(), "exo");
        Assert.assertEquals(c.getPath(), "/exo");
        Assert.assertEquals(c.getDomain(), "exo.com");
        c = cookies.get(1);
        Assert.assertEquals(c.getVersion(), 1);
        Assert.assertEquals(c.getName(), "name");
        Assert.assertEquals(c.getValue(), "andrew");
        Assert.assertEquals(c.getDomain(), "exo.org");
    }

    @Test
    public void testToString2() {
        // NewCookie
        NewCookie cookie = new NewCookie("name", "andrew");
        Assert.assertEquals("name=andrew;Version=1", cookie.toString());

        cookie = new NewCookie("name", "andrew", "/exo", "exo.com", 0, "sample comment", 1200, true);
        Assert.assertEquals("name=andrew;Version=0;Comment=\"sample comment\";Domain=exo.com;Path=/exo;Max-Age=1200;Secure",
                            cookie.toString());
    }
}
