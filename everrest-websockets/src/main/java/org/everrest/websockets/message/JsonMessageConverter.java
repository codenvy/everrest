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

/**
 * Implementation of MessageConverter that supports JSON messages.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JsonMessageConverter implements MessageConverter {
    @Override
    public <T extends Message> T fromString(String message, Class<T> clazz) throws MessageConversionException {
        try {
            final JsonParser parser = new JsonParser();
            parser.parse(new StringReader(message));
            final JsonValue json = parser.getJsonObject();
            return ObjectBuilder.createObject(clazz, json);
        } catch (JsonException e) {
            throw new MessageConversionException(e.getMessage(), e);
        }
    }

    @Override
    public String toString(Message output) throws MessageConversionException {
        try {
            final JsonValue jsonOutput = JsonGenerator.createJsonObject(output);
            final StringWriter writer = new StringWriter();
            final JsonWriter jsonWriter = new JsonWriter(writer);
            jsonOutput.writeTo(jsonWriter);
            jsonWriter.flush();
            return writer.toString();
        } catch (JsonException e) {
            throw new MessageConversionException(e.getMessage(), e);
        }
    }
}
