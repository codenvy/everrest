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
package org.everrest.core.impl.provider.json;

import org.everrest.core.impl.provider.json.JsonUtils.JsonToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.everrest.core.impl.provider.json.JsonUtils.JsonToken.array;
import static org.everrest.core.impl.provider.json.JsonUtils.JsonToken.key;
import static org.everrest.core.impl.provider.json.JsonUtils.JsonToken.object;

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
        this(new OutputStreamWriter(out, Charset.forName("UTF-8")));
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
        if (token == null || token == key || token == array) {
            try {
                if (commaFirst) {
                    writer.write(',');
                }
                writer.write('{');
                if (token == key) {
                    stack.pop();
                }
                stack.push(object);
                commaFirst = false;
            } catch (IOException e) {
                throw new JsonException(e.getMessage(), e);
            }
        } else {
            throw new JsonException("Syntax error. Unexpected element '{'.");
        }
    }


    public void writeEndObject() throws JsonException {
        try {
            JsonToken token = stack.pop();
            if (token == object) {
                writer.write('}');
                commaFirst = true;
            } else {
                throw new JsonException("Syntax error. Unexpected element '}'.");
            }
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void writeStartArray() throws JsonException {
        writeCurrentKey();
        JsonToken token = stack.peek();
        if (token == null || token == key || token == array) {
            try {
                if (commaFirst) {
                    writer.write(',');
                }
                writer.write('[');
                if (token == key) {
                    stack.pop();
                }
                stack.push(array);
                commaFirst = false;
            } catch (IOException e) {
                throw new JsonException(e.getMessage(), e);
            }
        } else {
            throw new JsonException("Syntax error. Unexpected element '['.");
        }
    }


    public void writeEndArray() throws JsonException {
        JsonToken token = stack.pop();
        try {
            if (token == array) {
                writer.write(']');
                commaFirst = true;
            } else {
                throw new JsonException("Syntax error. Unexpected element ']'.");
            }
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    public void writeKey(String key) throws JsonException {
        if (isNullOrEmpty(key)) {
            throw new JsonException("Key is null or empty");
        }

        key = key.trim();
        if (key.isEmpty()) {
            throw new JsonException("Empty key");
        }

        if (currentKey != null) {
            throw new JsonException("Syntax error. Sequence of two keys");
        }

        JsonToken token = stack.peek();
        if (token == object) {
            currentKey = key;
        } else {
            throw new JsonException(String.format("Syntax error. Unexpected characters '%s'.", key));
        }
    }


    private void writeCurrentKey() throws JsonException {
        if (currentKey == null) {
            return;
        }
        try {
            if (commaFirst) {
                writer.write(',');
            }
            writer.write(JsonUtils.getJsonString(currentKey));
            writer.write(':');
            commaFirst = false;
            stack.push(key);
            currentKey = null;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /** @deprecated use {@link #writeValue(String)}*/
    public void writeString(String value) throws JsonException {
        writeValue(value);
    }

    public void writeValue(String value) throws JsonException {
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
            if (token == key || token == array) {
                if (commaFirst) {
                    writer.write(',');
                }
                writer.write(value);
                commaFirst = true;
                if (token == key) {
                    stack.pop();
                }
            } else {
                throw new JsonException(String.format("Syntax error. Unexpected characters '%s'.", value));
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
