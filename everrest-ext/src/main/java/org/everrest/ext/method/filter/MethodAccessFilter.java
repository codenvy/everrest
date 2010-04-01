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
package org.everrest.ext.method.filter;

import org.everrest.core.Filter;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.resource.GenericMethodResource;

import java.lang.annotation.Annotation;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Contract of this class thats constrains access to the resource method that
 * use JSR-250 security common annotations. See also https://jsr250.dev.java.net .
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MethodAccessFilter.java 301 2009-10-19 14:41:18Z aparfonov $
 */
@Filter
public class MethodAccessFilter implements MethodInvokerFilter
{

   /**
    * Check does <tt>method</tt> contains one on of security annotations
    * PermitAll, DenyAll, RolesAllowed.
    * 
    * @see PermitAll
    * @see DenyAll
    * @see RolesAllowed {@inheritDoc}
    */
   public void accept(GenericMethodResource method) throws WebApplicationException
   {
      for (Annotation a : method.getMethod().getAnnotations())
      {
         Class<?> ac = a.annotationType();

         if (ac == PermitAll.class)
         {

            // all users allowed to call method
            return;

         }
         if (ac == DenyAll.class)
         {

            // nobody allowed to call method
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());

         }
         if (ac == RolesAllowed.class)
         {

            SecurityContext context = ApplicationContextImpl.getCurrent().getSecurityContext();
            for (String role : getAllowedRoles(a))
               if (context.isUserInRole(role))
                  return;

            // user is not in allowed roles
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
         }
      }
   }

   /**
    * Extract roles from {@link RolesAllowed} annotation.
    * 
    * @param roles See {@link RolesAllowed}
    * @return roles
    */
   private static String[] getAllowedRoles(Annotation roles)
   {
      return ((RolesAllowed)roles).value();
   }

}
