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
import org.everrest.core.header.QualityValue;

import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AcceptLanguageHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class AcceptLanguageHeaderDelegate extends AbstractHeaderDelegate<AcceptLanguage>
{

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<AcceptLanguage> support()
   {
      return AcceptLanguage.class;
   }

   /**
    * {@inheritDoc}
    */
   public AcceptLanguage fromString(String header)
   {
      if (header == null)
         throw new IllegalArgumentException();

      try
      {
         header = HeaderHelper.removeWhitespaces(header);
         String tag;
         Map<String, String> m = null;

         int p = header.indexOf(';');
         if (p != -1 && p < header.length() - 1)
         { // header has quality value
            tag = header.substring(0, p);
            m = new HeaderParameterParser().parse(header);
         }
         else
         { // no quality value
            tag = header;
         }

         p = tag.indexOf('-');
         String primaryTag;
         String subTag = null;

         if (p != -1 && p < tag.length() - 1)
         { // has sub-tag
            primaryTag = tag.substring(0, p);
            subTag = tag.substring(p + 1);
         }
         else
         { // no sub-tag
            primaryTag = tag;
         }

         if (m == null) // no quality value
            return new AcceptLanguage(new Locale(primaryTag, subTag != null ? subTag : ""));
         else
            return new AcceptLanguage(new Locale(primaryTag, subTag != null ? subTag : ""), HeaderHelper
               .parseQualityValue(m.get(QualityValue.QVALUE)));

      }
      catch (ParseException e)
      {
         throw new IllegalArgumentException("Accept language header malformed");
      }
   }

   /**
    * {@inheritDoc}
    */
   public String toString(AcceptLanguage language)
   {
      throw new UnsupportedOperationException("Accepted language header used only for request.");
   }

}
