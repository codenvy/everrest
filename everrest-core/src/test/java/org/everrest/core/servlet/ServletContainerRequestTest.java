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
package org.everrest.core.servlet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.tools.EmptyInputStream;
import org.everrest.test.mock.MockHttpServletRequest;
import org.junit.Test;

/**
 * Test for {@link ServletContainerRequest}
 * 
 * @author Tareq Sharafy <tareq.sharafy@sap.com>
 */
public class ServletContainerRequestTest {

    private static final String TEST_SCHEME = "http://";
    private static final String TEST_SERVER_NAME = "test.myhost.com";
    private static final int TEST_SERVER_PORT = 8080;
    private static final String TEST_HOST = TEST_SERVER_NAME + ":" + TEST_SERVER_PORT;
    private static final String TEST_CONTEXT_PATH = "/myapp";
    private static final String TEST_SERVLET_PATH = "/myservlet";
    private static final String TEST_SUBPATH = "/datapath";

    private static final String TEST_BASE_PATH = TEST_CONTEXT_PATH + TEST_SERVLET_PATH;
    private static final String TEST_FULL_PATH = TEST_BASE_PATH + TEST_SUBPATH;
    
    private static final String TEST_BASE_URI = TEST_SCHEME + TEST_HOST + TEST_BASE_PATH;
    private static final String TEST_REQUEST_URI = TEST_BASE_URI + TEST_SUBPATH;

    private static class MockEmptyBodyHttpRequest extends MockHttpServletRequest {

        MockEmptyBodyHttpRequest(String forwardedHost, String forwardedProto) {
            super("", new EmptyInputStream(), 0, "GET", transformHeaders(forwardedHost, forwardedProto));
        }

        private static Map<String, List<String>> transformHeaders(String forwardedHost, String forwardedProto) {
            Map<String, List<String>> finalHeaders = new HashMap<String, List<String>>();
            if (forwardedHost != null) {
                finalHeaders.put(ExtHttpHeaders.FORWARDED_HOST, Arrays.asList(forwardedHost));
            }
            if (forwardedProto != null) {
                finalHeaders.put(ExtHttpHeaders.FORWARDED_PROTO, Arrays.asList(forwardedProto));
            }
            // Add the 'host' header
            finalHeaders.put(HttpHeaders.HOST, Arrays.asList(TEST_HOST));
            return finalHeaders;
        }

        @Override
        public String getServerName() {
            return TEST_SERVER_NAME;
        }

        @Override
        public int getServerPort() {
            return TEST_SERVER_PORT;
        }

        @Override
        public String getContextPath() {
            return TEST_CONTEXT_PATH;
        }

        @Override
        public String getServletPath() {
            return TEST_SERVLET_PATH;
        }

        @Override
        public String getPathInfo() {
            return TEST_SUBPATH;
        }
    }

    @Test
    public void testSimpleRequest() {
        // A simple HTTP request
        MockHttpServletRequest httpReq = new MockEmptyBodyHttpRequest(null, null);
        ServletContainerRequest req = ServletContainerRequest.create(httpReq);
        // Validate the fields
        assertEquals(TEST_BASE_URI, req.getBaseUri().toString());
        assertEquals(TEST_REQUEST_URI, req.getRequestUri().toString());
    }

    @Test
    public void testInvalidForwardedHost() {
        // A simple HTTP request
        MockHttpServletRequest httpReq = new MockEmptyBodyHttpRequest("a b c", null);
        ServletContainerRequest req = ServletContainerRequest.create(httpReq);
        // Validate the fields
        assertEquals(TEST_BASE_URI, req.getBaseUri().toString());
        assertEquals(TEST_REQUEST_URI, req.getRequestUri().toString());
    }

    @Test
    public void testForwardedHost() {
        // A simple HTTP request
        MockHttpServletRequest httpReq = new MockEmptyBodyHttpRequest("other.myhost.com", null);
        ServletContainerRequest req = ServletContainerRequest.create(httpReq);
        // Validate the fields
        assertEquals(TEST_SCHEME + "other.myhost.com" + TEST_BASE_PATH, req.getBaseUri().toString());
        assertEquals(TEST_SCHEME + "other.myhost.com" + TEST_FULL_PATH, req.getRequestUri().toString());
    }

    @Test
    public void testForwardedHostWithPort() {
        // A simple HTTP request
        MockHttpServletRequest httpReq = new MockEmptyBodyHttpRequest("other.myhost.com:777", null);
        ServletContainerRequest req = ServletContainerRequest.create(httpReq);
        // Validate the fields
        assertEquals(TEST_SCHEME + "other.myhost.com:777" + TEST_BASE_PATH, req.getBaseUri().toString());
        assertEquals(TEST_SCHEME + "other.myhost.com:777" + TEST_FULL_PATH, req.getRequestUri().toString());
    }
}
