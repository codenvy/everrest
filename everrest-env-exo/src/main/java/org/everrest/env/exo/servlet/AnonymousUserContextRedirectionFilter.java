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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checks out if username present in HttpServletRequest then initializes
 * SessionProvider by getting current credentials from AuthenticationService and
 * keeps SessionProvider in ThreadLocalSessionProviderService. Otherwise
 * redirect request to alternative URL. Alternative web application can ask
 * about authentication again or not and gives or denies access to requested
 * resource. Filter requires parameter <code>context-name</code>, otherwise
 * ServletException will be thrown.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AnonymousUserContextRedirectionFilter implements Filter
{

   /**
    * context-name.
    */
   private final static String CONTEXT_NAME_PARAMETER = "context-name";

   /**
    * Logger.
    */
   private final static Log LOG = ExoLogger.getLogger("ws.AnonymousUserContextRedirectionFilter");

   /**
    * The name of context.
    */
   private String contextName;

   /**
    * {@inheritDoc}
    */
   public void destroy()
   {
      // nothing to do.
   }

   /**
    * {@inheritDoc}
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
      ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      String user = httpRequest.getRemoteUser();

      if (LOG.isDebugEnabled())
         LOG.debug("Current user '" + user + "'.");

      if (user != null)
      {
         filterChain.doFilter(request, response);
      }
      else
      {

         if (LOG.isDebugEnabled())
            LOG.debug("Redirect user to context '" + contextName + "'.");

         String pathInfo = httpRequest.getPathInfo();
         String query = httpRequest.getQueryString();

         /* Problem with LinkGenerator required to do this!
          * It is necessary to encode URI before redirect, otherwise
          * we get invalid URL (if it contains not ASCII characters).
          * When client (MS Word, for example) we get unparsed 'plus'
          * in URL in WebDAV server.  Currently it works and best
          * solution for now.
          *
          * ******************************************************
          * NOTE: We are not care about query parameters here!!!
          * 
          * j2ee documentation says about method HttpServletRequest#getQueryString():
          * "The value is not decoded by the container."
          * This string must be encoded by client, for LinkGenerator.
          */
         ((HttpServletResponse)response).sendRedirect(encodeURL(contextName + pathInfo)
            + (query != null ? "?" + query : ""));

      }
   }

   /**
    * Encode URL by URLEncoder#encode(url), then replace all '+' by '%20' .
    * 
    * @param url source String.
    * @return encoded String.
    * @throws UnsupportedEncodingException if encoding is unsupported.
    */
   private static String encodeURL(String url) throws UnsupportedEncodingException
   {

      StringBuffer sb = new StringBuffer();
      String[] paths = url.split("/");
      for (int i = 0; i < paths.length; i++)
      {
         if ("".equals(paths[i]))
            continue;

         String t = URLEncoder.encode(paths[i], "UTF-8");
         t = t.replace("+", "%20");
         sb.append('/').append(t);

      }

      return sb.toString();
   }

   /**
    * Get context name. It must be specified as init parameter. {@inheritDoc}
    */
   public void init(FilterConfig filterConfig) throws ServletException
   {
      contextName = filterConfig.getInitParameter(CONTEXT_NAME_PARAMETER);
      if (contextName == null)
      {
         LOG.error("AnonymousUserContextRedirectionFilter is not deployed. Set Init-param '" + CONTEXT_NAME_PARAMETER
            + " pointed to the target context name in the web.xml");
         throw new ServletException("Filter error. Init-param '" + CONTEXT_NAME_PARAMETER + "' is null.");
      }
   }

}
