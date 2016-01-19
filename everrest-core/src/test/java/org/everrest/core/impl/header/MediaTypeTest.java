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

import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * @author andrew00x
 */
public class MediaTypeTest {

    @Test
    public void testToString() {
        MediaType mime = new MediaType("text", "plain");
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        Assert.assertEquals("text/plain", hd.toString(mime));
    }

    @Test
    public void testToStringWithCharset() {
        HashMap<String, String> p = new HashMap<>();
        p.put("charset", "utf8");
        MediaType mime = new MediaType("text", "plain", p);
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        Assert.assertEquals("text/plain;charset=utf8", hd.toString(mime));
    }

    @Test
    public void testFromString() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        String header = "text/plain";
        MediaType mime = hd.fromString(header);
        Assert.assertEquals(0, mime.getParameters().size());
        Assert.assertEquals("text", mime.getType());
        Assert.assertEquals("plain", mime.getSubtype());
    }

    @Test
    public void testFromStringNoSubType() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        String header = "text";
        MediaType mime = hd.fromString(header);
        Assert.assertEquals(0, mime.getParameters().size());
        Assert.assertEquals("text", mime.getType());
        Assert.assertEquals("*", mime.getSubtype());
    }

    @Test
    public void testFromStringWithCharset() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        String header = "text;charset =     utf8";
        MediaType mime = hd.fromString(header);
        Assert.assertEquals(1, mime.getParameters().size());
        Assert.assertEquals("utf8", mime.getParameters().get("charset"));
        Assert.assertEquals("text", mime.getType());
        Assert.assertEquals("*", mime.getSubtype());
    }

    @Test
    public void testFromStringWithCharsetAndParameters() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        String header = "text/plain;   charset   =  utf-8  ;  test=hello";
        MediaType mime = hd.fromString(header);
        Assert.assertEquals(2, mime.getParameters().size());
        Assert.assertEquals("utf-8", mime.getParameters().get("charset"));
        Assert.assertEquals("hello", mime.getParameters().get("test"));
        Assert.assertEquals("text", mime.getType());
        Assert.assertEquals("plain", mime.getSubtype());
    }

    @Test
    public void testNoMediaType() throws Exception {
        MediaTypeHeaderDelegate hd = new MediaTypeHeaderDelegate();
        String header = "; charset=utf8";
        MediaType mime = hd.fromString(header);
        Assert.assertEquals("utf8", mime.getParameters().get("charset"));
        Assert.assertEquals("*", mime.getType());
        Assert.assertEquals("*", mime.getSubtype());
    }
}
