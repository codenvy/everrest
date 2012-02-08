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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class HeaderParameterParser
{
   /** Parameter separator. */
   private static final char SEPARATOR = ';';

   /** Current position in the parsed string. */
   private int pos = 0;

   /** Token's start. */
   private int i1 = 0;

   /** Token's end. */
   private int i2 = 0;

   /** String to be parsed. */
   private char[] chars = null;

   /** Parsed string length. */
   private int length = 0;

   /**
    * Parse header string for parameters.
    *
    * @param header source header string
    * @return header parameter
    * @throws ParseException if string can't be parsed or contains illegal
    * characters
    */
   public Map<String, String> parse(String header) throws ParseException
   {
      init(header);

      if (pos < 0)
      {
         return null;
      }

      pos++; // skip first ';'
      Map<String, String> m = null;
      while (hasChars())
      {

         String name = readToken(new char[]{'=', SEPARATOR});

         String value = null;
         if (hasChars() && chars[pos] == '=')
         {
            pos++; // skip '='
            if (chars[pos] == '"') // quoted string
            {
               value = readQuotedString();
            }
            else
            {
               value = readToken(new char[]{SEPARATOR});
            }
         }

         if (hasChars() && chars[pos] == SEPARATOR)
         {
            pos++; // skip ';'
         }

         if (name != null && name.length() > 0)
         {
            if (m == null)
            {
               m = new HashMap<String, String>();
            }
            m.put(name, value);
         }

      }
      return m;
   }

   /**
    * @param removeQuotes must leading and trailing quotes be skipped
    * @return parsed token
    */
   private String getToken(boolean removeQuotes)
   {
      // leading whitespace
      while ((i1 < i2) && Character.isWhitespace(chars[i1]))
      {
         i1++;
      }
      // tail whitespace
      while ((i2 > i1) && Character.isWhitespace(chars[i2 - 1]))
      {
         i2--;
      }

      // remove quotes
      if (removeQuotes && chars[i1] == '"' && chars[i2 - 1] == '"')
      {
         i1++;
         i2--;
      }

      String token = null;
      if (i2 > i1)
      {
         token = new String(chars, i1, i2 - i1);
      }

      return token;
   }

   /**
    * Check are there any character to be parsed.
    *
    * @return true if there are unparsed characters, false otherwise
    */
   private boolean hasChars()
   {
      return pos < length;
   }

   /**
    * Initialize character array for parsing.
    *
    * @param source source string for parsing
    */
   private void init(String source)
   {
      // looking for start parameters position
      // e.g. text/plain ; charsert=utf-8
      pos = source.indexOf(SEPARATOR);
      if (pos < 0)
      // header string does not contains parameters
      {
         return;
      }
      chars = source.toCharArray();
      length = chars.length;
      i1 = 0;
      i2 = 0;
   }

   /**
    * Process quoted string, it minds remove escape characters for quotes.
    *
    * @return processed string
    * @throws ParseException if string can't be parsed
    * @see HeaderHelper#filterEscape(String)
    */
   private String readQuotedString() throws ParseException
   {
      i1 = pos;
      i2 = pos;

      // indicate was previous character '\'
      boolean escape = false;
      // indicate is final '"' already found
      boolean qoute = false;

      while (hasChars())
      {
         char c = chars[pos];

         if (c == SEPARATOR && !qoute)
         {
            break;
         }

         if (c == '"' && !escape)
         {
            qoute = !qoute;
         }

         escape = !escape && c == '\\';
         pos++;
         i2++;
      }

      if (qoute)
      {
         throw new ParseException("String must be ended with qoute.", pos);
      }

      String token = getToken(true);
      if (token != null)
      {
         token = HeaderHelper.filterEscape(getToken(true));
      }
      return token;
   }

   /**
    * Read token from source string, token is not quoted string and does not
    * contains any separators. See <a
    * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html">HTTP1.1
    * specification</a>.
    *
    * @param terminators characters which indicate end of token
    * @return token
    * @throws ParseException if token contains illegal characters
    */
   private String readToken(char[] terminators) throws ParseException
   {
      i1 = pos;
      i2 = pos;
      while (hasChars())
      {
         char c = chars[pos];
         if (checkChar(c, terminators))
         {
            break;
         }
         pos++;
         i2++;
      }

      String token = getToken(false);
      if (token != null)
      {
         // check is it valid token
         int err = -1;
         if ((err = HeaderHelper.isToken(token)) != -1)
         {
            throw new ParseException("Token '" + token + "' contains not legal characters at " + err, err);
         }
      }

      return token;
   }

   /**
    * Check does char array <tt>chs</tt> contains char <tt>c</tt>.
    *
    * @param c char
    * @param chs char array
    * @return true if char array contains character <tt>c</tt>, false otherwise
    */
   private boolean checkChar(char c, char[] chs)
   {
      for (int i = 0; i < chs.length; i++)
      {
         if (c == chs[i])
         {
            return true;
         }
      }
      return false;
   }
}
