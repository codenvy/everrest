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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JsonHandler {

    /** The key. */
    private String key;

    /** JsonValue which is currently in process. */
    private JsonValue current;

    /** Stack of JsonValues. */
    private JsonStack<JsonValue> values;

    /** Constructs new JsonHandler. */
    public JsonHandler() {
        this.values = new JsonStack<JsonValue>();
    }

    /** {@inheritDoc} */
    public void characters(char[] characters) {
        if (current.isObject()) {
            current.addElement(key, parseCharacters(characters));
        } else if (current.isArray()) {
            current.addElement(parseCharacters(characters));
        }
    }

    /** {@inheritDoc} */
    public void endArray() {
        current = values.pop();
    }

    /** {@inheritDoc} */
    public void endObject() {
        current = values.pop();
    }

    /** {@inheritDoc} */
    public void key(String key) {
        this.key = key;
    }

    /** {@inheritDoc} */
    public void startArray() {
        ArrayValue o = new ArrayValue();
        if (current == null) {
            current = o;
        } else if (current.isObject()) {
            current.addElement(key, o);
        } else if (current.isArray()) {
            current.addElement(o);
        }
        values.push(current);
        current = o;
    }

    /** {@inheritDoc} */
    public void startObject() {
        if (current == null) {
            current = new ObjectValue();
            values.push(current);
            return;
        }
        ObjectValue o = new ObjectValue();
        if (current.isObject()) {
            current.addElement(key, o);
        } else if (current.isArray()) {
            current.addElement(o);
        }
        values.push(current);
        current = o;
    }

    /** Reset JSON events handler and prepare it for next usage. */
    public void reset() {
        current = null;
        key = null;
        values.clear();
    }

    /** {@inheritDoc} */
    public JsonValue getJsonObject() {
        return current;
    }

    /**
     * Parse characters array dependent of context.
     *
     * @param characters
     *         the characters array.
     * @return JsonValue.
     */
    private JsonValue parseCharacters(char[] characters) {
        String s = new String(characters);
        if (characters[0] == '"' && characters[characters.length - 1] == '"') {
            return new StringValue(s.substring(1, s.length() - 1));
        } else if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
            return new BooleanValue(Boolean.parseBoolean(s));
        } else if ("null".equalsIgnoreCase(s)) {
            return new NullValue();
        } else {
            char c = characters[0];
            if ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+') {
                // first try read as hex is start from '0'
                if (c == '0') {
                    if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                        try {
                            return new LongValue(Long.parseLong(s.substring(2), 16));
                        } catch (NumberFormatException e) {
                            // nothing to do!
                        }
                    } else {
                        // as oct long
                        try {
                            return new LongValue(Long.parseLong(s.substring(1), 8));
                        } catch (NumberFormatException e) {
                            // if fail, then it is not oct
                            try {
                                //try as dec long
                                return new LongValue(Long.parseLong(s));
                            } catch (NumberFormatException l) {
                                try {
                                    // and last try as double
                                    return new DoubleValue(Double.parseDouble(s));
                                } catch (NumberFormatException d) {
                                    // nothing to do!
                                }
                            }
                            // nothing to do!
                        }
                    }
                } else {
                    // if char set start not from '0'
                    try {
                        // try as long
                        return new LongValue(Long.parseLong(s));
                    } catch (NumberFormatException l) {
                        try {
                            // try as double if above failed
                            return new DoubleValue(Double.parseDouble(s));
                        } catch (NumberFormatException d) {
                            // nothing to do!
                        }
                    }
                }
            }
        }
        // if can't parse return as string
        return new StringValue(s);
    }

}
