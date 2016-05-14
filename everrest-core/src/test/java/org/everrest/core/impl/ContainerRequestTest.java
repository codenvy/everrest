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
package org.everrest.core.impl;

import com.google.common.collect.ImmutableMap;

import org.everrest.core.ApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Variant;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.COOKIE;
import static javax.ws.rs.core.HttpHeaders.DATE;
import static javax.ws.rs.core.HttpHeaders.IF_MATCH;
import static javax.ws.rs.core.HttpHeaders.IF_MODIFIED_SINCE;
import static javax.ws.rs.core.HttpHeaders.IF_NONE_MATCH;
import static javax.ws.rs.core.HttpHeaders.IF_UNMODIFIED_SINCE;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContainerRequestTest {
    private String httpMethod = "POST";
    private URI requestUri = URI.create("http://localhost:8080/servlet/a/b/c");
    private URI baseUri = URI.create("http://localhost:8080/servlet");
    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    private InputStream        entityStream;
    private SecurityContext    securityContext;
    private ApplicationContext applicationContext;

    private ContainerRequest containerRequest;

    @Before
    public void setUp() throws Exception {
        securityContext = mock(SecurityContext.class);
        applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        ApplicationContext.setCurrent(applicationContext);
        containerRequest = new ContainerRequest(httpMethod, requestUri, baseUri, entityStream, headers, securityContext);
    }

    @After
    public void tearDown() throws Exception {
        headers.clear();
        entityStream = null;
        securityContext = null;
    }

    @Test
    public void getsMethod() {
        assertEquals(httpMethod, containerRequest.getMethod());
    }

    @Test
    public void getsRequestUri() {
        assertEquals(requestUri, containerRequest.getRequestUri());
    }

    @Test
    public void getsBaseUri() {
        assertEquals(baseUri, containerRequest.getBaseUri());
    }

    @Test
    public void setsBaseUriAndRequestUri() {
        URI newBaseUri = URI.create("http://localhost1:8080/servlet");
        URI newRequestUri = URI.create("http://localhost:80802/servlet/a/b/c");

        containerRequest.setUris(newRequestUri, newBaseUri);

        assertEquals(newBaseUri, containerRequest.getBaseUri());
        assertEquals(newRequestUri, containerRequest.getRequestUri());
    }

    @Test
    public void getsAcceptableMediaTypes() {
        headers.putSingle(ACCEPT, "text/*;q=0.3, text/html;q=0.7, text/xml");
        List<MediaType> acceptableMediaTypes = containerRequest.getAcceptableMediaTypes();
        List<MediaType> expectedAcceptMediaTypes = newArrayList(new MediaType("text", "xml"),
                                                                new MediaType("text", "html", ImmutableMap.of("q", "0.7")),
                                                                new MediaType("text", "*", ImmutableMap.of("q", "0.3")));
        assertEquals(expectedAcceptMediaTypes, acceptableMediaTypes);
    }

    @Test
    public void getsAcceptableMediaType() {
        headers.putSingle(ACCEPT, "text/*;q=0.3, text/html;q=0.7, text/xml");
        List<MediaType> checkMediaTypes = newArrayList(new MediaType(), new MediaType("application", "xml"), new MediaType("text", "plain"));
        MediaType acceptableMediaType = containerRequest.getAcceptableMediaType(checkMediaTypes);
        assertEquals(new MediaType("text", "plain"), acceptableMediaType);
    }

    @Test
    public void returnsNullWhenAcceptableMediaTypeNotFound() {
        headers.putSingle(ACCEPT, "text/*;q=0.3, text/html;q=0.7, text/xml");
        List<MediaType> checkMediaTypes = newArrayList(new MediaType(), new MediaType("application", "xml"));
        MediaType acceptableMediaType = containerRequest.getAcceptableMediaType(checkMediaTypes);
        assertNull(acceptableMediaType);
    }

    @Test
    public void returnsEmptyListWhenNoCookiesHeadersInRequest() {
        List<String> cookieHeaders = containerRequest.getCookieHeaders();
        assertNotNull(cookieHeaders);
        assertTrue(cookieHeaders.isEmpty());
    }

    @Test
    public void getsCookieHeaders() {
        headers.putSingle(COOKIE, "foo=bar");
        List<String> cookieHeaders = containerRequest.getCookieHeaders();
        assertEquals(newArrayList("foo=bar"), cookieHeaders);
    }

    @Test
    public void resetsParsedFormDataWhenSetEntityStream() {
        InputStream entityStream = mock(InputStream.class);
        containerRequest.setEntityStream(entityStream);
        verify(applicationContext.getAttributes()).remove("org.everrest.provider.entity.decoded.form");
        verify(applicationContext.getAttributes()).remove("org.everrest.provider.entity.encoded.form");
    }

    @Test
    public void getsAuthenticationScheme() {
        when(securityContext.getAuthenticationScheme()).thenReturn("BASIC_AUTH");
        assertEquals("BASIC_AUTH", containerRequest.getAuthenticationScheme());
    }

    @Test
    public void getsUserPrincipal() {
        Principal principal = mock(Principal.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        assertSame(principal, containerRequest.getUserPrincipal());
    }

    @Test
    public void testIsSecure() {
        when(securityContext.isSecure()).thenReturn(true);
        assertTrue(containerRequest.isSecure());
    }

    @Test
    public void testIsUserInRole() {
        when(securityContext.isUserInRole("admin")).thenReturn(true);
        assertTrue(containerRequest.isUserInRole("admin"));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAsNullWhenBothIfMatchAndIfNoneMatchHeaderNotSet() {
        assertNull(containerRequest.evaluatePreconditions(new EntityTag("1234567")));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAsNullWhenIfMatchHeaderMatchesWithETag() {
        headers.putSingle(IF_MATCH, "\"1234567\"");
        assertNull(containerRequest.evaluatePreconditions(new EntityTag("1234567")));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAs_PRECONDITION_FAILED_WhenETagIsWeak() {
        headers.putSingle(IF_MATCH, "\"1234567\"");
        Response response = containerRequest.evaluatePreconditions(new EntityTag("W/1234567")).build();
        assertEquals(PRECONDITION_FAILED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAsNullWhenIfNoneMatchHeaderDoesNotMatchWithETag() {
        headers.putSingle(IF_NONE_MATCH, "\"1234567\"");
        assertNull(containerRequest.evaluatePreconditions(new EntityTag("7654321")));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAs_PRECONDITION_FAILED_WhenIfNoneMatchHeaderMatchesWithETag() {
        headers.putSingle(IF_NONE_MATCH, "\"1234567\"");
        Response response = containerRequest.evaluatePreconditions(new EntityTag("1234567")).build();
        assertEquals(PRECONDITION_FAILED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAs_NOT_MODIFIED_When_GET_RequestAndIfNoneMatchHeaderIsWeak() {
        containerRequest.setMethod("GET");
        headers.putSingle(IF_NONE_MATCH, "W/\"1234567\"");
        Response response = containerRequest.evaluatePreconditions(new EntityTag("1234567")).build();
        assertEquals(NOT_MODIFIED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAs_NOT_MODIFIED_When_HEAD_RequestAndIfNoneMatchHeaderIsWeak() {
        containerRequest.setMethod("HEAD");
        headers.putSingle(IF_NONE_MATCH, "W/\"1234567\"");
        Response response = containerRequest.evaluatePreconditions(new EntityTag("1234567")).build();
        assertEquals(NOT_MODIFIED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByLastModificationDateAs_PRECONDITION_FAILED_WhenLastModificationDateIsAfterIfUnmodifiedSinceHeader() {
        Date now = new Date();
        Date before = new Date(now.getTime() - 10000);
        headers.putSingle(IF_UNMODIFIED_SINCE, formatDateInRfc1123DateFormat(before));
        Response response = containerRequest.evaluatePreconditions(now).build();
        assertEquals(PRECONDITION_FAILED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByLastModificationDateAsNullWhenIfUnmodifiedSinceHeaderIsNotSet() {
        Date now = new Date();
        headers.putSingle(IF_UNMODIFIED_SINCE, null);
        assertNull(containerRequest.evaluatePreconditions(now));
    }

    @Test
    public void evaluatesPreconditionsResponseByLastModificationDateAsNullWhenIfUnmodifiedSinceHeaderHasInvalidFormat() {
        Date now = new Date();
        headers.putSingle(IF_UNMODIFIED_SINCE, "foo");
        assertNull(containerRequest.evaluatePreconditions(now));
    }

    @Test
    public void evaluatesPreconditionsResponseByLastModificationDateAs_NOT_MODIFIED_WhenLastModificationDateIsNotAfterIfModifiedSinceHeader() {
        Date now = new Date();
        Date before = new Date(now.getTime() - 10000);
        headers.putSingle(IF_MODIFIED_SINCE, formatDateInRfc1123DateFormat(now));
        Response response = containerRequest.evaluatePreconditions(before).build();
        assertEquals(NOT_MODIFIED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByLastModificationDateAsNullWhenIfModifiedSinceHeaderIsNotSet() {
        Date now = new Date();
        headers.putSingle(IF_MODIFIED_SINCE, null);
        assertNull(containerRequest.evaluatePreconditions(now));
    }

    @Test
    public void evaluatesPreconditionsResponseByLastModificationDateAsNullWhenIfModifiedSinceHeaderHasInvalidFormat() {
        Date now = new Date();
        headers.putSingle(IF_MODIFIED_SINCE, "foo");
        assertNull(containerRequest.evaluatePreconditions(now));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAndLastModificationDateAsNullWhenHeadersNotSet() {
        assertNull(containerRequest.evaluatePreconditions(new Date(), new EntityTag("1234567")));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAndLastModificationDateAsNullWhenIfMatchHeaderMatchesWithETag() {
        headers.putSingle(IF_MATCH, "\"1234567\"");
        assertNull(containerRequest.evaluatePreconditions(new Date(), new EntityTag("1234567")));
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAndLastModificationDateAs_PRECONDITION_FAILED_WhenETagIsWeak() {
        headers.putSingle(IF_MATCH, "\"1234567\"");
        Response response = containerRequest.evaluatePreconditions(new Date(), new EntityTag("W/1234567")).build();
        assertEquals(PRECONDITION_FAILED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByETgaAndLastModificationDateAs_NOT_MODIFIED_WhenLastModificationDateIsNotAfterIfModifiedSinceHeader() {
        Date now = new Date();
        Date before = new Date(now.getTime() - 10000);
        headers.putSingle(IF_MODIFIED_SINCE, formatDateInRfc1123DateFormat(now));
        Response response = containerRequest.evaluatePreconditions(before, new EntityTag("1234567")).build();
        assertEquals(NOT_MODIFIED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseByETagAndLastModificationDateAs_PRECONDITION_FAILED_WhenIfNoneMatchHeaderMatchesWithETag() {
        headers.putSingle(IF_NONE_MATCH, "\"1234567\"");
        Response response = containerRequest.evaluatePreconditions(new Date(), new EntityTag("1234567")).build();
        assertEquals(PRECONDITION_FAILED, response.getStatusInfo());
    }

    private String formatDateInRfc1123DateFormat(Date date) {
        DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        return format.format(date);
    }

    @Test
    public void evaluatesPreconditionsResponseAs_PRECONDITION_FAILED_WhenIfMatchHeaderSet() {
        headers.putSingle(IF_MATCH, "\"1234567\"");
        Response response = containerRequest.evaluatePreconditions().build();
        assertEquals(PRECONDITION_FAILED, response.getStatusInfo());
    }

    @Test
    public void evaluatesPreconditionsResponseAsNullWhenIfMatchHeaderNotSet() {
        headers.putSingle(IF_MATCH, null);
        assertNull(containerRequest.evaluatePreconditions());
    }

    @Test
    public void selectsValiant() {
        VariantsHandler variantsHandler = mock(VariantsHandler.class);
        containerRequest.setVariantsHandler(variantsHandler);

        List<Variant> variants = Variant.mediaTypes(MediaType.valueOf("text/xml")).languages(new Locale("en")).add()
                                        .mediaTypes(MediaType.valueOf("text/xml")).languages(new Locale("en", "us")).add()
                                        .build();

        containerRequest.selectVariant(variants);

        verify(variantsHandler).handleVariants(containerRequest, variants);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenVariantListIsNull() throws Exception {
        containerRequest.selectVariant(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenVariantListIsEmpty() throws Exception {
        containerRequest.selectVariant(newArrayList());
    }

    @Test
    public void getsAcceptableLanguages() {
        headers.put(ACCEPT_LANGUAGE, newArrayList("en-GB", "en;q=0.8"));

        assertEquals(newArrayList(new Locale("en", "GB"), new Locale("en")), containerRequest.getAcceptableLanguages());
    }

    @Test
    public void getsCookies() {
        headers.put(COOKIE, newArrayList("$Version=1;company=codenvy;$Path=/path,$Domain=codenvy.com", "name=andrew"));

        Map<String, Cookie> expectedCookies = ImmutableMap.of("company", new Cookie("company", "codenvy", "/path", "codenvy.com"), "name", new Cookie("name", "andrew"));

        assertEquals(expectedCookies, containerRequest.getCookies());
    }

    @Test
    public void getsDate() {
        Date date = new Date();
        headers.putSingle(DATE, formatDateInRfc1123DateFormat(date));

        assertTrue("Dates are not equal", Math.abs(date.getTime() - containerRequest.getDate().getTime()) < 1000);
    }

    @Test
    public void getsContentLengthAsMinusOneWhenContentLengthHeaderIsNotSet() {
        assertEquals(-1, containerRequest.getLength());
    }

    @Test
    public void getsContentLength() {
        headers.putSingle(CONTENT_LENGTH, "101");
        assertEquals(101, containerRequest.getLength());
    }

    @Test
    public void getsMediaType() {
        headers.putSingle(CONTENT_TYPE, "text/plain");
        assertEquals(new MediaType("text", "plain"), containerRequest.getMediaType());
    }

    @Test
    public void getsHeaderAsString() {
        headers.put("foo", newArrayList("bar1", "bar2"));
        assertEquals("bar1,bar2", containerRequest.getHeaderString("foo"));
    }

    @Test
    public void getsLanguage() {
        headers.putSingle(CONTENT_LANGUAGE, "en-gb");
        assertEquals(new Locale("en", "gb"), containerRequest.getLanguage());
    }
}