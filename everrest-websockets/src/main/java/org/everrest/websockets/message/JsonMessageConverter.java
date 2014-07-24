/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
