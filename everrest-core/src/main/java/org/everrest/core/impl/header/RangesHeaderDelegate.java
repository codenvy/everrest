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
package org.everrest.core.impl.header;

import org.everrest.core.impl.header.Ranges.Range;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.ArrayList;
import java.util.List;

import static org.everrest.core.util.StringUtils.charAtIsNot;
import static org.everrest.core.util.StringUtils.scan;

/**
 * @author andrew00x
 */
public class RangesHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Ranges> {

    @Override
    public Ranges fromString(String value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        if (!value.startsWith("bytes")) {
            throw new IllegalArgumentException(String.format("Invalid byte range: %s", value));
        }

        int eq = scan(value, '=');
        if (charAtIsNot(value, eq, '=')) {
            throw new IllegalArgumentException(String.format("Invalid byte range: %s", value));
        }

        value = value.substring(eq + 1);

        String[] tokens = value.split(",");
        List<Range> ranges = new ArrayList<>();
        for (String token : tokens) {
            long start = 0;
            long end = -1L;
            token = token.trim();
            int dash = scan(token, '-');
            if (charAtIsNot(token, dash, '-')) {
                throw new IllegalArgumentException("Invalid byte range.");
            } else if (dash == 0) {
                start = Long.parseLong(token);
            } else if (dash > 0) {
                start = Long.parseLong(token.substring(0, dash).trim());
                if (dash < token.length() - 1) {
                    end = Long.parseLong(token.substring(dash + 1, token.length()).trim());
                }
            }
            ranges.add(new Range(start, end));
        }
        return new Ranges(ranges);
    }

    @Override
    public String toString(Ranges value) {
        throw new UnsupportedOperationException("Range header used only in requests.");
    }
}
