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
package org.everrest.core.impl.provider.json;

import org.everrest.core.impl.provider.json.JsonUtils.JsonToken;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonParser
{

   /** JsonHandler will serve events from parser. */
   private final JsonHandler eventHandler;

   /** Stack of JSON tokens. */
   private final JsonStack<JsonToken> stack;

   /** @see {@link java.io.PushbackReader}. */
   private PushbackReader pushbackReader;

   public JsonParser()
   {
      this(new JsonHandler());
   }

   protected JsonParser(JsonHandler eventHandler)
   {
      this.eventHandler = eventHandler;
      stack = new JsonStack<JsonToken>();
   }

   /**
    * {@inheritDoc}
    */
   public void parse(Reader reader) throws JsonException
   {
      pushbackReader = new PushbackReader(reader);
      eventHandler.reset();
      stack.clear();
      char c = 0;
      while ((c = next()) != 0)
      {
         if (c == '{')
         {
            readObject();
         }
         else if (c == '[')
         {
            readArray();
         }
         else
         {
            throw new JsonException("Syntax error. Unexpected '" + c + "'. Must be '{'.");
         }
      }
      if (!stack.isEmpty())
      {
         throw new JsonException("Syntax error. Missing one or more close bracket(s).");
      }
   }

   /**
    * {@inheritDoc}
    */
   public void parse(InputStream sream) throws JsonException
   {
      parse(new InputStreamReader(sream, JsonUtils.DEFAULT_CHARSET));
   }

   /**
    * Get result of parsing.
    * 
    * @return parsed JSON value
    */
   public JsonValue getJsonObject()
   {
      return eventHandler.getJsonObject();
   }

   /**
    * Read JSON object token, it minds all characters from '{' to '}'.
    * 
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private void readObject() throws JsonException
   {
      char c = 0;
      // inform handler about start of object
      eventHandler.startObject();
      stack.push(JsonToken.object);
      for (;;)
      {
         switch (c = next())
         {
            case 0 :
               throw new JsonException("Syntax error. Unexpected end of object. Object must end by '}'.");
            case '{' :
               readObject();
               break;
            case '}' :
               // inform handler about end of object
               eventHandler.endObject();
               // check is allowed end of object now
               if (JsonToken.object != stack.pop())
               {
                  throw new JsonException("Syntax error. Unexpected end of object.");
               }
               // check is allowed char after end of json object
               switch (c = next())
               {
                  // end of stream
                  case 0 :
                     break;
                  case ',' :
                  case ']' :
                  case '}' :
                     back(c);
                     break;
                  default :
                     // must not happen
                     throw new JsonException("Syntax error. Excpected " + "for ',' or ']' or '}' but found '" + c
                        + "'.");
               }
               // end for(;;)
               return;
            case '[' :
               readArray();
               break;
            case ',' :
               // nothing to do just must not be default is switch
               break;
            default :
               back(c);
               // all characters from start object to ':' - key.
               readKey();
               next(':');
               c = next();
               back(c);
               // object/array/value
               if (c != '{' && c != '[')
               {
                  readValue();
               }
               break;
         }
      }
   }

   /**
    * Read JSON array token, it minds all characters from '[' to ']'.
    * 
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private void readArray() throws JsonException
   {
      char c = 0;
      // inform handler about start of array
      eventHandler.startArray();
      stack.push(JsonToken.array);
      for (;;)
      {
         switch (c = next())
         {
            case 0 :
               throw new JsonException("Syntax error. Unexpected end of array. Array must end by ']'.");
            case ']' :
               // inform handler about end of array
               eventHandler.endArray();
               // check is allowed end of array now
               if (JsonToken.array != stack.pop())
               {
                  throw new JsonException("Syntax error. Unexpected end of array.");
               }
               // check is allowed char after end of json array
               switch (c = next())
               {
                  // end of stream
                  case 0 :
                     break;
                  case ',' :
                  case ']' :
                  case '}' :
                     back(c);
                     break;
                  default :
                     // must not happen
                     throw new JsonException("Syntax error. Excpected for ',' or ']' or '}' but found '" + c + "'.");
               }
               // end for(;;)
               return;
            case '[' :
               readArray();
               break;
            case '{' :
               readObject();
               break;
            case ',' :
               // nothing to do just must not be default
               break;
            default :
               back(c);
               readValue();
               break;
         }
      }
   }

   /**
    * Read key from stream.
    * 
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private void readKey() throws JsonException
   {
      char c = next();
      if (c != '"')
      {
         throw new JsonException("Syntax error. Key must start from quote, but found '" + c + "'.");
      }
      back(c);
      String s = new String(nextString());
      // if key as ""
      if (s.length() == 2)
      {
         throw new JsonException("Missing key.");
      }
      eventHandler.key(s.substring(1, s.length() - 1));
   }

   /**
    * Read value from stream.
    * 
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private void readValue() throws JsonException
   {
      char c = next();
      back(c);
      if (c == '"')
      {
         // value will be read as string
         eventHandler.characters(nextString());
      }
      else
      {
         // not string (numeric or boolean or null)
         CharArrayWriter cw = new CharArrayWriter();
         while ("{[,]}\"".indexOf(c = next()) < 0)
         {
            // Bug : WS-66
            if (c == 0)
            {
               throw new JsonException("Unexpected end of stream.");
            }
            cw.append(c);
         }
         back(c);
         eventHandler.characters(cw.toCharArray());
      }
      c = next(",]}");
      back(c);
   }

   /**
    * Get next char from stream, skipping whitespace and comments. Comments: One
    * line comment from // to end of line; Multi-line comments from / and * to *
    * and /
    * 
    * @return the next char.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private char next() throws JsonException
   {
      try
      {
         int c = 0;
         while ((c = pushbackReader.read()) != -1)
         {
            if (c == '/')
            {
               c = pushbackReader.read();
               if (c == '/')
               {
                  do
                  {
                     c = pushbackReader.read();
                  }
                  while (c != -1 && c != '\n' && c != '\r');
               }
               else if (c == '*')
               {
                  for (;;)
                  {
                     c = pushbackReader.read();
                     if (c == '*')
                     {
                        c = pushbackReader.read();
                        if (c == '/')
                        {
                           break;
                        }
                     }
                     if (c == -1)
                     {
                        throw new JsonException("Syntax error. Missing end of comment.");
                     }
                  }
               }
               else
               {
                  back((char)c);
                  return '/';
               }
            }
            else if (c == -1 || c > ' ')
            {
               break;
            }
         }
         return (c == -1) ? 0 : (char)c;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * Get next char from stream.
    * 
    * @return the next char.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private char nextAny() throws JsonException
   {
      try
      {
         int c = pushbackReader.read();
         return (c == -1) ? 0 : (char)c;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * Get next char from stream. And check is this char equals expected.
    * 
    * @param c the expected char.
    * @return the next char.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private char next(char c) throws JsonException
   {
      char n = next();
      if (n != c)
      {
         throw new JsonException("Expected for '" + c + "' but found '" + n + "'.");
      }
      return n;
   }

   /**
    * Get next char from stream. And check is this char presents in given
    * string.
    * 
    * @param s the string.
    * @return the next char.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private char next(String s) throws JsonException
   {
      char n = next();
      // if char present in string
      if (s.indexOf(n) >= 0)
      {
         return n;
      }
      // else error
      char[] ch = s.toCharArray();
      StringBuilder sb = new StringBuilder();
      int i = 0;
      for (char c : ch)
      {
         if (i > 0)
         {
            sb.append(" or ");
         }
         i++;
         sb.append('\'').append(c).append('\'');
      }
      throw new JsonException("Expected for " + sb.toString() + " but found '" + n + "'.");
   }

   /**
    * Get next n characters from stream.
    * 
    * @param n the number of characters.
    * @return the array of characters.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private char[] next(int n) throws JsonException
   {
      char[] buff = new char[n];
      try
      {
         int i = pushbackReader.read(buff);
         if (i == -1)
         {
            throw new JsonException("Unexpected end of stream.");
         }
         return buff;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * Get array chars up to given and include it.
    * 
    * @return the char array.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private char[] nextString() throws JsonException
   {
      CharArrayWriter cw = new CharArrayWriter();
      char c = nextAny(); // read '"'
      cw.append(c);
      for (;;)
      {
         switch (c = nextAny())
         {
            case 0 :
            case '\n' :
            case '\r' :
               throw new JsonException("Syntax error. Unterminated string.");
            case '\\' :
               switch (c = nextAny())
               {
                  case 0 :
                  case '\n' :
                  case '\r' :
                     throw new JsonException("Syntax error. Unterminated string");
                  case 'n' :
                     cw.append('\n');
                     break;
                  case 'r' :
                     cw.append('\r');
                     break;
                  case 'b' :
                     cw.append('\b');
                     break;
                  case 't' :
                     cw.append('\t');
                     break;
                  case 'f' :
                     cw.append('\f');
                     break;
                  case 'u' : // unicode
                     String s = new String(next(4));
                     cw.append((char)Integer.parseInt(s, 16));
                     break;
                  default :
                     cw.append(c);
                     break;
               }
               break;
            default :
               cw.append(c);
               if (c == '"')
               {
                  return cw.toCharArray();
               }
               break;
         }
      }
   }

   /**
    * Push back given char to stream.
    * 
    * @param c the char for pushing back.
    * @throws JsonException if JSON document has wrong format or i/o error
    *            occurs.
    */
   private void back(char c) throws JsonException
   {
      try
      {
         pushbackReader.unread(c);
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

}
