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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonWriter
{

   /** Stack for control position in document. */
   private final JsonStack<JsonToken> stack;

   /** Writer. */
   private final Writer writer;

   /** Indicate is comma must be written before next object or value. */
   private boolean commaFirst;

   /**
    * Constructs JsonWriter.
    * 
    * @param writer Writer.
    */
   public JsonWriter(Writer writer)
   {
      this.writer = writer;
      this.stack = new JsonStack<JsonToken>();
      this.commaFirst = false;
   }

   /**
    * Constructs JsonWriter.
    * 
    * @param out OutputStream.
    * @throws UnsupportedEncodingException
    */
   public JsonWriter(OutputStream out)
   {
      this(new OutputStreamWriter(out, JsonUtils.DEFAULT_CHARSET));
   }

   /**
    * {@inheritDoc}
    */
   public void writeStartObject() throws JsonException
   {
      JsonToken token = stack.peek();
      // Object can be stated after key with followed ':' or as array item.
      if (token != null && token != JsonToken.key && token != JsonToken.array)
      {
         throw new JsonException("Syntax error. Unexpected element '{'.");
      }
      try
      {
         if (commaFirst)
         {
            // needed ',' before
            writer.write(',');
         }
         writer.write('{');
         // if at the top of stack is 'key' then remove it.
         if (token == JsonToken.key)
         {
            stack.pop();
         }
         // remember new object opened
         stack.push(JsonToken.object);
         commaFirst = false;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeEndObject() throws JsonException
   {
      try
      {
         JsonToken token = stack.pop();
         if (token != JsonToken.object)
         {
            System.out.println(token);
            // wrong JSON structure.
            throw new JsonException("Sysntax error. Unexpected element '}'.");
         }
         writer.write('}');
         commaFirst = true;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeStartArray() throws JsonException
   {
      JsonToken token = stack.peek();
      if (token != JsonToken.key && token != JsonToken.array)
      {
         throw new JsonException("Sysntax error. Unexpected element '['.");
      }
      try
      {
         if (commaFirst)
         {
            // needed ',' before
            writer.write(',');
         }
         writer.write('[');
         if (token == JsonToken.key)
         {
            // if at the top of stack is 'key' then remove it.
            stack.pop();
         }
         // remember new array opened
         stack.push(JsonToken.array);
         commaFirst = false;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeEndArray() throws JsonException
   {
      JsonToken token = stack.pop();
      try
      {
         if (token != JsonToken.array)
         {
            // wrong JSON structure
            throw new JsonException("Sysntax error. Unexpected element ']'.");
         }
         writer.write(']');
         commaFirst = true;
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeKey(String key) throws JsonException
   {
      if (key == null)
      {
         throw new JsonException("Key is null.");
      }

      JsonToken token = stack.peek();
      if (token != JsonToken.object)
      {
         throw new JsonException("Sysntax error. Unexpected characters '" + key + "'.");
      }
      try
      {
         if (commaFirst)
         {
            writer.write(',');
         }
         // create JSON representation for given string.
         writer.write(JsonUtils.getJsonString(key));
         writer.write(':');
         commaFirst = false;
         stack.push(JsonToken.key);
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeString(String value) throws JsonException
   {
      write(JsonUtils.getJsonString(value));
   }

   /**
    * {@inheritDoc}
    */
   public void writeValue(long value) throws JsonException
   {
      write(Long.toString(value));
   }

   /**
    * {@inheritDoc}
    */
   public void writeValue(double value) throws JsonException
   {
      write(Double.toString(value));
   }

   /**
    * {@inheritDoc}
    */
   public void writeValue(boolean value) throws JsonException
   {
      write(Boolean.toString(value));
   }

   /**
    * {@inheritDoc}
    */
   public void writeNull() throws JsonException
   {
      write("null");
   }

   /**
    * Write single String.
    * 
    * @param value String.
    * @throws JsonException if any errors occurs.
    */
   private void write(String value) throws JsonException
   {
      JsonToken token = stack.peek();
      try
      {
         if (token != JsonToken.key && token != JsonToken.array)
         {
            throw new JsonException("Sysntax error. Unexpected characters '" + value + "'.");
         }
         if (commaFirst)
         {
            writer.write(',');
         }
         writer.write(value);
         commaFirst = true;
         if (token == JsonToken.key)
         {
            // if at the top of stack is 'key' then remove it.
            stack.pop();
         }
      }
      catch (IOException e)
      {
         throw new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush() throws JsonException
   {
      try
      {
         writer.flush();
      }
      catch (IOException e)
      {
         new JsonException(e.getMessage(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void close() throws JsonException
   {
      try
      {
         writer.close();
      }
      catch (IOException e)
      {
         new JsonException(e.getMessage(), e);
      }
   }

}
