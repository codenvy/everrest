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
package org.everrest.core.impl.header;

import org.everrest.core.header.AbstractHeaderDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RangeHeaderDelegate extends AbstractHeaderDelegate<Ranges> {

    @Override
    public Class<Ranges> support() {
        return Ranges.class;
    }


    @Override
    public Ranges fromString(String value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("null");
        }
        if (!value.startsWith("bytes")) {
            throw new IllegalArgumentException("Invalid byte range.");
        }

        value = value.substring(value.indexOf("=") + 1);

        String[] tokens = value.split(",");
        List<Ranges.Range> r = new ArrayList<Ranges.Range>();
        for (String token : tokens) {
            long start = 0;
            long end = -1L;
            token = token.trim();
            int dash = token.indexOf("-");
            if (dash == -1) {
                throw new IllegalArgumentException("Invalid byte range.");
            } else if (dash == 0) {
                start = Long.parseLong(token);
            } else if (dash > 0) {
                start = Long.parseLong(token.substring(0, dash).trim());
                if (dash < token.length() - 1) {
                    end = Long.parseLong(token.substring(dash + 1, token.length()).trim());
                }
            }
            r.add(new Ranges.Range(start, end));
        }
        return new Ranges(r);
    }


    @Override
    public String toString(Ranges value) {
        throw new UnsupportedOperationException();
    }
}
