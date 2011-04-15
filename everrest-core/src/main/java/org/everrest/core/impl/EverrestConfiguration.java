/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.core.impl;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EverrestConfiguration
{
   public static final String EVERREST_HTTP_METHOD_OVERRIDE = "org.everrest.http.method.override";

   public static final String EVERREST_NORMALIZE_URI = "org.everrest.normalize.uri";

   public static final String EVERREST_CHECK_SECURITY = "org.everrest.security";

   protected boolean checkSecurity = true;

   protected boolean httpMethodOverride = true;

   protected boolean normalizeUri = false;

   public boolean isCheckSecurity()
   {
      return checkSecurity;
   }

   public boolean isHttpMethodOverride()
   {
      return httpMethodOverride;
   }

   public boolean isNormalizeUri()
   {
      return normalizeUri;
   }

   public void setCheckSecurity(boolean checkSecurity)
   {
      this.checkSecurity = checkSecurity;
   }

   public void setHttpMethodOverride(boolean httpMethodOverride)
   {
      this.httpMethodOverride = httpMethodOverride;
   }

   public void setNormalizeUri(boolean normalizeUri)
   {
      this.normalizeUri = normalizeUri;
   }

}
