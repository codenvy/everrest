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

package org.everrest.core.tools;

import org.everrest.core.impl.ContainerRequest;

import java.io.InputStream;
import java.net.URI;
import java.security.Principal;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;

/**
 * For test purposes only. Need this to emulate authenticated user.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
class SecurityContextRequest extends ContainerRequest
{
   private final SecurityContext sctx;

   public SecurityContextRequest(String method, URI requestUri, URI baseUri, InputStream entityStream,
      MultivaluedMap<String, String> httpHeaders, SecurityContext sctx)
   {
      super(method, requestUri, baseUri, entityStream, httpHeaders);
      this.sctx = sctx;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getAuthenticationScheme()
   {
      return sctx != null ? sctx.getAuthenticationScheme() : null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Principal getUserPrincipal()
   {
      return sctx != null ? sctx.getUserPrincipal() : null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isSecure()
   {
      return sctx != null ? sctx.isSecure() : false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isUserInRole(String role)
   {
      return sctx != null ? sctx.isUserInRole(role) : false;
   }

}
