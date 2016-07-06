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

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.List;
import java.util.Map;

import static org.everrest.core.impl.header.HeaderHelper.addQuotesIfHasWhitespace;

public class CacheControlHeaderDelegate implements RuntimeDelegate.HeaderDelegate<CacheControl> {

    @Override
    public CacheControl fromString(String header) {
        throw new UnsupportedOperationException("CacheControl used only for response headers.");
    }

    @Override
    public String toString(CacheControl header) {
        StringBuilder buff = new StringBuilder();
        if (!header.isPrivate()) {
            appendString(buff, "public");
        }
        if (header.isPrivate()) {
            appendWithParameters(buff, "private", header.getPrivateFields());
        }
        if (header.isNoCache()) {
            appendWithParameters(buff, "no-cache", header.getNoCacheFields());
        }
        if (header.isNoStore()) {
            appendString(buff, "no-store");
        }
        if (header.isNoTransform()) {
            appendString(buff, "no-transform");
        }
        if (header.isMustRevalidate()) {
            appendString(buff, "must-revalidate");
        }
        if (header.isProxyRevalidate()) {
            appendString(buff, "proxy-revalidate");
        }
        if (header.getMaxAge() >= 0) {
            appendString(buff, Integer.toString(header.getMaxAge()));
        }
        if (header.getSMaxAge() >= 0) {
            appendString(buff, Integer.toString(header.getSMaxAge()));
        }
        for (Map.Entry<String, String> entry : header.getCacheExtension().entrySet()) {
            appendWithSingleParameter(buff, entry.getKey(), entry.getValue());
        }
        return buff.toString();
    }

    /**
     * Adds single {@code String} to {@code StringBuilder}.
     *
     * @param buff
     *         the StringBuilder
     * @param s
     *         single String
     */
    private void appendString(StringBuilder buff, String s) {
        if (buff.length() > 0) {
            buff.append(',').append(' ');
        }

        buff.append(s);
    }

    /**
     * Adds single pair key=value to {@code StringBuilder}. If value contains whitespace then quotes will be added.
     *
     * @param buff
     *         the StringBuilder
     * @param key
     *         the key
     * @param value
     *         the value
     */
    private void appendWithSingleParameter(StringBuilder buff, String key, String value) {
        StringBuilder localBuff = new StringBuilder();
        localBuff.append(key);

        if (value != null && value.length() > 0) {
            localBuff.append('=').append(addQuotesIfHasWhitespace(value));
        }

        appendString(buff, localBuff.toString());
    }

    /**
     * Add pair key="value1, value2" to {@code StringBuilder}.
     *
     * @param buff
     *         the StringBuilder
     * @param key
     *         the key
     * @param values
     *         the collection of values
     */
    private void appendWithParameters(StringBuilder buff, String key, List<String> values) {
        appendString(buff, key);
        if (values.size() > 0) {
            StringBuilder localBuff = new StringBuilder();
            buff.append('=');
            buff.append('"');

            for (String value : values) {
                appendString(localBuff, value);
            }

            buff.append(localBuff.toString());
            buff.append('"');
        }
    }
}
