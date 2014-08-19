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

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Locale;

/**
 * @author andrew00x
 */
public class LocaleHeaderDelegate implements RuntimeDelegate.HeaderDelegate<Locale> {

    @Override
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


    @Override
    public String toString(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException();
        }
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
