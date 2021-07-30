/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.header;

import org.everrest.core.util.StringUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Character.isWhitespace;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.charAtIsNot;
import static org.everrest.core.util.StringUtils.scan;

public class HeaderParameterParser {
    private static final char   PARAMS_SEPARATOR  = ';';
    private static final String NAME_TERMINATORS  = "=;";
    private static final String VALUE_TERMINATORS = ";";

    /** Current position in the parsed string. */
    private int pos;

    /** Parsed String */
    private String source;

    /** Length of parsed string */
    private int sourceLength;

    /**
     * Parses header string to map of parameters.
     *
     * @param header
     *         header header string
     * @return header parameter
     * @throws ParseException
     *         if string can't be parsed or contains illegal characters
     */
    public Map<String, String> parse(String header) throws ParseException {
        init(header);

        Map<String, String> parameters = new HashMap<>();

        pos++; // skip first ';'
        while (hasRemainingChars()) {
            String name = readToken(NAME_TERMINATORS);
            String value = null;
            if (charAtIs(source, pos, '=')) {
                pos++; // skip '='
                if (charAtIs(source, pos, '"')) {
                    value = readQuotedString();
                } else if (hasRemainingChars()) {
                    value = readToken(VALUE_TERMINATORS);
                }
            }

            if (charAtIs(source, pos, PARAMS_SEPARATOR)) {
                pos++; // skip ';'
            }

            if (!isNullOrEmpty(name)) {
                parameters.put(name, value);
            }

        }
        return parameters;
    }

    private boolean hasRemainingChars() {
        return pos < sourceLength;
    }

    private void init(String header) {
        pos = scan(header, PARAMS_SEPARATOR);
        if (charAtIsNot(header, pos, PARAMS_SEPARATOR)) {
            return;
        }
        source = header;
        sourceLength = header.length();
    }

    private String readQuotedString() throws ParseException {
        int startOfToken = pos;
        int endOfToken = pos;

        // indicate was previous character '\'
        boolean escape = false;
        // indicate is final '"' already found
        boolean inQuotes = false;

        while (hasRemainingChars()) {
            if (!inQuotes && charAtIs(source, pos, PARAMS_SEPARATOR)) {
                break;
            }

            if (!escape && charAtIs(source, pos, '"')) {
                inQuotes = !inQuotes;
            }

            escape = !escape && charAtIs(source, pos, '\\');

            pos++;
            endOfToken++;
        }

        if (inQuotes) {
            throw new ParseException("String must be ended with quote.", pos);
        }

        String token = readToken(startOfToken, endOfToken);
        if (token != null) {
            token = HeaderHelper.removeQuoteEscapes(token);
        }
        return token;
    }

    /**
     * Reads token from header string, token is not quoted string and does not contains any separators.
     * See <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html">HTTP1.1 specification</a>.
     *
     * @param terminators
     *         characters which indicate end of token
     * @return token
     * @throws ParseException
     *         if token contains illegal characters
     */
    private String readToken(String terminators) throws ParseException {
        int startOfToken = pos;
        int endOfToken = pos;
        while (hasRemainingChars()) {
            char c = source.charAt(pos);
            if (StringUtils.contains(terminators, c)) {
                break;
            }
            pos++;
            endOfToken++;
        }

        String token = readToken(startOfToken, endOfToken);
        if (token != null) {
            int err;
            if ((err = HeaderHelper.isToken(token)) != -1) {
                throw new ParseException(String.format("Token '%s' contains not legal characters at %d", token, err), err);
            }
        }

        return token;
    }

    private String readToken(int startOfToken, int endOfToken) {
        while ((startOfToken < endOfToken) && isWhitespace(source.charAt(startOfToken))) {
            startOfToken++;
        }
        while ((endOfToken > startOfToken) && isWhitespace(source.charAt(endOfToken - 1))) {
            endOfToken--;
        }

        if (charAtIs(source, startOfToken, '"') && charAtIs(source, endOfToken - 1, '"')) {
            startOfToken++;
            endOfToken--;
        }

        String token = null;
        if (endOfToken > startOfToken) {
            token = source.substring(startOfToken, endOfToken);
        }

        return token;
    }
}
