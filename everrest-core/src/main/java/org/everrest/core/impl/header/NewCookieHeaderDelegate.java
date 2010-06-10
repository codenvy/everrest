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

import javax.ws.rs.core.NewCookie;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: NewCookieHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public class NewCookieHeaderDelegate extends AbstractHeaderDelegate<NewCookie>
{

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<NewCookie> support()
   {
      return NewCookie.class;
   }

   /**
    * {@inheritDoc}
    */
   public NewCookie fromString(String header)
   {
      throw new UnsupportedOperationException("NewCookie used only for response headers.");
   }

   /**
    * {@inheritDoc}
    */
   public String toString(NewCookie cookie)
   {
      StringBuffer sb = new StringBuffer();
      sb.append(cookie.getName()).append('=').append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getValue()));

      sb.append(';').append("Version=").append(cookie.getVersion());

      if (cookie.getComment() != null)
         sb.append(';').append("Comment=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getComment()));

      if (cookie.getDomain() != null)
         sb.append(';').append("Domain=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getDomain()));

      if (cookie.getPath() != null)
         sb.append(';').append("Path=").append(HeaderHelper.addQuotesIfHasWhitespace(cookie.getPath()));

      if (cookie.getMaxAge() != -1)
         sb.append(';').append("Max-Age=").append(HeaderHelper.addQuotesIfHasWhitespace("" + cookie.getMaxAge()));

      if (cookie.isSecure())
         sb.append(';').append("Secure");

      return sb.toString();
   }

}
