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

import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * Reflection for HTTP language tag.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: Language.java 285 2009-10-15 16:21:30Z aparfonov $
 */
public class Language
{

   /**
    * See {@link RuntimeDelegate#createHeaderDelegate(Class)}.
    */
   private static final HeaderDelegate<Locale> DELEGATE =
      RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class);

   /**
    * @see {@link Locale}
    */
   private final Locale locale;

   /**
    * Constructs new instance of Language.
    * 
    * @param locale @see {@link Locale}
    */
   public Language(Locale locale)
   {
      this.locale = locale;
   }

   /**
    * Create {@link Locale} from Language Tag string.
    * 
    * @param language string representation of Language Tag, See {@link <a
    *          href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec3.10"
    *          >HTTP/1.1 documentation</a>}.
    * @return {@link Locale}
    */
   public static Locale getLocale(String language)
   {
      return DELEGATE.fromString(language);
   }

   /**
    * Get primary-tag of language tag, e. g. if Language tag 'en-gb' then 'en' is
    * primary-tag. See {@link <a
    * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec3.10"
    * >HTTP/1.1 documentation</a>}.
    * 
    * @return the primary-tag of Language tag
    */
   public String getPrimaryTag()
   {
      return locale.getLanguage().toLowerCase();
   }

   /**
    * Get sub-tag of language tag, e. g. if Language tag 'en-gb' then 'gb' is
    * sub-tag. See {@link <a
    * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec3.10"
    * >HTTP/1.1 documentation</a>}.
    * 
    * @return the sub-tag of Language tag
    */
   public String getSubTag()
   {
      return locale.getCountry().toLowerCase();
   }

   /**
    * @return @see {@link Locale}
    */
   public Locale getLocale()
   {
      return locale;
   }

   /**
    * Check is two Language instance is compatible.
    * 
    * @param other checked language
    * @return true if given Language is compatible with current false otherwise
    */
   public boolean isCompatible(Language other)
   {
      if (other == null)
         return false;
      if ("*".equals(getPrimaryTag()))
         return true;
      // primary tags match and sub-tag not specified (any matches)
      // if 'accept-language' is 'en' then 'en-us' and 'en-gb' is matches
      if (getPrimaryTag().equalsIgnoreCase(other.getPrimaryTag()) && "".equals(getSubTag()))
         return true;

      return this.toString().equalsIgnoreCase(other.toString());
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(getPrimaryTag());
      if (!"".equals(getSubTag()))
         sb.append('-').append(getSubTag());

      return sb.toString();
   }

}
