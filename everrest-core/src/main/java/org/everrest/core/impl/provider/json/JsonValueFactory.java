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

import static org.everrest.core.util.StringUtils.charAtIs;

/**
 * Creates JsonValue from string, type of created JsonValue based on content of string.
 */
public class JsonValueFactory {
    public JsonValue createJsonValue(String string) {
        if (charAtIs(string, 0, '"') && charAtIs(string, string.length() - 1, '"')) {
            return new StringValue(string.substring(1, string.length() - 1));
        } else if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
            return new BooleanValue(Boolean.parseBoolean(string));
        } else if ("null".equalsIgnoreCase(string)) {
            return new NullValue();
        } else {
            char c = string.charAt(0);
            if ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+') {
                if (c == '0') {
                    if (string.length() > 2 && (string.charAt(1) == 'x' || string.charAt(1) == 'X')) {
                        try {
                            return new LongValue(Long.parseLong(string.substring(2), 16));
                        } catch (NumberFormatException notHexNumber) {
                        }
                    } else {
                        try {
                            return new LongValue(Long.parseLong(string.substring(1), 8));
                        } catch (NumberFormatException notOctNumber) {
                            try {
                                return new LongValue(Long.parseLong(string));
                            } catch (NumberFormatException notLong) {
                                try {
                                    return new DoubleValue(Double.parseDouble(string));
                                } catch (NumberFormatException notNumber) {
                                }
                            }
                        }
                    }
                } else {
                    try {
                        return new LongValue(Long.parseLong(string));
                    } catch (NumberFormatException notLong) {
                        try {
                            return new DoubleValue(Double.parseDouble(string));
                        } catch (NumberFormatException notNumber) {
                        }
                    }
                }
            }
        }
        return new StringValue(string);
    }
}
