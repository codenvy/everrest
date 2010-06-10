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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.InitialProperties;
import org.everrest.core.impl.EnvironmentContext;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ContextParameterResolver.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class ContextParameterResolver extends ParameterResolver<Context>
{

   /**
    * Known essences that can be passed to method via parameters that has
    * &#64;Context annotation.
    */
   private enum CONTEXT_PARAMS {
      /**
       * @see HttpHeaders
       */
      HTTP_HEADERS,
      /**
       * @see SecurityContext
       */
      SECURITY_CONTEXT,
      /**
       * @see Request
       */
      REQUEST,
      /**
       * @see UriInfo
       */
      URI_INFO,
      /**
       * @see Providers
       */
      PROVIDERS,
      /**
       * @see InitialProperties
       */
      PROPERTIES
   }

   /**
    * Mapping from class name to member of {@link CONTEXT_PARAMS}.
    */
   private static final Map<String, CONTEXT_PARAMS> CONTEXT_PARAMETERS_MAP = new HashMap<String, CONTEXT_PARAMS>(4);

   static
   {
      CONTEXT_PARAMETERS_MAP.put(HttpHeaders.class.getName(), CONTEXT_PARAMS.HTTP_HEADERS);
      CONTEXT_PARAMETERS_MAP.put(SecurityContext.class.getName(), CONTEXT_PARAMS.SECURITY_CONTEXT);
      CONTEXT_PARAMETERS_MAP.put(Request.class.getName(), CONTEXT_PARAMS.REQUEST);
      CONTEXT_PARAMETERS_MAP.put(UriInfo.class.getName(), CONTEXT_PARAMS.URI_INFO);
      CONTEXT_PARAMETERS_MAP.put(Providers.class.getName(), CONTEXT_PARAMS.PROVIDERS);
      CONTEXT_PARAMETERS_MAP.put(InitialProperties.class.getName(), CONTEXT_PARAMS.PROPERTIES);
   }

   /**
    * @param contextParam {@link Context}
    */
   ContextParameterResolver(Context contextParam)
   {
      // @Context annotation has not value.
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception
   {
      String className = parameter.getParameterClass().getName();
      CONTEXT_PARAMS cp = CONTEXT_PARAMETERS_MAP.get(className);
      if (cp != null)
      {
         switch (cp)
         {
            case HTTP_HEADERS :
               return context.getHttpHeaders();
            case SECURITY_CONTEXT :
               return context.getSecurityContext();
            case REQUEST :
               return context.getRequest();
            case URI_INFO :
               return context.getUriInfo();
            case PROVIDERS :
               return context.getProviders();
            case PROPERTIES :
               return context.getInitialProperties();
         }
      }
      // For servlet container environment context contains HttpServletRequest,
      // HttpServletResponse, ServletConfig, ServletContext
      return EnvironmentContext.getCurrent().get(parameter.getParameterClass());
   }

}
