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
package org.everrest.core.impl.resource;

/**
 * Describe the Path annotation, see {@link javax.ws.rs.Path}.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: PathValue.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class PathValue
{

   /**
    * URI template, see {@link javax.ws.rs.Path#value()} .
    */
   private final String path;

   /**
    * @param path URI template
    */
   public PathValue(String path)
   {
      this.path = path;
   }

   /**
    * @return URI template string
    */
   public String getPath()
   {
      return path;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return "( " + path + " )";
   }

}
