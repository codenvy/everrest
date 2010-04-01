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

import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: MediaTypeHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class MediaTypeHeaderDelegate extends AbstractHeaderDelegate<MediaType>
{

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<MediaType> support()
   {
      return MediaType.class;
   }

   /**
    * {@inheritDoc}
    */
   public MediaType fromString(String header)
   {
      if (header == null)
         throw new IllegalArgumentException();

      try
      {
         int p = header.indexOf('/');
         int col = header.indexOf(';');

         String type = null;
         String subType = null;

         if (p < 0 && col < 0) // no '/' and ';'
            return new MediaType(header, null);
         else if (p > 0 && col < 0) // there is no ';' so no parameters
            return new MediaType(HeaderHelper.removeWhitespaces(header.substring(0, p)), HeaderHelper
               .removeWhitespaces(header.substring(p + 1)));
         else if (p < 0 && col > 0)
         { // there is no '/' but present ';'
            type = HeaderHelper.removeWhitespaces(header.substring(0, col));
            // sub-type is null
         }
         else
         { // presents '/' and ';'
            type = HeaderHelper.removeWhitespaces(header.substring(0, p));
            subType = header.substring(p + 1, col);
         }

         Map<String, String> m = new HeaderParameterParser().parse(header);
         return new MediaType(type, subType, m);

      }
      catch (ParseException e)
      {
         throw new IllegalArgumentException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public String toString(MediaType mime)
   {
      StringBuffer sb = new StringBuffer();
      sb.append(mime.getType()).append('/').append(mime.getSubtype());

      for (Entry<String, String> entry : mime.getParameters().entrySet())
      {
         sb.append(';').append(entry.getKey()).append('=');
         HeaderHelper.appendWithQuote(sb, entry.getValue());
      }

      return sb.toString();
   }

}
