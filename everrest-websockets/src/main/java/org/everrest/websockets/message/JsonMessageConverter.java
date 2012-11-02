/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.everrest.websockets.message;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.JsonWriter;
import org.everrest.core.impl.provider.json.ObjectBuilder;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.CharBuffer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JsonMessageConverter implements MessageConverter
{
   @Override
   public InputMessage read(CharBuffer input) throws MessageConverterException
   {
      try
      {
         JsonParser parser = new JsonParser();
         parser.parse(new StringReader(input.toString()));
         JsonValue json = parser.getJsonObject();
         return ObjectBuilder.createObject(InputMessage.class, json);
      }
      catch (JsonException e)
      {
         throw new MessageConverterException(e.getMessage(), e);
      }
   }

   @Override
   public CharBuffer write(OutputMessage output) throws MessageConverterException
   {
      try
      {
         JsonValue jsonOutput = JsonGenerator.createJsonObject(output);
         StringWriter writer = new StringWriter();
         JsonWriter jsonWriter = new JsonWriter(writer);
         jsonOutput.writeTo(jsonWriter);
         jsonWriter.flush();
         return CharBuffer.wrap(writer.toString());
      }
      catch (JsonException e)
      {
         throw new MessageConverterException(e.getMessage(), e);
      }
   }
}
