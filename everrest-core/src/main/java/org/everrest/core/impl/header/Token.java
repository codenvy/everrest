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

/**
 * Token is any header part which contains only valid characters see
 * {@link HeaderHelper#isToken(String)} . Token is separated by ','
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class Token
{

   /**
    * Token.
    */
   private String token;

   /**
    * @param token a token
    */
   public Token(String token)
   {
      this.token = token.toLowerCase();
   }

   /**
    * @return the token in lower case
    */
   public String getToken()
   {
      return token;
   }

   /**
    * Check is to token is compatible.
    * 
    * @param other the token must be checked
    * @return true if token is compatible false otherwise
    */
   public boolean isCompatible(Token other)
   {
      if ("*".equals(token))
         return true;

      return token.equalsIgnoreCase(other.getToken());
   }

}
