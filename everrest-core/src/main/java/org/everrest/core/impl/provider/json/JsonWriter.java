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
package org.everrest.core.impl.provider.json;

import org.everrest.core.impl.provider.json.JsonUtils.JsonToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author andrew00x
 */
public class JsonWriter {

    /** Stack for control position in document. */
    private final JsonStack<JsonToken> stack;

    /** Writer. */
    private final Writer writer;

    /** Indicate is comma must be written before next object or value. */
    private boolean commaFirst;

    private String currentKey;

    private boolean writeNulls;

    /**
     * Constructs JsonWriter.
     *
     * @param writer
     *         Writer.
     */
    public JsonWriter(Writer writer) {
        this.writer = writer;
        this.stack = new JsonStack<>();
        this.commaFirst = false;
        this.writeNulls = false;
    }

    /**
     * Constructs JsonWriter.
     *
     * @param out
     *         OutputStream.
     */
    public JsonWriter(OutputStream out) {
        this(new OutputStreamWriter(out, JsonUtils.DEFAULT_CHARSET));
    }

    public final boolean isWriteNulls() {
        return writeNulls;
    }

    public final void setWriteNulls(boolean writeNulls) {
        this.writeNulls = writeNulls;
    }

    public void writeStartObject() throws JsonException {
        writeCurrentKey();
        JsonToken token = stack.peek();
        // Object can be stated after key with followed ':' or as array item.
        if (token != null && token != JsonToken.key && token != JsonToken.array) {
            throw new JsonException("Syntax error. Unexpected element '{'.");
        }
        try {
            if (commaFirst) {
                // needed ',' before
                writer.write(',');
            }
            writer.write('{');
            // if at the top of stack is 'key' then remove it.
            if (token == JsonToken.key) {
                stack.pop();
            }
            // remember new object opened
            stack.push(JsonToken.object);
            commaFirst = false;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void writeEndObject() throws JsonException {
        try {
            JsonToken token = stack.pop();
            if (token != JsonToken.object) {
                //System.out.println(token);
                // wrong JSON structure.
                throw new JsonException("Syntax error. Unexpected element '}'.");
            }
            writer.write('}');
            commaFirst = true;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void writeStartArray() throws JsonException {
        writeCurrentKey();
        JsonToken token = stack.peek();
        //if (token != JsonToken.key && token != JsonToken.array)
        if (token != null && token != JsonToken.key && token != JsonToken.array) {
            throw new JsonException("Syntax error. Unexpected element '['.");
        }
        try {
            if (commaFirst) {
                // needed ',' before
                writer.write(',');
            }
            writer.write('[');
            if (token == JsonToken.key) {
                // if at the top of stack is 'key' then remove it.
                stack.pop();
            }
            // remember new array opened
            stack.push(JsonToken.array);
            commaFirst = false;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void writeEndArray() throws JsonException {
        JsonToken token = stack.pop();
        try {
            if (token != JsonToken.array) {
                // wrong JSON structure
                throw new JsonException("Syntax error. Unexpected element ']'.");
            }
            writer.write(']');
            commaFirst = true;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void writeKey(String key) throws JsonException {
        if (key == null) {
            throw new JsonException("Key is null.");
        }

        if (currentKey != null) {
            throw new IllegalStateException();
        }

        JsonToken token = stack.peek();
        if (token != JsonToken.object) {
            throw new JsonException("Syntax error. Unexpected characters '" + key + "'.");
        }

        currentKey = key;
    }


    private void writeCurrentKey() throws JsonException {
        if (currentKey == null) {
            return;
        }
        try {
            if (commaFirst) {
                writer.write(',');
            }
            // create JSON representation for given string.
            writer.write(JsonUtils.getJsonString(currentKey));
            writer.write(':');
            commaFirst = false;
            stack.push(JsonToken.key);
            currentKey = null;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    public void writeString(String value) throws JsonException {
        writeCurrentKey();
        write(JsonUtils.getJsonString(value));
    }


    public void writeValue(long value) throws JsonException {
        writeCurrentKey();
        write(Long.toString(value));
    }


    public void writeValue(double value) throws JsonException {
        writeCurrentKey();
        write(Double.toString(value));
    }


    public void writeValue(boolean value) throws JsonException {
        writeCurrentKey();
        write(Boolean.toString(value));
    }


    public void writeNull() throws JsonException {
        if (writeNulls) {
            writeCurrentKey();
            write("null");
        } else {
            currentKey = null;
        }
    }

    /**
     * Write single String.
     *
     * @param value
     *         String.
     * @throws JsonException
     *         if any errors occurs.
     */
    private void write(String value) throws JsonException {
        JsonToken token = stack.peek();
        try {
            if (token != JsonToken.key && token != JsonToken.array) {
                throw new JsonException("Syntax error. Unexpected characters '" + value + "'.");
            }
            if (commaFirst) {
                writer.write(',');
            }
            writer.write(value);
            commaFirst = true;
            if (token == JsonToken.key) {
                // if at the top of stack is 'key' then remove it.
                stack.pop();
            }
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void flush() throws JsonException {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void close() throws JsonException {
        try {
            writer.close();
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

}
