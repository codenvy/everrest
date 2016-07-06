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

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Locale;

import static org.everrest.core.impl.header.HeaderHelper.removeWhitespaces;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

public class LocaleHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Locale> {

    @Override
    public Locale fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        header = removeWhitespaces(header);
        int p = scan(header, ',');
        if (charAtIs(header, p, ',')) {
            header = header.substring(0, p);
        }

        p = scan(header, '-');
        if (charAtIs(header, p, '-')) {
            return new Locale(header.substring(0, p), header.substring(p + 1));
        }
        return new Locale(header);
    }


    @Override
    public String toString(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException();
        }
        String language = locale.getLanguage();
        if (language.isEmpty() || "*".equals(language)) {
            return null;
        }

        String country = locale.getCountry();
        if (country.isEmpty()) {
            return language.toLowerCase();
        }

        return language.toLowerCase() + "-" + country.toLowerCase();
    }
}
