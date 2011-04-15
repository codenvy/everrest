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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class MockHttpServletRequest.
 *
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: $
 */

@SuppressWarnings("unchecked")
public class MockHttpServletRequest implements HttpServletRequest
{

   /** HTTP method. */
   private String method;

   /** Length. */
   private int length;

   /** Request url. */
   private String requestURL;

   /** Data. */
   private InputStream data;

   /** Headers. */
   private CaseInsensitiveMultivaluedMap<String> headers = new CaseInsensitiveMultivaluedMap<String>();

   /** The parameters. */
   private Map<String, List<String>> parameters = new HashMap<String, List<String>>();

   /** The session. */
   private HttpSession session;

   /** The locale. */
   private Locale locale;

   /** The secure. */
   private boolean secure;

   /** The Constant p. */
   private static final Pattern p = Pattern.compile("http://([^:]+?):([^/]+?)/([^/]+?)/(.*?)");

   /** The attributes. */
   private Map<String, Object> attributes = new HashMap<String, Object>();

   private Principal principal;

   /**
    * Instantiates a new mock http servlet request.
    *
    * @param url the url
    * @param data the data
    * @param length the length
    * @param method the method
    * @param headers the headers
    */
   public MockHttpServletRequest(String url, InputStream data, int length, String method,
      Map<String, List<String>> headers)
   {
      this.requestURL = url;
      this.data = data;
      this.length = length;
      this.method = method;
      if (headers != null)
         this.headers.putAll(headers);
      String queryString = getQueryString();
      if (queryString != null)
      {
         parameters.putAll(parseQueryString(queryString));
      }
      session = new MockHttpSession();
   }

   /**
    * Reset.
    */
   public void reset()
   {
      parameters = new HashMap();
      attributes = new HashMap();
   }

   /**
    * {@inheritDoc}
    */
   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration getAttributeNames()
   {
      return new EnumerationImpl(attributes.keySet().iterator());
   }

   /**
    * {@inheritDoc}
    */
   public String getAuthType()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getCharacterEncoding()
   {
      return "UTF-8";
   }

   /**
    * {@inheritDoc}
    */
   public int getContentLength()
   {
      return length;
   }

   /**
    * {@inheritDoc}
    */
   public String getContentType()
   {
      return headers.getFirst("content-type");
   }

   /**
    * {@inheritDoc}
    */
   public String getContextPath()
   {
      Matcher m = p.matcher(requestURL);
      if (!m.matches())
         throw new RuntimeException("Unable determine context path.");
      return '/' + m.group(3);
   }

   /**
    * {@inheritDoc}
    */
   public Cookie[] getCookies()
   {
      return new Cookie[0];
   }

   /**
    * {@inheritDoc}
    */
   public long getDateHeader(String name)
   {
      if (headers.get(name) != null)
         return Long.valueOf(headers.getFirst(name));
      return -1L;
   }

   /**
    * {@inheritDoc}
    */
   public String getHeader(String name)
   {
      return headers.getFirst(name);
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration getHeaderNames()
   {
      return new EnumerationImpl(headers.keySet().iterator());
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration getHeaders(String name)
   {
      ArrayList values = (ArrayList)headers.get(name);
      if (values.size() > 0)
         return new EnumerationImpl(values.iterator());

      return new EnumerationImpl(Collections.EMPTY_LIST.iterator());
   }

   /**
    * {@inheritDoc}
    */
   public ServletInputStream getInputStream() throws IOException
   {
      return new MockServletInputStream(data);
   }

   /**
    * {@inheritDoc}
    */
   public int getIntHeader(String name)
   {
      if (headers.get(name).size() > 0)
         return Integer.parseInt(headers.getFirst(name));
      return -1;
   }

   /**
    * Gets the local addr.
    *
    * @return the local addr
    */
   public String getLocalAddr()
   {
      return "127.0.0.1";
   }

   /**
    * {@inheritDoc}
    */
   public Locale getLocale()
   {
      return Locale.US;
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration getLocales()
   {
      return null;
   }

   /**
    * Gets the local name.
    *
    * @return the local name
    */
   public String getLocalName()
   {
      return "localhost";
   }

   /**
    * Gets the local port.
    *
    * @return the local port
    */
   public int getLocalPort()
   {
      return 80;
   }

   /**
    * {@inheritDoc}
    */
   public String getMethod()
   {
      return method;
   }

   /**
    * {@inheritDoc}
    */
   public String getParameter(String name)
   {
      Iterator<String> it = parameters.keySet().iterator();
      while (it.hasNext())
      {
         String key = it.next();
         if (key.equalsIgnoreCase(name))
         {
            ArrayList values = (ArrayList)parameters.get(key);
            if (values != null)
               return (String)values.get(0);
         }
      }
      return (null);
   }

   /**
    * {@inheritDoc}
    */
   public Map getParameterMap()
   {
      return parameters;
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration getParameterNames()
   {
      return new EnumerationImpl(parameters.keySet().iterator());
   }

   /**
    * {@inheritDoc}
    */
   public String[] getParameterValues(String name)
   {
      ArrayList<String> arr = new ArrayList<String>();
      Iterator it = parameters.keySet().iterator();
      while (it.hasNext())
      {

         String pname = (String)it.next();
         if (pname.equalsIgnoreCase(name))
            arr.add(parameters.get(name).get(0));
      }
      return arr.toArray(new String[arr.size()]);

   }

   /**
    * {@inheritDoc}
    */
   public String getPathInfo()
   {
      Matcher m = p.matcher(requestURL);
      if (!m.matches())
         throw new RuntimeException("Unable determine pathInfo.");
      String p = m.group(4);
      int q = p.indexOf('?');
      if (q > 0)
      {
         p = p.substring(0, q);
      }
      return '/' + p;
   }

   /**
    * {@inheritDoc}
    */
   public String getPathTranslated()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getProtocol()
   {
      return "HTTP/1.1";
   }

   /**
    * {@inheritDoc}
    */
   public String getQueryString()
   {
      if (requestURL == null)
         return null;
      int sep = requestURL.lastIndexOf('?');
      if (sep == -1)
         return null;
      return requestURL.substring(sep + 1);
   }

   /**
    * {@inheritDoc}
    */
   public BufferedReader getReader() throws IOException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getRealPath(String arg0)
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getRemoteAddr()
   {
      return "127.0.0.1";
   }

   /**
    * {@inheritDoc}
    */
   public String getRemoteHost()
   {
      return "localhost";
   }

   /**
    * Gets the remote port.
    *
    * @return the remote port
    */
   public int getRemotePort()
   {
      return 8080;
   }

   /**
    * {@inheritDoc}
    */
   public String getRemoteUser()
   {
      return "root";
   }

   /**
    * {@inheritDoc}
    */
   public RequestDispatcher getRequestDispatcher(String s)
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getRequestedSessionId()
   {
      return "sessionId";
   }

   /**
    * {@inheritDoc}
    */
   public String getRequestURI()
   {
      return getContextPath() + getServletPath() + getPathInfo();
   }

   /**
    * {@inheritDoc}
    */
   public StringBuffer getRequestURL()
   {
      if (requestURL == null)
         return null;
      return new StringBuffer(requestURL);
   }

   /**
    * {@inheritDoc}
    */
   public String getScheme()
   {
      return "http";
   }

   /**
    * {@inheritDoc}
    */
   public String getServerName()
   {
      Matcher m = p.matcher(requestURL);
      if (!m.matches())
         throw new RuntimeException("Unable determine server name.");
      return m.group(1);
   }

   /**
    * {@inheritDoc}
    */
   public int getServerPort()
   {
      Matcher m = p.matcher(requestURL);
      if (!m.matches())
         throw new RuntimeException("Unable determine request URI.");
      return Integer.valueOf(m.group(2));
   }

   /**
    * {@inheritDoc}
    */
   public String getServletPath()
   {
      return "";
   }

   /**
    * {@inheritDoc}
    */
   public HttpSession getSession()
   {
      return session;
   }

   /**
    * {@inheritDoc}
    */
   public HttpSession getSession(boolean b)
   {
      return session;
   }

   /**
    * {@inheritDoc}
    */
   public Principal getUserPrincipal()
   {
      return principal == null ? principal = new MockPrincipal("root") : principal;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRequestedSessionIdFromCookie()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRequestedSessionIdFromUrl()
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRequestedSessionIdFromURL()
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRequestedSessionIdValid()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSecure()
   {
      return secure;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isUserInRole(String role)
   {
      return "admin".equals(role);
   }

   /**
    * {@inheritDoc}
    */
   public void removeAttribute(String name)
   {
      attributes.remove(name);
   }

   /**
    * {@inheritDoc}
    */
   public void setAttribute(String name, Object object)
   {
      attributes.put(name, object);
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterEncoding(String enc) throws UnsupportedEncodingException
   {
   }

   /**
    * Sets the parameter.
    *
    * @param name the name
    * @param value the value
    */
   public void setParameter(String name, String value)
   {
      ArrayList arr = new ArrayList<String>();
      arr.add(value);
      parameters.put(name, arr);
   }

   public static Map<String, List<String>> parseQueryString(String rawQuery)
   {
      HashMap<String, List<String>> m = new HashMap<String, List<String>>();
      if (rawQuery == null || rawQuery.length() == 0)
         return m;
      int p = 0;
      int n = 0;
      while (n < rawQuery.length())
      {
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
            name = pair;
         else
         {
            name = pair.substring(0, eq);
            value = pair.substring(eq + 1);
         }

         if (m.get(name) == null)
         {
            List<String> arr = new ArrayList<String>();
            arr.add(value);
            m.put(name, arr);
         }
         else
         {
            List<String> arr = m.get(name);
            arr.add(value);
         }
         p = n + 1;
      }
      return m;
   }
}

@SuppressWarnings("unchecked")
class EnumerationImpl implements Enumeration
{

   private final Iterator iter;

   public EnumerationImpl(Iterator iter)
   {
      this.iter = iter;
   }

   public boolean hasMoreElements()
   {
      return iter.hasNext();
   }

   public Object nextElement()
   {
      return iter.next();
   }
}

class MockServletInputStream extends ServletInputStream
{

   private final InputStream data;

   public MockServletInputStream(InputStream data)
   {
      this.data = data;
   }

   @Override
   public int read() throws IOException
   {
      return data.read();
   }
}