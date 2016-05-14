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
package org.everrest.core.impl.header;

import org.everrest.core.header.QualityValue;

import java.text.ParseException;
import java.util.Map;

import static org.everrest.core.impl.header.HeaderHelper.isToken;
import static org.everrest.core.impl.header.HeaderHelper.parseQualityValue;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

public class AcceptToken implements QualityValue {
    private static final char PARAMETERS_SEPARATOR = ';';

    public static AcceptToken valueOf(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        try {
            int separator = scan(value, PARAMETERS_SEPARATOR);
            boolean hasParameters = charAtIs(value, separator, PARAMETERS_SEPARATOR);
            String token = hasParameters ? value.substring(0, separator).trim() : value.trim();
            token = token.trim();

            int i;
            if ((i = isToken(token)) != -1) {
                throw new IllegalArgumentException(String.format("Not valid character at index %d in %s", i, token));
            }

            if (hasParameters) {
                Map<String, String> param = new HeaderParameterParser().parse(value);
                if (param.containsKey(QVALUE)) {
                    return new AcceptToken(token, parseQualityValue(param.get(QVALUE)));
                }
            }

            return new AcceptToken(token);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final Token token;
    /** Quality value factor. */
    private final float qValue;

    /**
     * Create AcceptToken with default quality value 1.0 .
     *
     * @param token
     *         a token
     */
    public AcceptToken(String token) {
        this(token, DEFAULT_QUALITY_VALUE);
    }

    /**
     * Create AcceptToken with specified quality value.
     *
     * @param token
     *         a token
     * @param qValue
     *         a quality value
     */
    public AcceptToken(String token, float qValue) {
        this.token = new Token(token);
        this.qValue = qValue;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public float getQvalue() {
        return qValue;
    }

    public boolean isCompatible(AcceptToken other) {
        return isCompatible(other.getToken());
    }

    public boolean isCompatible(Token other) {
        return token.isCompatible(other);
    }

    public boolean isCompatible(String other) {
        return token.isCompatible(other);
    }

    @Override
    public String toString() {
        return token + ";q=" + qValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcceptToken)) {
            return false;
        }

        AcceptToken other = (AcceptToken)o;
        return Float.compare(other.qValue, qValue) == 0 && token.equals(other.token);
    }

    @Override
    public int hashCode() {
        int hashcode = 8;
        hashcode = hashcode * 31 + (qValue == 0.0F ? 0 : Float.floatToIntBits(qValue));
        hashcode = hashcode * 31 + token.hashCode();
        return hashcode;
    }
}
