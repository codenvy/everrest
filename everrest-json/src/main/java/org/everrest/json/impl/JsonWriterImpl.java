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
package org.everrest.json.impl;

import org.everrest.json.JsonException;
import org.everrest.json.JsonWriter;
import org.everrest.json.impl.JsonUtils.JsonToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Stack;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: JsonWriterImpl.java 34417 2009-07-23 14:42:56Z dkatayev $
 */
public class JsonWriterImpl implements JsonWriter
{

   /**
    * Stack for control position in document.
    */
   private final Stack<JsonToken> jsonTokens = new Stack<JsonToken>();

   /**
    * Writer.
    */
   private final Writer writer;

   /**
    * Indicate is current value is the first, if not before value must be
    * written comma.
    */
   private boolean commaFirst = false;

   /**
    * Constructs JsonWriter.
    * 
    * @param writer Writer.
    */
   public JsonWriterImpl(Writer writer)
   {
      this.writer = writer;
   }

   /**
    * Constructs JsonWriter.
    * 
    * @param out OutputStream.
    * @throws UnsupportedEncodingException
    */
   public JsonWriterImpl(OutputStream out)
   {
      this(new OutputStreamWriter(out, JsonUtils.DEFAULT_CHARSET));
   }

   /**
    * {@inheritDoc}
    */
   public void writeStartObject() throws JsonException
   {
      if (!jsonTokens.isEmpty())
      {
         // Object can be stated after key with followed ':' or as array item.
         if (jsonTokens.peek() != JsonToken.key && jsonTokens.peek() != JsonToken.array)
            throw new JsonException("Syntax error. Unexpected element '{'.");
      }
      try
      {
         if (commaFirst) // needed ',' before
            writer.write(',');
         writer.write('{');
         // if at the top of stack is 'key' then remove it.
         if (!jsonTokens.isEmpty() && jsonTokens.peek() == JsonToken.key)
            jsonTokens.pop();
         jsonTokens.push(JsonToken.object); // remember new object opened
         commaFirst = false;
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeEndObject() throws JsonException
   {
      try
      {
         if (jsonTokens.pop() != JsonToken.object) // wrong JSON structure.
            throw new JsonException("Sysntax error. Unexpected element '}'.");
         writer.write('}');
         commaFirst = true;
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeStartArray() throws JsonException
   {
      if (jsonTokens.isEmpty() || (jsonTokens.peek() != JsonToken.key && jsonTokens.peek() != JsonToken.array))
         throw new JsonException("Sysntax error. Unexpected element '['..");
      try
      {
         if (commaFirst) // needed ',' before
            writer.write(',');
         writer.write('[');
         if (jsonTokens.peek() == JsonToken.key)
            // if at the top of stack is 'key' then remove it.
            jsonTokens.pop();
         jsonTokens.push(JsonToken.array); // remember new array opened
         commaFirst = false;
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeEndArray() throws JsonException
   {
      try
      {
         if (jsonTokens.pop() != JsonToken.array) // wrong JSON structure
            throw new JsonException("Sysntax error. Unexpected element ']'.");
         writer.write(']');
         commaFirst = true;
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeKey(String key) throws JsonException
   {
      if (key == null)
         throw new JsonException("Key is null.");
      if (jsonTokens.isEmpty() || jsonTokens.peek() != JsonToken.object)
         throw new JsonException("Sysntax error. Unexpected characters '" + key + "'." + jsonTokens);
      try
      {
         if (commaFirst)
            writer.write(',');
         // create JSON representation for given string.
         writer.write(JsonUtils.getJsonString(key));
         writer.write(':');
         commaFirst = false;
         jsonTokens.push(JsonToken.key);
      }
      catch (IOException e)
      {
         throw new JsonException(e);
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
         new JsonException(e);
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
         new JsonException(e);
      }
   }

   /**
    * Write single String.
    * 
    * @param value String.
    * @throws JsonException if any errors occurs.
    */
   private void write(String value) throws JsonException
   {
      try
      {
         if (jsonTokens.isEmpty() || (jsonTokens.peek() != JsonToken.key && jsonTokens.peek() != JsonToken.array))
            throw new JsonException("Sysntax error. Unexpected characters '" + value + "'.");
         if (commaFirst)
            writer.write(',');
         writer.write(value);
         commaFirst = true;
         if (jsonTokens.peek() == JsonToken.key)
            jsonTokens.pop();
      }
      catch (IOException e)
      {
         throw new JsonException(e);
      }
   }
}
