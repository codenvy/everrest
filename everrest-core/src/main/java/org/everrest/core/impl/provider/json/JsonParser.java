/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider.json;

import org.everrest.core.impl.provider.json.JsonUtils.JsonToken;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.Charset;

import static org.everrest.core.util.StringUtils.contains;

public class JsonParser {
    private static final int END_OF_STREAM = 0;

    /** JsonHandler will serve events from parser. */
    private final JsonHandler eventHandler;

    /** Stack of JSON tokens. */
    private final JsonStack<JsonToken> stack;

    /** @see {@link java.io.PushbackReader}. */
    private PushbackReader pushbackReader;

    public JsonParser() {
        this(new JsonHandler());
    }

    protected JsonParser(JsonHandler eventHandler) {
        this.eventHandler = eventHandler;
        stack = new JsonStack<>();
    }


    public void parse(Reader reader) throws JsonException {
        pushbackReader = new PushbackReader(reader);
        eventHandler.reset();
        stack.clear();
        char c;
        while ((c = next()) != END_OF_STREAM) {
            if (c == '{') {
                readObject();
            } else if (c == '[') {
                readArray();
            } else {
                throw new JsonException(String.format("Syntax error. Unexpected '%s'. Must be '{'.", c));
            }
            c = assertNextIs(",]}");
            if (c != END_OF_STREAM) {
                pushBack(c);
            }
        }
        if (!stack.isEmpty()) {
            throw new JsonException("Syntax error. Missing one or more close bracket(s).");
        }
    }


    public void parse(InputStream stream) throws JsonException {
        parse(new InputStreamReader(stream, Charset.forName("UTF-8")));
    }

    /**
     * Get result of parsing.
     *
     * @return parsed JSON value
     */
    public JsonValue getJsonObject() {
        return eventHandler.getJsonObject();
    }

    /**
     * Read JSON object token, it minds all characters from '{' to '}'.
     *
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private void readObject() throws JsonException {
        char c;
        startObject();
        while (true) {
            switch (c = next()) {
                case END_OF_STREAM:
                    throw new JsonException("Syntax error. Unexpected end of object. Object must end by '}'.");
                case '}':
                    endObject();
                    return;
                case '{':
                    readObject();
                    break;
                case '[':
                    readArray();
                    break;
                case ']':
                    endArray();
                    break;
                case ',':
                    break;
                default:
                    pushBack(c);
                    readKey();
                    assertNextIs(':');
                    c = next();
                    pushBack(c);
                    if (c != '{' && c != '[') {
                        readValue();
                    }
                    break;
            }
        }
    }

    private void endObject() throws JsonException {
        if (JsonToken.object == stack.pop()) {
            eventHandler.endObject();
        } else {
            throw new JsonException("Syntax error. Unexpected end of object.");
        }
    }

    private void startObject() {
        eventHandler.startObject();
        stack.push(JsonToken.object);
    }

    /**
     * Read JSON array token, it minds all characters from '[' to ']'.
     *
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private void readArray() throws JsonException {
        char c;
        startArray();
        while (true) {
            switch (c = next()) {
                case END_OF_STREAM:
                    throw new JsonException("Syntax error. Unexpected end of array. Array must end by ']'.");
                case ']':
                    endArray();
                    return;
                case '[':
                    readArray();
                    break;
                case '{':
                    readObject();
                    break;
                case '}':
                    endObject();
                    break;
                case ',':
                    break;
                default:
                    pushBack(c);
                    readValue();
                    break;
            }
        }
    }

    private void startArray() {
        eventHandler.startArray();
        stack.push(JsonToken.array);
    }

    private void endArray() throws JsonException {
        if (JsonToken.array == stack.pop()) {
            eventHandler.endArray();
        } else {
            throw new JsonException("Syntax error. Unexpected end of array.");
        }
    }

    /**
     * Read key from stream.
     *
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private void readKey() throws JsonException {
        char c = next();
        if (c != '"') {
            throw new JsonException(String.format("Syntax error. Key must start from quote, but found '%s'.", c));
        }
        pushBack(c);
        String key = new String(nextString());
        if (key.length() == 2) {
            throw new JsonException("Missing key.");
        }
        eventHandler.key(key.substring(1, key.length() - 1));
    }

    /**
     * Read value from stream.
     *
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private void readValue() throws JsonException {
        char c = next();
        pushBack(c);
        if (c == '"') {
            eventHandler.characters(nextString());
        } else {
            CharArrayWriter charArrayWriter = new CharArrayWriter();
            while (true) {
                c = next();
                if (c == END_OF_STREAM) {
                    throw new JsonException("Unexpected end of stream.");
                } else if (contains("{[,]}\"", c)) {
                    break;
                }
                charArrayWriter.append(c);
            }
            pushBack(c);
            eventHandler.characters(charArrayWriter.toCharArray());
        }
        c = assertNextIs(",]}");
        pushBack(c);
    }

    /**
     * Get next char from stream, skipping whitespace and comments. Comments: One
     * line comment from // to end of line; Multi-line comments from &#8260;* to *&#8260;
     *
     * @return the next char.
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private char next() throws JsonException {
        try {
            int c;
            while ((c = pushbackReader.read()) != -1) {
                if (c == '/') {
                    c = pushbackReader.read();
                    if (c == '/') {
                        skipLine();
                    } else if (c == '*') {
                        skipComment();
                    }
                } else if (c > ' ') {
                    break;
                }
            }
            return (c == -1) ? END_OF_STREAM : (char)c;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    private void skipLine() throws IOException {
        int c;
        do {
            c = pushbackReader.read();
        } while (c != -1 && c != '\n' && c != '\r');
    }

    private void skipComment() throws IOException, JsonException {
        int c;
        while (true) {
            c = pushbackReader.read();
            if (c == '*') {
                c = pushbackReader.read();
                if (c == '/') {
                    break;
                }
            }
            if (c == -1) {
                throw new JsonException("Syntax error. Missing end of comment.");
            }
        }
    }

    /**
     * Get next char from stream.
     *
     * @return the next char.
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private char nextAny() throws JsonException {
        try {
            int c = pushbackReader.read();
            return (c == -1) ? END_OF_STREAM : (char)c;
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * Get next char from stream. And check is this char equals expected.
     *
     * @param expectedCharacter
     *         the expected char.
     * @return the next char.
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private char assertNextIs(char expectedCharacter) throws JsonException {
        char c = next();
        if (c == END_OF_STREAM || c == expectedCharacter) {
            return c;
        }
        throw new JsonException(String.format("Expected for '%s' but found '%s'.", expectedCharacter, c));
    }

    /**
     * Get next char from stream. And check is this char presents in given string.
     *
     * @param expectedCharacters
     *         the string.
     * @return the next char.
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private char assertNextIs(String expectedCharacters) throws JsonException {
        char c = next();
        if (c == END_OF_STREAM || contains(expectedCharacters, c)) {
            return c;
        }
        char[] chars = expectedCharacters.toCharArray();
        StringBuilder errorMessage = new StringBuilder("Expected ");
        for (int i = 0; i < chars.length; i++) {
            if (i > 0) {
                errorMessage.append(" or ");
            }
            errorMessage.append('\'').append(chars[i]).append('\'');
        }
        errorMessage.append(" but found '").append(c).append('\'');
        throw new JsonException(errorMessage.toString());
    }

    /**
     * Get array chars up to given and include it.
     *
     * @return the char array.
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private char[] nextString() throws JsonException {
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        char c = nextAny(); // read '"'
        charArrayWriter.append(c);
        while (true) {
            switch (c = nextAny()) {
                case END_OF_STREAM:
                case '\n':
                case '\r':
                    throw new JsonException("Syntax error. Unterminated string");
                case '\\':
                    switch (c = nextAny()) {
                        case END_OF_STREAM:
                        case '\n':
                        case '\r':
                            throw new JsonException("Syntax error. Unterminated string");
                        case 'n':
                            charArrayWriter.append('\n');
                            break;
                        case 'r':
                            charArrayWriter.append('\r');
                            break;
                        case 'b':
                            charArrayWriter.append('\b');
                            break;
                        case 't':
                            charArrayWriter.append('\t');
                            break;
                        case 'f':
                            charArrayWriter.append('\f');
                            break;
                        case 'u': // unicode
                            charArrayWriter.append(readUnicodeCharacter());
                            break;
                        default:
                            charArrayWriter.append(c);
                            break;
                    }
                    break;
                default:
                    charArrayWriter.append(c);
                    if (c == '"') {
                        return charArrayWriter.toCharArray();
                    }
                    break;
            }
        }
    }

    private char readUnicodeCharacter() throws JsonException {
        char[] buff = new char[4];
        try {
            int i = pushbackReader.read(buff);
            if (i != 4) {
                throw new JsonException("Unexpected end of stream.");
            }
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }

        String unicodeString = new String(buff);
        int c;
        try {
            c = Integer.parseInt(unicodeString, 16);
        } catch (NumberFormatException e) {
             throw new JsonException(String.format("Invalid unicode character %s", unicodeString));
        }
        return (char)c;
    }

    /**
     * Push back given char to stream.
     *
     * @param c
     *         the char for pushing back.
     * @throws JsonException
     *         if JSON document has wrong format or i/o error occurs.
     */
    private void pushBack(char c) throws JsonException {
        try {
            pushbackReader.unread(c);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }
}
