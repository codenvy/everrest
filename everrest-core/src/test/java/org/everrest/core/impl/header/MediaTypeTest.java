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
