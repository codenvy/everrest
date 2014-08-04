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
package org.everrest.core.impl;

import org.everrest.core.impl.header.HeaderHelper;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author andrew00x
 */
public class ResponseImplTest {

    @Test
    public void testSetHeader() throws Exception {
        Response response =
                Response.ok().language(new Locale("en", "GB")).language(new Locale("en", "US")).type(new MediaType("text", "plain"))
                        .location(new URI("http://exoplatform.org/ws/rs/test")).cookie(new NewCookie("name1", "value1"))
                        .tag(new EntityTag("123456789", true)).lastModified(new Date(1000)).build();
        MultivaluedMap<String, Object> headers = response.getMetadata();
        Assert.assertEquals("http://exoplatform.org/ws/rs/test", HeaderHelper.getHeaderAsString(headers.getFirst("location")));
        Assert.assertEquals("name1=value1;Version=1", HeaderHelper.getHeaderAsString(headers.getFirst("set-cookie")));
        Assert.assertEquals("en-us", HeaderHelper.getHeaderAsString(headers.getFirst("content-language")));
        Assert.assertEquals("text/plain", HeaderHelper.getHeaderAsString(headers.getFirst("content-type")));
        Assert.assertEquals("W/\"123456789\"", HeaderHelper.getHeaderAsString(headers.getFirst("etag")));
        Assert.assertEquals("Thu, 01 Jan 1970 00:00:01 GMT", HeaderHelper.getHeaderAsString(headers.getFirst("last-modified")));
    }

    @Test
    public void testVariant() {
        Response response = Response.ok().variant(new Variant(new MediaType("text", "xml"), new Locale("en", "GB"), "UTF-8")).build();
        Assert.assertEquals("text/xml", HeaderHelper.getHeaderAsString(response.getMetadata().getFirst("content-type")));
        Assert.assertEquals("UTF-8", HeaderHelper.getHeaderAsString(response.getMetadata().getFirst("content-encoding")));
        Assert.assertEquals("en-gb", HeaderHelper.getHeaderAsString(response.getMetadata().getFirst("content-language")));
    }

    @Test
    public void testVariants() {
        List<Variant> vs = new ArrayList<>(3);
        vs.add(new Variant(new MediaType("text", "xml"), null, null));
        vs.add(new Variant(null, null, "KOI8-R"));
        vs.add(new Variant(null, new Locale("ru", "RU"), null));
        Response response = Response.ok().variants(vs).build();
        Assert.assertEquals("Accept,Accept-Language,Accept-Encoding", response.getMetadata().getFirst("vary"));
        vs.remove(1);
        response = Response.ok().variants(vs).build();
        Assert.assertEquals("Accept,Accept-Language", response.getMetadata().getFirst("vary"));
        vs.remove(0);
        response = Response.ok().variants(vs).build();
        Assert.assertEquals("Accept-Language", response.getMetadata().getFirst("vary"));
        vs.clear();
        response = Response.ok().variants(vs).build();
        Assert.assertNull(response.getMetadata().getFirst("vary"));
    }

    @Test
    public void testRemoveHeaders() {
        Response response = Response.ok().header("foo", "bar").header("foo", "to be or not to be").header("foo", null).build();
        Assert.assertNull(response.getMetadata().get("foo"));
        response = Response.ok().header("foo2", "bar").header("foo1", "to be or not to be").header("foo2", null).build();
        Assert.assertNull(response.getMetadata().get("foo2"));
        Assert.assertEquals(1, response.getMetadata().get("foo1").size());
        Assert.assertEquals("to be or not to be", response.getMetadata().getFirst("foo1"));
    }

    @Test
    public void testRemoveHeaders2() {
        Response response =
                Response.ok().header("content-type", "text/plain").header("content-type", "text/*").header("content-type", null).build();
        Assert.assertNull(response.getMetadata().get("content-type"));
        response = Response.ok().header("content-type", "text/plain").header("content-length", "10").header("content-type", null)
                           .build();
        Assert.assertNull(response.getMetadata().get("content-type"));
        Assert.assertEquals(1, response.getMetadata().get("content-length").size());
        Assert.assertEquals("10", response.getMetadata().getFirst("content-length"));
    }

    @Test
    public void testRemoveHeadersCookie() {
        Response response =
                Response.ok().header("content-type", "text/plain").entity("entity").header("set-cookie", new NewCookie("name1", "value1"))
                        .cookie((NewCookie[])null).build();
        Assert.assertNull(response.getMetadata().get("Set-Cookie"));
    }
}
