/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import com.google.common.collect.ImmutableMap;

import org.everrest.core.impl.uri.LinkBuilderImpl;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static javax.ws.rs.core.HttpHeaders.ALLOW;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.DATE;
import static javax.ws.rs.core.HttpHeaders.ETAG;
import static javax.ws.rs.core.HttpHeaders.LAST_MODIFIED;
import static javax.ws.rs.core.HttpHeaders.LINK;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;
import static javax.ws.rs.core.Response.Status.Family.OTHER;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author andrew00x
 */
public class ResponseImplTest {

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void getsContentType() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        MediaType mediaType = new MediaType("text", "plain", ImmutableMap.of("charset", "utf-8"));
        headers.putSingle(CONTENT_TYPE, mediaType);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertSame(mediaType, response.getMediaType());
    }

    @Test
    public void parsesContentTypeHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(CONTENT_TYPE, "text/plain;charset=utf-8");
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(new MediaType("text", "plain", ImmutableMap.of("charset", "utf-8")), response.getMediaType());
    }

    @Test
    public void getsContentLanguage() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        Locale locale = new Locale("en", "GB");
        headers.putSingle(CONTENT_LANGUAGE, locale);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertSame(locale, response.getLanguage());
    }

    @Test
    public void parsesContentLanguageHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(CONTENT_LANGUAGE, "en-GB");
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(new Locale("en", "GB"), response.getLanguage());
    }

    @Test
    public void getsContentLengthMinusOneIfHeaderIsNotSet() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(-1, response.getLength());
    }

    @Test
    public void getsContentLength() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(CONTENT_LENGTH, 3);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(3, response.getLength());
    }

    @Test
    public void parsesContentLengthHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(CONTENT_LENGTH, "3");
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(3, response.getLength());
    }

    @Test
    public void getsAllowedMethods() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(ALLOW, newArrayList("get,Put", null, "POST"));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(newHashSet("GET", "PUT", "POST"), response.getAllowedMethods());
    }

    @Test
    public void getsEmptySetIfAllowHeaderIsNotSet() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(newHashSet(), response.getAllowedMethods());
    }

    @Test
    public void getsSetCookieHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        List<Object> cookiesList = newArrayList(new NewCookie("name", "andrew"), new NewCookie("company", "codenvy", "/path", "codenvy.com", 1, "comment", 300, null, true, true));
        headers.put(SET_COOKIE, cookiesList);

        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);
        Map<String, NewCookie> expectedCookies = ImmutableMap.of("name", new NewCookie("name", "andrew"),
                                                                 "company", new NewCookie("company", "codenvy", "/path", "codenvy.com", 1, "comment", 300, null, true, true));

        assertEquals(expectedCookies, response.getCookies());
    }

    @Test
    public void parsesSetCookieHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        headers.put(SET_COOKIE, newArrayList("name=andrew",
                                             "company=codenvy;version=1;paTh=/path;Domain=codenvy.com;comment=\"comment\";max-age=300;HttpOnly;secure"));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);
        Map<String, NewCookie> expectedCookies = ImmutableMap.of("name", new NewCookie("name", "andrew"),
                                                                 "company", new NewCookie("company", "codenvy", "/path", "codenvy.com", 1, "comment", 300, null, true, true));

        assertEquals(expectedCookies, response.getCookies());
    }

    @Test
    public void getsEmptyMapIfSetCookieHeaderIsNotSet() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(newHashMap(), response.getCookies());
    }

    @Test
    public void getsEntityTag() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        EntityTag entityTag = new EntityTag("bar");
        headers.putSingle(ETAG, entityTag);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertSame(entityTag, response.getEntityTag());
    }

    @Test
    public void parsesEntityTagHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(ETAG, "\"bar\"");
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(new EntityTag("bar"), response.getEntityTag());
    }

    @Test
    public void getsDate() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        Date entityTag = new Date();
        headers.putSingle(DATE, entityTag);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertSame(entityTag, response.getDate());
    }

    @Test
    public void parsesDateHeader() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        Date date = new Date();
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(DATE, dateFormat.format(date));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(dateFormat.format(date), dateFormat.format(response.getDate()));
    }

    @Test
    public void getsLastModified() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        Date entityTag = new Date();
        headers.putSingle(LAST_MODIFIED, entityTag);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertSame(entityTag, response.getLastModified());
    }

    @Test
    public void parsesLastModifiedHeader() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        Date date = new Date();
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(LAST_MODIFIED, dateFormat.format(date));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(dateFormat.format(date), dateFormat.format(response.getLastModified()));
    }

    @Test
    public void getsLocation() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        URI location = new URI("http://localhost:8080/bar");
        headers.putSingle(LOCATION, location);
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertSame(location, response.getLocation());
    }

    @Test
    public void parsesLocationHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(LOCATION, "http://localhost:8080/bar");
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(new URI("http://localhost:8080/bar"), response.getLocation());
    }

    @Test
    public void getsLinks() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        Link link = new LinkBuilderImpl().uri("http://localhost:8080/x/y/z").rel("xxx").build();
        headers.put(LINK, newArrayList(link));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(newHashSet(link), response.getLinks());
    }

    @Test
    public void parsesLinkHeader() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(LINK, newArrayList("< http://localhost:8080/x/y/z  >; rel=xxx"));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(newHashSet(new LinkBuilderImpl().uri("http://localhost:8080/x/y/z").rel("xxx").build()), response.getLinks());
    }

    @Test
    public void getsEmptySetIfLinkHeaderIsNotSet() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(newHashSet(), response.getLinks());
    }

    @Test
    public void checksLinkPresence() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(LINK, newArrayList("< http://localhost:8080/x/y/z  >; rel=xxx"));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertTrue(response.hasLink("xxx"));
        assertFalse(response.hasLink("yyy"));
    }

    @Test
    public void getsLinkByRelation() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(LINK, newArrayList("< http://localhost:8080/x/y/z  >; rel=xxx"));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(new LinkBuilderImpl().uri("http://localhost:8080/x/y/z").rel("xxx").build(), response.getLink("xxx"));
        assertNull(response.getLink("yyy"));
    }

    @Test
    public void getsLinkBuilderByRelation() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(LINK, newArrayList("< http://localhost:8080/x/y/z  >; rel=xxx"));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(new LinkBuilderImpl().uri("http://localhost:8080/x/y/z").rel("xxx").build(), response.getLinkBuilder("xxx").build());
        assertNull(response.getLinkBuilder("yyy"));
    }

    @Test
    public void getsHeadersAsStringToStringMapAndUsesRuntimeDelegateForConvertValuesToString() throws Exception {
        HeaderDelegate<HeaderValue> headerDelegate = mock(HeaderDelegate.class);
        when(headerDelegate.toString(isA(HeaderValue.class))).thenReturn("bar");
        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(HeaderValue.class)).thenReturn(headerDelegate);
        RuntimeDelegate.setInstance(runtimeDelegate);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put("foo", newArrayList(new HeaderValue()));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(ImmutableMap.of("foo", newArrayList("bar")), response.getStringHeaders());
    }

    @Test
    public void getsHeadersAsStringToStringMapAndUsesToStringMethodOfValueToConvertIt() throws Exception {
        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(runtimeDelegate);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        HeaderValue headerValue = mock(HeaderValue.class);
        when(headerValue.toString()).thenReturn("bar");
        headers.put("foo", newArrayList(headerValue));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals(ImmutableMap.of("foo", newArrayList("bar")), response.getStringHeaders());
    }

    @Test
    public void getSingleHeaderAsStringAndUsesRuntimeDelegateForConvertValueToString() throws Exception {
        HeaderDelegate<HeaderValue> headerDelegate = mock(HeaderDelegate.class);
        when(headerDelegate.toString(isA(HeaderValue.class))).thenReturn("bar");
        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(HeaderValue.class)).thenReturn(headerDelegate);
        RuntimeDelegate.setInstance(runtimeDelegate);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put("foo", newArrayList(new HeaderValue()));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals("bar", response.getHeaderString("foo"));
    }

    @Test
    public void getMultipleHeaderAsStringAndUsesRuntimeDelegateForConvertValuesToString() throws Exception {
        HeaderDelegate<HeaderValue> headerDelegate = mock(HeaderDelegate.class);
        when(headerDelegate.toString(isA(HeaderValue.class))).thenReturn("bar1", "bar2");
        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(HeaderValue.class)).thenReturn(headerDelegate);
        RuntimeDelegate.setInstance(runtimeDelegate);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put("foo", newArrayList(new HeaderValue(), new HeaderValue()));
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertEquals("bar1,bar2", response.getHeaderString("foo"));
    }

    @Test
    public void getsNullIfHeaderNotExist() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ResponseImpl response = new ResponseImpl(200, "foo", null, headers);

        assertNull(response.getHeaderString("foo"));
    }

    public static class HeaderValue {
    }

    @Test
    public void getsStatusInfoForKnownStatus() throws Exception {
        ResponseImpl response = new ResponseImpl(200, "foo", null, null);

        assertEquals(OK, response.getStatusInfo());

    }

    @Test
    public void getsUnknownStatusInfoForUnknownStatus() throws Exception {
        ResponseImpl response = new ResponseImpl(0, "foo", null, null);

        Response.StatusType statusInfo = response.getStatusInfo();
        assertEquals(0, statusInfo.getStatusCode());
        assertEquals(OTHER, statusInfo.getFamily());
        assertEquals("Unknown", statusInfo.getReasonPhrase());
    }
}
