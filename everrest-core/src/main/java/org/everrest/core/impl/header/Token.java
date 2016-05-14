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

/**
 * Token is any header part which contains only valid characters see {@link HeaderHelper#isToken(String)} . Token is separated by ','
 *
 * @author andrew00x
 */
public class Token {
    /** Token. */
    private final String token;

    /**
     * @param token
     *         a token
     */
    public Token(String token) {
        this.token = token.toLowerCase();
    }

    /** @return the token in lower case */
    public String getToken() {
        return token;
    }

    /**
     * Check is to token is compatible.
     *
     * @param other
     *         the token must be checked
     * @return true if token is compatible false otherwise
     */
    public boolean isCompatible(Token other) {
        return "*".equals(token) || token.equalsIgnoreCase(other.getToken());
    }

    public boolean isCompatible(String other) {
        return other != null && ("*".equals(token) || token.equalsIgnoreCase(other));
    }

    @Override
    public String toString() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Token)) {
            return false;
        }

        Token other = (Token)o;
        return token.equals(other.token);
    }

    @Override
    public int hashCode() {
        int hashcode = 8;
        return hashcode * 31 + token.hashCode();
    }
}
