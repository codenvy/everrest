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
package org.everrest.core.impl;

import org.everrest.core.impl.ResponseImpl.ResponseBuilderImpl;
import org.junit.Test;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ResponseBuilderImplTest {

    @Test
    public void setsNamedHeaders() throws Exception {
        CacheControl cacheControl = new CacheControl();
        String encoding = "gzip";
        URI contentLocation = URI.create("http://localhost:8080/foo");
        NewCookie[] cookies = new NewCookie[] {new NewCookie("bar", "foo"), new NewCookie("foo", "bar")};
        String[] allow = new String[] {"GET", "POST", "PUT"};
        Date expires = new SimpleDateFormat("yyyy/MM/dd").parse("2010/01/08");
        Locale language = new Locale("en", "US");
        URI location = URI.create("http://localhost:8080/bar");
        Date lastModified = new SimpleDateFormat("yyyy/MM/dd").parse("2006/10/28");
        EntityTag tag = new EntityTag("foo");
        MediaType type = new MediaType("text", "plain");
        Link[] links = new Link[]{Link.fromUri(URI.create("http://localhost:8080/bar")).build()};
        Response response = new ResponseBuilderImpl()
                .cacheControl(cacheControl)
                .encoding(encoding)
                .contentLocation(contentLocation)
                .cookie(cookies)
                .allow(allow)
                .expires(expires)
                .language(language)
                .location(location)
                .lastModified(lastModified)
                .tag(tag)
                .type(type)
                .links(links)
                .build();
        assertEquals(newArrayList(cacheControl), response.getHeaders().get("Cache-Control"));
        assertEquals(newArrayList(encoding), response.getHeaders().get("Content-Encoding"));
        assertEquals(newArrayList(contentLocation), response.getHeaders().get("Content-Location"));
        assertEquals(newArrayList(cookies), response.getHeaders().get("Set-Cookie"));
        assertEquals(newArrayList(allow), response.getHeaders().get("Allow"));
        assertEquals(newArrayList(expires), response.getHeaders().get("Expires"));
        assertEquals(newArrayList(language), response.getHeaders().get("Content-Language"));
        assertEquals(newArrayList(lastModified), response.getHeaders().get("Last-Modified"));
        assertEquals(newArrayList(location), response.getHeaders().get("Location"));
        assertEquals(newArrayList(tag), response.getHeaders().get("ETag"));
        assertEquals(newArrayList(type), response.getHeaders().get("Content-type"));
        assertEquals(newArrayList(links), response.getHeaders().get("Link"));
    }

    @Test
    public void setsVariant() {
        MediaType mediaType = new MediaType("text", "xml");
        Locale language = new Locale("en", "GB");
        String encoding = "UTF-8";
        Response response = new ResponseBuilderImpl().status(200).variant(new Variant(mediaType, language, encoding)).build();
        assertEquals(mediaType, response.getMetadata().getFirst("content-type"));
        assertEquals(encoding, response.getMetadata().getFirst("content-encoding"));
        assertEquals(language, response.getMetadata().getFirst("content-language"));
    }

    @Test
    public void setsNullVariant() {
        MediaType mediaType = new MediaType("text", "xml");
        Locale language = new Locale("en", "GB");
        String encoding = "UTF-8";
        Response response = new ResponseBuilderImpl().status(200)
                                    .type(mediaType)
                                    .encoding(encoding)
                                    .language(language)
                                    .variant(null).build();
        assertNull(response.getMetadata().getFirst("content-type"));
        assertNull(response.getMetadata().getFirst("content-encoding"));
        assertNull(response.getMetadata().getFirst("content-language"));
    }

    @Test
    public void setsListOfVariants() {
        List<Variant> variants = new ArrayList<>(3);
        variants.add(new Variant(new MediaType("text", "xml"), (String)null, null));
        variants.add(new Variant(null, (String)null, "KOI8-R"));
        variants.add(new Variant(null, new Locale("ru", "RU"), null));
        Response response = new ResponseBuilderImpl().status(200).variants(variants).build();
        assertEquals("Accept,Accept-Language,Accept-Encoding", response.getMetadata().getFirst("vary"));
        variants.remove(1);
        response = new ResponseBuilderImpl().status(200).variants(variants).build();
        assertEquals("Accept,Accept-Language", response.getMetadata().getFirst("vary"));
        variants.remove(0);
        response = new ResponseBuilderImpl().status(200).variants(variants).build();
        assertEquals("Accept-Language", response.getMetadata().getFirst("vary"));
    }

    @Test
    public void setsEmptyListOfVariants() {
        Response response = new ResponseBuilderImpl().status(200)
                                    .variants(new Variant(new MediaType("text", "xml"), (String)null, null))
                                    .variants(newArrayList()).build();
        assertEquals("Accept", response.getMetadata().getFirst("vary"));
    }

    @Test
    public void setsNullListOfVariants() {
        Response response = new ResponseBuilderImpl().status(200)
                                    .variant(new Variant(new MediaType("text", "xml"), (String)null, null))
                                    .variants((List<Variant>)null).build();
        assertNull(response.getMetadata().getFirst("vary"));
    }

    @Test
    public void setsLanguageAsString() {
        Response response = new ResponseBuilderImpl()
                .language("en-gb")
                .build();
        assertEquals(newArrayList("en-gb"), response.getHeaders().get("Content-Language"));
    }

    @Test
    public void setsContentTypeAsString() {
        Response response = new ResponseBuilderImpl()
                .type("text/plain")
                .build();
        assertEquals(newArrayList("text/plain"), response.getHeaders().get("Content-type"));
    }

    @Test
    public void setsETagHeaderAsString() {
        String tag = "\"foo\"";
        Response response = new ResponseBuilderImpl()
                .tag(tag)
                .build();
        assertEquals(newArrayList(tag), response.getHeaders().get("ETag"));
    }

    @Test
    public void setsLinkHeaderByUriAndRelation() {
        Response response = new ResponseBuilderImpl()
                .link(URI.create("http://localhost:8080/bar"), "foo")
                .build();
        Link expectedLink = Link.fromUri(URI.create("http://localhost:8080/bar")).rel("foo").build();
        assertEquals(newArrayList(expectedLink), response.getHeaders().get("link"));
    }

    @Test
    public void setsLinkHeaderByUriStringAndRelation() {
        Response response = new ResponseBuilderImpl()
                .link("http://localhost:8080/bar", "foo")
                .build();
        Link expectedLink = Link.fromUri("http://localhost:8080/bar").rel("foo").build();
        assertEquals(newArrayList(expectedLink), response.getHeaders().get("link"));
    }

    @Test
    public void replacesAllHeaders() {
        MultivaluedMap<String, Object> replacement = new MultivaluedHashMap<>();
        replacement.putSingle("foo1", "bar1");
        replacement.putSingle("foo2", "bar2");
        Response response = new ResponseBuilderImpl().status(200).header("foo", "bar").replaceAll(replacement).build();

        assertEquals(replacement, response.getHeaders());
    }

    @Test
    public void removesAllHeaders() {
        Response response = Response.ok().header("foo", "bar").replaceAll(null).build();

        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    public void clonesResponseBuilder() throws Exception {
        Annotation[] entityAnnotations = new Annotation[] {mock(Annotation.class)};

        ResponseBuilder responseBuilder = new ResponseBuilderImpl().status(200).entity("foo", entityAnnotations).header("a", "b");
        ResponseBuilder responseBuilderClone = responseBuilder.clone();

        Response response = responseBuilder.build();
        Response responseClone = responseBuilderClone.build();

        assertEquals(response.getStatus(), responseClone.getStatus());
        assertEquals(response.getEntity(), responseClone.getEntity());
        assertArrayEquals(((ResponseImpl)response).getEntityAnnotations(), ((ResponseImpl)responseClone).getEntityAnnotations());
        assertEquals(response.getHeaders(), responseClone.getHeaders());
    }

    @Test
    public void removesCacheControlHeaderWithNullValue() {
        CacheControl cacheControl = new CacheControl();
        Response response = new ResponseBuilderImpl()
                .cacheControl(cacheControl)
                .cacheControl(null)
                .build();
        assertNull(response.getHeaders().get("Cache-Control"));
    }

    @Test
    public void removesEncodingHeaderWithNullValue() {
        String encoding = "gzip";
        Response response = new ResponseBuilderImpl()
                .encoding(encoding)
                .encoding(null)
                .build();
        assertNull(response.getHeaders().get("Content-Encoding"));
    }

    @Test
    public void removesContentLocationHeaderByNullValue() {
        URI contentLocation = URI.create("http://localhost:8080/foo");
        Response response = new ResponseBuilderImpl()
                .contentLocation(contentLocation)
                .contentLocation(null)
                .build();
        assertNull(response.getHeaders().get("Content-Location"));
    }

    @Test
    public void removesAllowHeaderByNullValue() {
        String[] allow = new String[] {"GET", "POST", "PUT"};
        Response response = new ResponseBuilderImpl()
                .allow(allow)
                .allow((String[])null)
                .build();
        assertNull(response.getHeaders().get("Allow"));
    }

    @Test
    public void setsAllowHeaderBySet() {
        Set<String> allow = newHashSet("GET", "POST", "PUT");
        Response response = new ResponseBuilderImpl()
                .allow(allow)
                .build();
        assertEquals(newArrayList(allow), response.getHeaders().get("Allow"));
    }

    @Test
    public void removesAllowHeaderByNullSet() {
        String[] allow = new String[] {"GET", "POST", "PUT"};
        Response response = new ResponseBuilderImpl()
                .allow(allow)
                .allow((Set<String>)null)
                .build();
        assertNull(response.getHeaders().get("Allow"));
    }

    @Test
    public void removesExpiresByNullValue() throws Exception {
        Date expires = new SimpleDateFormat("yyyy/MM/dd").parse("2010/01/08");
        Response response = new ResponseBuilderImpl()
                .expires(expires)
                .expires(null)
                .build();
        assertNull(response.getHeaders().get("Expires"));
    }

    @Test
    public void removesHeaderByNullValue() {
        Response response = new ResponseBuilderImpl()
                .header("foo", "bar")
                .header("foo", null)
                .build();
        assertNull(response.getHeaders().get("foo"));
    }

    @Test
    public void removesLanguageHeaderByNullLocale() {
        Locale language = new Locale("en");
        Response response = new ResponseBuilderImpl()
                .language(language)
                .language((Locale)null)
                .build();
        assertNull(response.getHeaders().get("Content-Language"));
    }

    @Test
    public void removesLanguageHeaderByNullString() {
        Response response = new ResponseBuilderImpl()
                .language("en-gb")
                .language((String)null)
                .build();
        assertNull(response.getHeaders().get("Content-Language"));
    }

    @Test
    public void removesLastModifiedHeaderByNullValue() throws Exception {
        Date lastModified = new SimpleDateFormat("yyyy/MM/dd").parse("2006/10/28");
        Response response = new ResponseBuilderImpl()
                .lastModified(lastModified)
                .lastModified(null)
                .build();
        assertNull(response.getHeaders().get("Last-Modified"));
    }

    @Test
    public void removesLocationHeaderNyNullValue() {
        URI location = URI.create("http://localhost:8080/bar");
        Response response = new ResponseBuilderImpl()
                .location(location)
                .location(null)
                .build();
        assertNull(response.getHeaders().get("Location"));
    }

    @Test
    public void removesETagHeaderByNullString() {
        String tag = "\"foo\"";
        Response response = new ResponseBuilderImpl()
                .tag(tag)
                .tag((String)null)
                .build();
        assertNull(response.getHeaders().get("ETag"));
    }

    @Test
    public void removesETagHeaderByNullEntityTag() {
        EntityTag tag = new EntityTag("foo");
        Response response = new ResponseBuilderImpl()
                .tag(tag)
                .tag((EntityTag)null)
                .build();
        assertNull(response.getHeaders().get("ETag"));
    }

    @Test
    public void removesContentTypeByNullMediaType() {
        MediaType mediaType = new MediaType("text", "plain");
        Response response = new ResponseBuilderImpl()
                .type(mediaType)
                .type((MediaType)null)
                .build();
        assertNull(response.getHeaders().get("Content-type"));
    }

    @Test
    public void removesContentTypeByNullString() {
        Response response = new ResponseBuilderImpl()
                .type("text/plain")
                .type((String)null)
                .build();
        assertNull(response.getHeaders().get("Content-type"));
    }

    @Test
    public void removesLinksByNullValues() {
        Link[] links = new Link[]{Link.fromUri(URI.create("http://localhost:8080/bar")).build()};
        Response response = new ResponseBuilderImpl()
                .links(links)
                .links((Link[])null)
                .build();
        assertNull(response.getHeaders().get("Link"));
    }
}