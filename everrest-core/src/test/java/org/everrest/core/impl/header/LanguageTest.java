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

import org.everrest.core.impl.BaseTest;

import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class LanguageTest extends BaseTest
{

   public void testFromString()
   {
      String header = "en-GB";
      Locale locale = Language.getLocale(header);
      assertEquals("en", locale.getLanguage());
      assertEquals("GB", locale.getCountry());

      header = "en-US,      en-GB";
      locale = Language.getLocale(header);
      assertEquals("en", locale.getLanguage());
      assertEquals("US", locale.getCountry());
   }

   public void testToString()
   {
      HeaderDelegate<Locale> delegate = RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class);
      Locale locale = new Locale("");
      assertNull(delegate.toString(locale));
      locale = new Locale("*");
      assertNull(delegate.toString(locale));
      locale = new Locale("en", "GB");
      assertEquals("en-gb", delegate.toString(locale));
   }

}
