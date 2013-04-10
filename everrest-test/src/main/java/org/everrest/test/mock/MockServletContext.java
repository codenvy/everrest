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
package org.everrest.test.mock;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * The Class MockServletContext.
 *
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: $
 */
public class MockServletContext implements ServletContext {

    /** The name. */
    private String name;

    /** The init params. */
    private HashMap<String, String> initParams;

    /** The attributes. */
    private HashMap<String, Object> attributes;

    /** The context path. */
    private String contextPath;

    /** The log buffer. */
    private StringBuilder logBuffer = new StringBuilder();

    /** Instantiates a new mock servlet context. */
    public MockServletContext() {
        this("MockServletContext1");
    }

    /**
     * Instantiates a new mock servlet context.
     *
     * @param name
     *         the name
     */
    public MockServletContext(String name) {
        this.name = name;
        this.initParams = new HashMap<String, String>();
        this.attributes = new HashMap<String, Object>();
    }

    /**
     * Instantiates a new mock servlet context.
     *
     * @param name
     *         the name
     * @param path
     *         the path
     */
    public MockServletContext(String name, String path) {
        this(name);
        contextPath = path;
        attributes.put("javax.servlet.context.tempdir", path);
    }

    /**
     * Sets the name.
     *
     * @param name
     *         the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the log buffer.
     *
     * @return the log buffer
     */
    public String getLogBuffer() {
        try {
            return logBuffer.toString();
        } finally {
            logBuffer = new StringBuilder();
        }
    }

    /** {@inheritDoc} */
    public ServletContext getContext(String s) {
        return null;
    }

    /** {@inheritDoc} */
    public int getMajorVersion() {
        return 2;
    }

    /** {@inheritDoc} */
    public int getMinorVersion() {
        return 4;
    }

    /** {@inheritDoc} */
    public String getMimeType(String s) {
        return "text/html";
    }

    /** {@inheritDoc} */
    public Set getResourcePaths(String s) {

        if (!s.endsWith("/"))
            s = s + "/";

        Set<String> set = new HashSet<String>();

        try {
            URL url = getResource(s);
            if (url != null) {
                File dir = new File(url.getPath());
                if (dir.isDirectory()) {
                    File[] arr = dir.listFiles();
                    for (int i = 0; i < arr.length; i++) {
                        File tmp = arr[i];
                        if (tmp.isDirectory())
                            set.add(s + "/" + tmp.getName() + "/");
                        else
                            set.add(s + "/" + tmp.getName());
                    }
                }
            }
        } catch (MalformedURLException e) {
        }
        return set;
    }

    /** {@inheritDoc} */
    public URL getResource(String s) throws MalformedURLException {
        String path = "file:" + contextPath + s;
        URL url = new URL(path);
        return url;
    }

    /** {@inheritDoc} */
    public InputStream getResourceAsStream(String s) {
        try {
            return getResource(s).openStream();
        } catch (IOException e) {
        }
        return null;
    }

    /** {@inheritDoc} */
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    /** {@inheritDoc} */
    public RequestDispatcher getNamedDispatcher(String s) {
        return null;
    }

    /** {@inheritDoc} */
    @Deprecated
    public Servlet getServlet(String s) throws ServletException {
        return null;
    }

    /** {@inheritDoc} */
    @Deprecated
    public Enumeration getServlets() {
        return null;
    }

    /** {@inheritDoc} */
    @Deprecated
    public Enumeration getServletNames() {
        return null;
    }

    /** {@inheritDoc} */
    public void log(String s) {
        logBuffer.append(s);
    }

    /** {@inheritDoc} */
    @Deprecated
    public void log(Exception e, String s) {
        logBuffer.append(s).append(e.getMessage());
    }

    /** {@inheritDoc} */
    public void log(String s, Throwable throwable) {
        logBuffer.append(s).append(throwable.getMessage());
    }

    /**
     * Sets the context path.
     *
     * @param s
     *         the new context path
     */
    public void setContextPath(String s) {
        contextPath = s;
    }

    /** {@inheritDoc} */
    public String getRealPath(String s) {
        return contextPath + s;
    }

    /** {@inheritDoc} */
    public String getServerInfo() {
        return null;
    }

    /**
     * Sets the init parameter.
     *
     * @param name
     *         the name
     * @param value
     *         the value
     */
    public void setInitParameter(String name, String value) {
        initParams.put(name, value);
    }

    /** {@inheritDoc} */
    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    /** {@inheritDoc} */
    public Enumeration getInitParameterNames() {
        Vector<String> keys = new Vector<String>(initParams.keySet());
        return keys.elements();
    }

    /** {@inheritDoc} */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /** {@inheritDoc} */
    public Enumeration getAttributeNames() {
        Vector<String> keys = new Vector<String>(attributes.keySet());
        return keys.elements();
    }

    /** {@inheritDoc} */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /** {@inheritDoc} */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /** {@inheritDoc} */
    public String getServletContextName() {
        return name;
    }

}
