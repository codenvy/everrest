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
package org.everrest.core.impl;

import org.everrest.core.impl.header.HeaderHelper;

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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ResponseImplTest extends BaseTest {

    public void testSetHeader() throws Exception {
        Response response =
                Response.ok().language(new Locale("en", "GB")).language(new Locale("en", "US")).type(
                        new MediaType("text", "plain")).location(new URI("http://exoplatform.org/ws/rs/test")).cookie(
                        new NewCookie("name1", "value1")).tag(new EntityTag("123456789", true)).lastModified(new Date(1000))
                        .build();
        MultivaluedMap<String, Object> headers = response.getMetadata();
        assertEquals("http://exoplatform.org/ws/rs/test", HeaderHelper.getHeaderAsString(headers.getFirst("location")));
        assertEquals("name1=value1;Version=1", HeaderHelper.getHeaderAsString(headers.getFirst("set-cookie")));
        assertEquals("en-us", HeaderHelper.getHeaderAsString(headers.getFirst("content-language")));
        assertEquals("text/plain", HeaderHelper.getHeaderAsString(headers.getFirst("content-type")));
        assertEquals("W/\"123456789\"", HeaderHelper.getHeaderAsString(headers.getFirst("etag")));
        assertEquals("Thu, 01 Jan 1970 00:00:01 GMT", HeaderHelper.getHeaderAsString(headers.getFirst("last-modified")));
    }

    public void testVariant() {
        Response response =
                Response.ok().variant(new Variant(new MediaType("text", "xml"), new Locale("en", "GB"), "UTF-8")).build();
        assertEquals("text/xml", HeaderHelper.getHeaderAsString(response.getMetadata().getFirst("content-type")));
        assertEquals("UTF-8", HeaderHelper.getHeaderAsString(response.getMetadata().getFirst("content-encoding")));
        assertEquals("en-gb", HeaderHelper.getHeaderAsString(response.getMetadata().getFirst("content-language")));
    }

    public void testVariants() {
        List<Variant> vs = new ArrayList<Variant>(3);
        vs.add(new Variant(new MediaType("text", "xml"), null, null));
        vs.add(new Variant(null, null, "KOI8-R"));
        vs.add(new Variant(null, new Locale("ru", "RU"), null));
        Response response = Response.ok().variants(vs).build();
        assertEquals("Accept,Accept-Language,Accept-Encoding", response.getMetadata().getFirst("vary"));
        vs.remove(1);
        response = Response.ok().variants(vs).build();
        assertEquals("Accept,Accept-Language", response.getMetadata().getFirst("vary"));
        vs.remove(0);
        response = Response.ok().variants(vs).build();
        assertEquals("Accept-Language", response.getMetadata().getFirst("vary"));
        vs.clear();
        response = Response.ok().variants(vs).build();
        assertNull(response.getMetadata().getFirst("vary"));
    }

    public void testRemoveHeaders() {
        Response response =
                Response.ok().header("foo", "bar").header("foo", "to be or not to be").header("foo", null).build();
        assertNull(response.getMetadata().get("foo"));
        response = Response.ok().header("foo2", "bar").header("foo1", "to be or not to be").header("foo2", null).build();
        assertNull(response.getMetadata().get("foo2"));
        assertEquals(1, response.getMetadata().get("foo1").size());
        assertEquals("to be or not to be", response.getMetadata().getFirst("foo1"));
    }

    public void testRemoveHeaders2() {
        Response response =
                Response.ok().header("content-type", "text/plain").header("content-type", "text/*").header("content-type",
                                                                                                           null).build();
        assertNull(response.getMetadata().get("content-type"));
        response =
                Response.ok().header("content-type", "text/plain").header("content-length", "10").header("content-type", null)
                        .build();
        assertNull(response.getMetadata().get("content-type"));
        assertEquals(1, response.getMetadata().get("content-length").size());
        assertEquals("10", response.getMetadata().getFirst("content-length"));
    }

    public void testRemoveHeadersCookie() {
        Response response =
                Response.ok().header("content-type", "text/plain").entity("entity").header("set-cookie",
                                                                                           new NewCookie("name1", "value1"))
                        .cookie((NewCookie[])null).build();
        assertNull(response.getMetadata().get("Set-Cookie"));
    }

}
