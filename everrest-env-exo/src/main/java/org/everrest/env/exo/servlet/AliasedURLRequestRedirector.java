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
package org.everrest.env.exo.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class AliasedURLRequestRedirector implements Filter
{

   //TODO
   private static String START_ESCAPED = "%7B$";

   private static String END_ESCAPED = "%7D";

   private static String START = "{$";

   private static String END = "}";

   Map<String, String> replaceMap = new HashMap<String, String>();

   /**
    * Filter configuration.
    */
   //  private FilterConfig conf;

   //  private String contextName;

   /**
    * Filter initialization method.
    * 
    * @param conf filter configuration
    * @throws ServletException exception
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   @SuppressWarnings("unchecked")
   public void init(FilterConfig conf) throws ServletException
   {
      //    this.contextName = conf.getServletContext().getServletContextName();
      //    this.conf = conf;
      Enumeration<String> enumeration = conf.getInitParameterNames();
      while (enumeration.hasMoreElements())
      {
         String key = (String)enumeration.nextElement();
         String val = conf.getInitParameter(key);
         replaceMap.put(key, val);
      }
   }

   /**
    * Filter finalization method.
    * 
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
   }

   /**
    * The main filter method. Wraps the original http request with the custom
    * wrapper and calls chain.
    * 
    * @param req original request
    * @param res original response
    * @param chain filter chain
    * @throws IOException exception
    * @throws ServletException exception
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
    *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
      ServletException
   {
      chain.doFilter(createRequestWrapper(req), res);
   }

   /**
    * Locates PageProvider component in the container, gets map of the URL
    * parameters to replace, then creates the custom wrapper and passes the map
    * to it.
    * 
    * @param req original request
    * @return
    */
   private ServletRequest createRequestWrapper(ServletRequest req)
   {
      //    replaceMap.put("context", contextName);
      return new UrlReplacerWrapper((HttpServletRequest)req, replaceMap);
   }

   /**
    * The custom http request wrapper that is designed to substitute given
    * parameters in URLs with corresponding values. Parameters are marked at an
    * URL in the form: {$parameter-name}
    * 
    */
   public class UrlReplacerWrapper extends HttpServletRequestWrapper
   {

      /**
       * Map with substitutes.
       */
      private final Map<String, String> replaceMap;

      /**
       * Generated path info.
       */
      private String pathInfo;

      /**
       * Generated request uri.
       */
      private String requestUri;

      /**
       * Generated request url.
       */
      private StringBuffer requestUrl;

      /**
       * @param request original request
       * @param replaceMap replace map
       */
      public UrlReplacerWrapper(HttpServletRequest request, Map<String, String> replaceMap)
      {
         super(request);
         this.replaceMap = replaceMap;
         pathInfo = replaceByMap(super.getPathInfo());
         requestUri = replaceByMap(super.getRequestURI());
         StringBuffer sb = super.getRequestURL();
         if (sb != null)
            requestUrl = new StringBuffer(replaceByMap(sb.toString()));
         else
            requestUrl = null;
      }

      /**
       * Actual URL substitution method.
       * 
       * @param path
       * @return
       */
      //TODO
      private String replaceByMap(String path)
      {
         if (path == null)
            return null;
         //      if (path.indexOf(START) < 0 || path.indexOf(START_ESCAPED)< 0)
         //        return path;
         String result = path;
         for (Iterator<String> i = replaceMap.keySet().iterator(); i.hasNext();)
         {
            String name = i.next();
            String value = replaceMap.get(name);
            if (value != null)
            {
               if (path.indexOf(START) > 0)
               {
                  result = result.replace(START + name + END, value);
               }
               if (path.indexOf(START_ESCAPED) > 0)
               {
                  result = result.replace(START_ESCAPED + name + END_ESCAPED, value);
               }
            }
         }
         return result;
      }

      /**
       * Overridden method.
       * 
       * @return actual path info
       * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
       */
      public String getPathInfo()
      {
         return pathInfo;
      }

      /**
       * Overridden method.
       * 
       * @return actual URI
       * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
       */
      public String getRequestURI()
      {
         return requestUri;
      }

      /**
       * Overridden method.
       * 
       * @return actual URL
       * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
       */
      public StringBuffer getRequestURL()
      {
         return requestUrl;
      }

   }

}
