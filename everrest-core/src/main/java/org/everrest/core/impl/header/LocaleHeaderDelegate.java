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

import java.util.Locale;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class LocaleHeaderDelegate extends AbstractHeaderDelegate<Locale>
{

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<Locale> support()
   {
      return Locale.class;
   }

   /**
    * {@inheritDoc}
    */
   public Locale fromString(String header)
   {
      if (header == null)
         throw new IllegalArgumentException();

      header = HeaderHelper.removeWhitespaces(header);
      int p;
      // Can be set multiple content language, the take first one
      if ((p = header.indexOf(',')) > 0)
         header = header.substring(0, p);

      p = header.indexOf('-');
      if (p != -1 && p < header.length() - 1)
         return new Locale(header.substring(0, p), header.substring(p + 1));
      else
         return new Locale(header);
   }

   /**
    * {@inheritDoc}
    */
   public String toString(Locale locale)
   {
      String lan = locale.getLanguage();
      // For output if language does not set correctly then ignore it.
      if ("".equals(lan) || "*".equals(lan))
         return null;

      String con = locale.getCountry();
      if ("".equals(lan))
         return lan.toLowerCase();

      return lan.toLowerCase() + "-" + con.toLowerCase();
   }

}
