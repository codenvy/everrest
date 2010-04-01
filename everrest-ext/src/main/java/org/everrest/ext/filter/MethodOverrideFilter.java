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
package org.everrest.ext.filter;

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;

/**
 * Override HTTP request method by method specified in X-HTTP-Method-Override
 * header.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MethodOverrideFilter.java 301 2009-10-19 14:41:18Z aparfonov $
 */
@Filter
public class MethodOverrideFilter implements RequestFilter
{

   /**
    * {@inheritDoc}
    */
   public void doFilter(GenericContainerRequest request)
   {
      String method = request.getRequestHeaders().getFirst(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE);
      if (method != null)
         request.setMethod(method);
   }

}
