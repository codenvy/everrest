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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Class MockServletContext.
 *
 * @author Max Shaposhnik
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
   * @param name the name
   */
  public MockServletContext(String name) {
    this.name = name;
    this.initParams = new HashMap<String, String>();
    this.attributes = new HashMap<String, Object>();
  }

  /**
   * Instantiates a new mock servlet context.
   *
   * @param name the name
   * @param path the path
   */
  public MockServletContext(String name, String path) {
    this(name);
    contextPath = path;
    attributes.put("jakarta.servlet.context.tempdir", path);
  }

  /**
   * Sets the name.
   *
   * @param name the new name
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

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public ServletContext getContext(String s) {
    return null;
  }

  @Override
  public int getMajorVersion() {
    return 3;
  }

  @Override
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public int getEffectiveMajorVersion() {
    return 3;
  }

  @Override
  public int getEffectiveMinorVersion() {
    return 0;
  }

  @Override
  public String getMimeType(String s) {
    return "text/html";
  }

  @Override
  public Set<String> getResourcePaths(String s) {
    if (!s.endsWith("/")) {
      s = s + "/";
    }
    Set<String> set = new HashSet<String>();
    try {
      URL url = getResource(s);
      if (url != null) {
        File dir = new File(url.getPath());
        if (dir.isDirectory()) {
          File[] arr = dir.listFiles();
          if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
              File tmp = arr[i];
              if (!tmp.isDirectory()) {
                set.add(s + "/" + tmp.getName());
              } else {
                set.add(s + "/" + tmp.getName() + "/");
              }
            }
          }
        }
      }
    } catch (MalformedURLException ignored) {
    }
    return set;
  }

  @Override
  public URL getResource(String s) throws MalformedURLException {
    String path = "file:" + contextPath + s;
    return new URL(path);
  }

  @Override
  public InputStream getResourceAsStream(String s) {
    try {
      return getResource(s).openStream();
    } catch (IOException ignored) {
    }
    return null;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  @Override
  public RequestDispatcher getNamedDispatcher(String s) {
    return null;
  }

  @Override
  public Servlet getServlet(String s) throws ServletException {
    return null;
  }

  @Override
  public Enumeration<Servlet> getServlets() {
    return null;
  }

  @Override
  public Enumeration<String> getServletNames() {
    return null;
  }

  @Override
  public void log(String s) {
    logBuffer.append(s);
  }

  @Override
  public void log(Exception e, String s) {
    logBuffer.append(s).append(e.getMessage());
  }

  @Override
  public void log(String s, Throwable throwable) {
    logBuffer.append(s).append(throwable.getMessage());
  }

  public void setContextPath(String s) {
    contextPath = s;
  }

  @Override
  public String getRealPath(String s) {
    return contextPath + s;
  }

  @Override
  public String getServerInfo() {
    return null;
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    if (initParams.get(name) == null) {
      initParams.put(name, value);
      return true;
    }
    return false;
  }

  @Override
  public String getInitParameter(String name) {
    return initParams.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(initParams.keySet());
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(attributes.keySet());
  }

  @Override
  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  @Override
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  @Override
  public String getServletContextName() {
    return name;
  }

  @Override
  public ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  // Methods bellow are not implemented for this mock.

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, String className) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public ServletRegistration.Dynamic addServlet(
      String servletName, Class<? extends Servlet> servletClass) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, String className) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public FilterRegistration.Dynamic addFilter(
      String filterName, Class<? extends Filter> filterClass) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void addListener(String className) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void declareRoles(String... roleNames) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public String getVirtualServerName() {
    return null;
  }

  @Override
  public int getSessionTimeout() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public String getRequestCharacterEncoding() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public String getResponseCharacterEncoding() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {
    throw new UnsupportedOperationException("not supported");
  }
}
