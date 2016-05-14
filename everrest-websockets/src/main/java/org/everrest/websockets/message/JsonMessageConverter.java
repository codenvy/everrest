/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.JsonWriter;

import java.io.StringReader;
import java.io.StringWriter;

import static org.everrest.core.impl.provider.json.JsonGenerator.createJsonObject;
import static org.everrest.core.impl.provider.json.ObjectBuilder.createObject;

/**
 * @author andrew00x
 */
public class JsonMessageConverter {
    public <T extends Message> T fromString(String message, Class<T> clazz) throws JsonException {
        final JsonParser parser = new JsonParser();
        parser.parse(new StringReader(message));
        final JsonValue json = parser.getJsonObject();
        return createObject(clazz, json);
    }

    public String toString(Message output) throws JsonException {
        final JsonValue jsonOutput = createJsonObject(output);
        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);
        jsonOutput.writeTo(jsonWriter);
        return writer.toString();
    }
}
