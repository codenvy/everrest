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
package org.everrest.test.mock;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The Class MockHttpServletResponse.
 *
 * @author Max Shaposhnik
 */
public class MockHttpServletResponse implements HttpServletResponse {

    /** The writer. */
    private PrintWriter writer;

    /** The stream. */
    private ByteArrayOutputStream stream;

    /** The output. */
    private ByteArrayServletOutputStream output;

    /** The buffer. */
    private byte[] buffer = new byte[1024];

    /** The buffer count. */
    private int bufferCount = 0;

    /** The cookies. */
    private List<Cookie> cookies = new ArrayList<Cookie>();

    /** The headers. */
    private CaseInsensitiveMultivaluedMap<String> headers = new CaseInsensitiveMultivaluedMap<String>();

    /** The status. */
    private int status = HttpServletResponse.SC_OK;

    /** The message. */
    private String message = "";

    /** The locale. */
    private Locale locale = Locale.getDefault();

    /** The content type. */
    private String contentType = null;

    /** The content length. */
    protected int contentLength = -1;

    /** The encoding. */
    protected String encoding = null;

    /** The date format we will use for creating date headers. */
    protected static final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** Instantiates a new mock http servlet response. */
    public MockHttpServletResponse() {
        stream = new ByteArrayOutputStream();
        writer = new PrintWriter(stream);
        output = new ByteArrayServletOutputStream(stream);
    }

    /**
     * Gets the output content.
     *
     * @return the output content
     */
    public String getOutputContent() {
        return new String(stream.toByteArray());
    }

    /** {@inheritDoc} */
    public void flushBuffer() throws IOException {
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            } finally {
                bufferCount = 0;
            }
        }
    }

    /** {@inheritDoc} */
    public int getBufferSize() {
        return (buffer.length);
    }

    /** {@inheritDoc} */
    public ServletOutputStream getOutputStream() throws IOException {
        return this.output;
    }

    /** {@inheritDoc} */
    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }

    /** {@inheritDoc} */
    public boolean isCommitted() {
        return false;
    }

    /** {@inheritDoc} */
    public void reset() {
        bufferCount = 0;
    }

    /** {@inheritDoc} */
    public void resetBuffer() {
        bufferCount = 0;
    }

    /** {@inheritDoc} */
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    /** {@inheritDoc} */
    public void addDateHeader(String name, long value) {
        addHeader(name, format.format(new Date(value)));
    }

    /** {@inheritDoc} */
    public void addHeader(String name, String value) {
        headers.get(name).add(value);
    }

    /** {@inheritDoc} */
    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }

    /** {@inheritDoc} */
    public boolean containsHeader(String name) {
        return (headers.get(name) != null);
    }

    /** {@inheritDoc} */
    public String encodeRedirectURL(String url) {
        return url;
    }

    /** {@inheritDoc} */
    public String encodeRedirectUrl(String url) {
        return url;
    }

    /** {@inheritDoc} */
    public String encodeURL(String url) {
        return url;
    }

    /** {@inheritDoc} */
    public String encodeUrl(String url) {
        return url;
    }

    /** {@inheritDoc} */
    public void sendError(int status) throws IOException {
        sendError(status, "");
    }

    /** {@inheritDoc} */
    public void sendError(int status, String message) throws IOException {
        this.status = status;
        this.message = message;
        resetBuffer();
    }

    /** {@inheritDoc} */
    public void sendRedirect(String location) throws IOException {
        resetBuffer();
        setStatus(SC_MOVED_TEMPORARILY);
        setHeader("Location", location);
    }

    /** {@inheritDoc} */
    public void setDateHeader(String name, long value) {
        setHeader(name, format.format(new Date(value)));
    }

    /** {@inheritDoc} */
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        headers.put(name, values);
        String lowerCaseName = name.toLowerCase();
        if (lowerCaseName.equals("content-length")) {
            int contentLength = Integer.parseInt(value);
            if (contentLength >= 0) {
                setContentLength(contentLength);
            }
        } else if (lowerCaseName.equals("content-type")) {
            setContentType(value);
        }
    }

    /** {@inheritDoc} */
    public void setIntHeader(String name, int value) {
        setHeader(name, "" + value);
    }

    /** {@inheritDoc} */
    public void setStatus(int status) {
        this.status = status;
    }

    /** {@inheritDoc} */
    public void setStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String name) {
        return headers.getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return new ArrayList<String>(headers.get(name));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return new ArrayList<String>(headers.keySet());
    }

    /** {@inheritDoc} */
    public String getCharacterEncoding() {
        return encoding == null ? "UTF-8" : encoding;
    }

    /** {@inheritDoc} */
    public Locale getLocale() {
        return locale;
    }

    /** {@inheritDoc} */
    public void setBufferSize(int size) {
        if (buffer.length >= size) {
            return;
        }
        buffer = new byte[size];
    }

    /** {@inheritDoc} */
    public void setContentLength(int length) {
        this.contentLength = length;

    }

    /** {@inheritDoc} */
    public void setContentType(String type) {
        this.contentType = type;
    }

    /** {@inheritDoc} */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /** The Class ByteArrayServletOutputStream. */
    private static class ByteArrayServletOutputStream extends ServletOutputStream {

        /** The baos. */
        ByteArrayOutputStream baos;

        /**
         * Instantiates a new byte array servlet output stream.
         *
         * @param baos
         *         the baos
         */
        public ByteArrayServletOutputStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        /** {@inheritDoc} */
        public void write(int i) throws IOException {
            baos.write(i);
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }
}
