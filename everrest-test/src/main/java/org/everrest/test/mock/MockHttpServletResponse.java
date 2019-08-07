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
package org.everrest.test.mock;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
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


    @Override
    public void flushBuffer() throws IOException {
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            } finally {
                bufferCount = 0;
            }
        }
    }


    @Override
    public int getBufferSize() {
        return (buffer.length);
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.output;
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }


    @Override
    public boolean isCommitted() {
        return false;
    }


    @Override
    public void reset() {
        bufferCount = 0;
    }


    @Override
    public void resetBuffer() {
        bufferCount = 0;
    }


    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }


    @Override
    public void addDateHeader(String name, long value) {
        addHeader(name, format.format(new Date(value)));
    }


    @Override
    public void addHeader(String name, String value) {
        headers.get(name).add(value);
    }


    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }


    @Override
    public boolean containsHeader(String name) {
        return (headers.get(name) != null);
    }


    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }


    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }


    @Override
    public String encodeURL(String url) {
        return url;
    }


    @Override
    public String encodeUrl(String url) {
        return url;
    }


    @Override
    public void sendError(int status) throws IOException {
        sendError(status, "");
    }


    @Override
    public void sendError(int status, String message) throws IOException {
        this.status = status;
        this.message = message;
        resetBuffer();
    }


    @Override
    public void sendRedirect(String location) throws IOException {
        resetBuffer();
        setStatus(SC_MOVED_TEMPORARILY);
        setHeader("Location", location);
    }


    @Override
    public void setDateHeader(String name, long value) {
        setHeader(name, format.format(new Date(value)));
    }


    @Override
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


    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, "" + value);
    }


    @Override
    public void setStatus(int status) {
        this.status = status;
    }


    @Override
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


    @Override
    public String getCharacterEncoding() {
        return encoding == null ? "UTF-8" : encoding;
    }


    @Override
    public Locale getLocale() {
        return locale;
    }


    @Override
    public void setBufferSize(int size) {
        if (buffer.length >= size) {
            return;
        }
        buffer = new byte[size];
    }


    @Override
    public void setContentLength(int length) {
        this.contentLength = length;

    }

    @Override
    public void setContentLengthLong(long l) {

    }


    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }


    @Override
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


        @Override
        public void write(int i) throws IOException {
            baos.write(i);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }
}
