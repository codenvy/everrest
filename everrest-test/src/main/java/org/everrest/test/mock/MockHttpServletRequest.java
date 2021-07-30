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
package org.everrest.test.mock;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Class MockHttpServletRequest.
 *
 * @author Max Shaposhnik
 */
@SuppressWarnings("unchecked")
public class MockHttpServletRequest implements HttpServletRequest {
    private static final Pattern URL_PATTERN = Pattern.compile("http://([^:]+?):([^/]+?)/([^/]+?)/(.*?)");

    private String method;
    private int length;
    private String requestURL;
    private InputStream data;
    private CaseInsensitiveMultivaluedMap<String> headers = new CaseInsensitiveMultivaluedMap<String>();
    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private HttpSession session;
    private Locale locale;
    private boolean secure;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Principal principal;

    /**
     * Instantiates a new mock http servlet request.
     *
     * @param url
     *         the url
     * @param data
     *         the data
     * @param length
     *         the length
     * @param method
     *         the method
     * @param headers
     *         the headers
     */
    public MockHttpServletRequest(String url, InputStream data, int length, String method,
                                  Map<String, List<String>> headers) {
        this.requestURL = url;
        this.data = data;
        this.length = length;
        this.method = method;
        if (headers != null)
            this.headers.putAll(headers);
        String queryString = getQueryString();
        if (queryString != null) {
            parameters.putAll(parseQueryString(queryString));
        }
        session = new MockHttpSession();
    }

    /** Reset. */
    public void reset() {
        parameters = new HashMap();
        attributes = new HashMap();
    }


    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }


    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }


    @Override
    public String getAuthType() {
        return null;
    }


    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }


    @Override
    public int getContentLength() {
        return length;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }


    @Override
    public String getContentType() {
        return headers.getFirst("content-type");
    }


    @Override
    public String getContextPath() {
        Matcher m = URL_PATTERN.matcher(requestURL);
        if (!m.matches())
            throw new RuntimeException("Unable determine context path.");
        return '/' + m.group(3);
    }


    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }


    @Override
    public long getDateHeader(String name) {
        if (headers.get(name) != null)
            return Long.valueOf(headers.getFirst(name));
        return -1L;
    }


    @Override
    public String getHeader(String name) {
        return headers.getFirst(name);
    }


    @Override
    public Enumeration getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }


    @Override
    public Enumeration getHeaders(String name) {
        return Collections.enumeration(headers.get(name));
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new MockServletInputStream(data);
    }


    @Override
    public int getIntHeader(String name) {
        if (headers.get(name).size() > 0)
            return Integer.parseInt(headers.getFirst(name));
        return -1;
    }

    /**
     * Gets the local addr.
     *
     * @return the local addr
     */
    @Override
    public String getLocalAddr() {
        return "127.0.0.1";
    }


    @Override
    public Locale getLocale() {
        return Locale.US;
    }


    @Override
    public Enumeration getLocales() {
        return null;
    }

    /**
     * Gets the local name.
     *
     * @return the local name
     */
    @Override
    public String getLocalName() {
        return "localhost";
    }

    /**
     * Gets the local port.
     *
     * @return the local port
     */
    @Override
    public int getLocalPort() {
        return 80;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }


    @Override
    public String getMethod() {
        return method;
    }


    @Override
    public String getParameter(String name) {
        return parameters.entrySet()
                         .stream()
                         .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                         .map(entry -> entry.getValue())
                         .filter(value -> value != null)
                         .flatMap(l -> l.stream())
                         .findFirst().orElse(null);
    }


    @Override
    public Map getParameterMap() {
        return parameters.entrySet()
                         .stream()
                         .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                         .collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().stream().toArray(String[]::new)));
    }


    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }


    @Override
    public String[] getParameterValues(String name) {
        return parameters.entrySet()
                         .stream()
                         .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                         .map(entry -> entry.getValue())
                         .filter(value -> value != null)
                         .flatMap(l -> l.stream())
                         .toArray(String[]::new);

    }

    @Override
    public String getPathInfo() {
        Matcher m = URL_PATTERN.matcher(requestURL);
        if (!m.matches())
            throw new RuntimeException("Unable determine pathInfo.");
        String p = m.group(4);
        int q = p.indexOf('?');
        if (q > 0) {
            p = p.substring(0, q);
        }
        return '/' + p;
    }


    @Override
    public String getPathTranslated() {
        return null;
    }


    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }


    @Override
    public String getQueryString() {
        if (requestURL == null)
            return null;
        int sep = requestURL.lastIndexOf('?');
        if (sep == -1)
            return null;
        return requestURL.substring(sep + 1);
    }


    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }


    @Override
    public String getRealPath(String arg0) {
        return null;
    }


    @Override
    public String getRemoteAddr() {
        return "127.0.0.1";
    }


    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    /**
     * Gets the remote port.
     *
     * @return the remote port
     */
    @Override
    public int getRemotePort() {
        return 8080;
    }


    @Override
    public String getRemoteUser() {
        return "root";
    }


    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }


    @Override
    public String getRequestedSessionId() {
        return "sessionId";
    }


    @Override
    public String getRequestURI() {
        return getContextPath() + getServletPath() + getPathInfo();
    }


    @Override
    public StringBuffer getRequestURL() {
        if (requestURL == null)
            return null;
        return new StringBuffer(requestURL);
    }


    @Override
    public String getScheme() {
        return "http";
    }


    @Override
    public String getServerName() {
        Matcher m = URL_PATTERN.matcher(requestURL);
        if (!m.matches())
            throw new RuntimeException("Unable determine server name.");
        return m.group(1);
    }


    @Override
    public int getServerPort() {
        Matcher m = URL_PATTERN.matcher(requestURL);
        if (!m.matches())
            throw new RuntimeException("Unable determine request URI.");
        return Integer.valueOf(m.group(2));
    }


    @Override
    public String getServletPath() {
        return "";
    }


    @Override
    public HttpSession getSession() {
        return session;
    }

    @Override
    public String changeSessionId() {
        return null;
    }


    @Override
    public HttpSession getSession(boolean b) {
        return session;
    }


    @Override
    public Principal getUserPrincipal() {
        return principal == null ? principal = new MockPrincipal("root") : principal;
    }


    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return true;
    }


    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return Collections.emptySet();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }


    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }


    @Override
    public boolean isRequestedSessionIdValid() {
        return true;
    }


    @Override
    public boolean isSecure() {
        return secure;
    }


    @Override
    public boolean isUserInRole(String role) {
        return "admin".equals(role);
    }


    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }


    @Override
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }


    @Override
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
    }

    /**
     * Sets the parameter.
     *
     * @param name
     *         the name
     * @param value
     *         the value
     */
    public void setParameter(String name, String value) {
        ArrayList arr = new ArrayList<String>();
        arr.add(value);
        parameters.put(name, arr);
    }

    public static Map<String, List<String>> parseQueryString(String rawQuery) {
        HashMap<String, List<String>> m = new HashMap<String, List<String>>();
        if (rawQuery == null || rawQuery.length() == 0)
            return m;
        int p = 0;
        int n = 0;
        try {
            while (n < rawQuery.length()) {
                n = rawQuery.indexOf('&', p);
                if (n == -1)
                    n = rawQuery.length();

                String pair = rawQuery.substring(p, n);
                if (pair.length() == 0)
                    continue;

                String name;
                String value = ""; // default value
                int eq = pair.indexOf('=');
                if (eq == -1) // no value, default is ""
                    name = URLDecoder.decode(pair, "UTF-8");
                else {
                    name = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
                    value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                }

                if (m.get(name) == null) {
                    List<String> arr = new ArrayList<String>();
                    arr.add(value);
                    m.put(name, arr);
                } else {
                    List<String> arr = m.get(name);
                    arr.add(value);
                }
                p = n + 1;
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(); // UTF-8 is supported for sure
        }
        return m;
    }

    class MockServletInputStream extends ServletInputStream {

        private final InputStream data;

        public MockServletInputStream(InputStream data) {
            this.data = data;
        }

        @Override
        public int read() throws IOException {
            return data.read();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }
}
