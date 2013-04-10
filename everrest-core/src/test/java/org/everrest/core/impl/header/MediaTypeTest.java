/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.header;

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class MediaTypeTest extends BaseTest {

    public void testToString() {
        MediaType mime = new MediaType("text", "plain");
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();

        assertEquals("text/plain", hd.toString(mime));
    }

    public void testToString2() {
        HashMap<String, String> p = new HashMap<String, String>();
        p.put("charset", "utf8");
        MediaType mime = new MediaType("text", "plain", p);
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();

        assertEquals("text/plain;charset=utf8", hd.toString(mime));
    }

    public void testFromString() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();

        String header = "text";
        MediaType mime = hd.fromString(header);
        assertEquals(0, mime.getParameters().size());
        assertEquals("text", mime.getType());
        assertEquals("*", mime.getSubtype());

        header = "text/plain";
        mime = hd.fromString(header);
        assertEquals(0, mime.getParameters().size());
        assertEquals("text", mime.getType());
        assertEquals("plain", mime.getSubtype());
    }

    public void testFromString2() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();

        String header = "text;charset =     utf8";
        MediaType mime = hd.fromString(header);
        assertEquals(1, mime.getParameters().size());
        assertEquals("utf8", mime.getParameters().get("charset"));
        assertEquals("text", mime.getType());
        assertEquals("*", mime.getSubtype());

        header = "text/plain;   charset   =  utf-8  ;  test=hello";
        mime = hd.fromString(header);
        assertEquals(2, mime.getParameters().size());
        assertEquals("utf-8", mime.getParameters().get("charset"));
        assertEquals("hello", mime.getParameters().get("test"));
        assertEquals("text", mime.getType());
        assertEquals("plain", mime.getSubtype());
    }

    public void testNoMediaType() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        String header = "; charset=utf8";
        MediaType mime = hd.fromString(header);
        assertEquals("utf8", mime.getParameters().get("charset"));
        assertEquals("*", mime.getType());
        assertEquals("*", mime.getSubtype());
    }
}
