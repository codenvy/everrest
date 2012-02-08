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
package org.everrest.core.impl.header;

import org.everrest.core.header.AbstractHeaderDelegate;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AcceptMediaTypeHeaderDelegate.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class AcceptMediaTypeHeaderDelegate extends AbstractHeaderDelegate<AcceptMediaType>
{
   /** {@inheritDoc} */
   @Override
   public Class<AcceptMediaType> support()
   {
      return AcceptMediaType.class;
   }

   /** {@inheritDoc} */
   public AcceptMediaType fromString(String header)
   {
      if (header == null)
      {
         throw new IllegalArgumentException();
      }

      MediaType mediaType = MediaType.valueOf(header);

      return new AcceptMediaType(mediaType.getType(), mediaType.getSubtype(), mediaType.getParameters());

   }

   /** {@inheritDoc} */
   public String toString(AcceptMediaType acceptedMediaType)
   {
      throw new UnsupportedOperationException("Accepted media type header used only for request.");
   }
}
