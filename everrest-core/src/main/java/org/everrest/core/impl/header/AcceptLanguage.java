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

import org.everrest.core.header.QualityValue;

import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class AcceptLanguage extends Language implements QualityValue
{

   /**
    * Default accepted language, it minds any language is acceptable.
    */
   public static final AcceptLanguage DEFAULT = new AcceptLanguage(new Locale("*"));

   /**
    * Quality value for 'accepted' HTTP headers, e. g. en-gb;0.9
    */
   private final float qValue;

   /**
    * See {@link RuntimeDelegate#createHeaderDelegate(Class)}.
    */
   private static final HeaderDelegate<AcceptLanguage> DELEGATE =
      RuntimeDelegate.getInstance().createHeaderDelegate(AcceptLanguage.class);

   /**
    * Creates a new instance of AcceptedLanguage by parsing the supplied string.
    * 
    * @param header accepted language string
    * @return AcceptedLanguage
    */
   public static AcceptLanguage valueOf(String header)
   {
      return DELEGATE.fromString(header);
   }

   /**
    * Constructs new instance of accepted language with default quality value.
    * 
    * @param locale the language
    */
   public AcceptLanguage(Locale locale)
   {
      super(locale);
      qValue = DEFAULT_QUALITY_VALUE;
   }

   /**
    * Constructs new instance of accepted language with quality value.
    * 
    * @param locale the language
    * @param qValue quality value
    */
   public AcceptLanguage(Locale locale, float qValue)
   {
      super(locale);
      this.qValue = qValue;
   }

   // QualityValue

   /**
    * {@inheritDoc}
    */
   public float getQvalue()
   {
      return qValue;
   }

}
