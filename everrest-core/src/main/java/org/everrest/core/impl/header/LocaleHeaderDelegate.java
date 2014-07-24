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

import java.util.Locale;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class LocaleHeaderDelegate extends AbstractHeaderDelegate<Locale> {
    /** {@inheritDoc} */
    @Override
    public Class<Locale> support() {
        return Locale.class;
    }

    /** {@inheritDoc} */
    public Locale fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        header = HeaderHelper.removeWhitespaces(header);
        int p;
        // Can be set multiple content language, the take first one
        if ((p = header.indexOf(',')) > 0) {
            header = header.substring(0, p);
        }

        p = header.indexOf('-');
        if (p != -1 && p < header.length() - 1) {
            return new Locale(header.substring(0, p), header.substring(p + 1));
        } else {
            return new Locale(header);
        }
    }

    /** {@inheritDoc} */
    public String toString(Locale locale) {
        String lan = locale.getLanguage();
        // For output if language does not set correctly then ignore it.
        if (lan.isEmpty() || "*".equals(lan)) {
            return null;
        }

        String con = locale.getCountry();
        if (con.isEmpty()) {
            return lan.toLowerCase();
        }

        return lan.toLowerCase() + "-" + con.toLowerCase();
    }
}
