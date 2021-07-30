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

import javax.ws.rs.ext.RuntimeDelegate;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static org.everrest.core.header.QualityValue.QVALUE;
import static org.everrest.core.impl.header.HeaderHelper.parseQualityValue;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

public class AcceptLanguageHeaderDelegate implements RuntimeDelegate.HeaderDelegate<AcceptLanguage> {

    private static final char QUALITY_SEPARATOR = ';';
    private static final char SUB_TAG_SEPARATOR = '-';

    @Override
    public AcceptLanguage fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        try {
            header = HeaderHelper.removeWhitespaces(header);
            String tag;
            Map<String, String> params = null;

            int p = scan(header, QUALITY_SEPARATOR);
            if (charAtIs(header, p, QUALITY_SEPARATOR)) {
                tag = header.substring(0, p);
                params = new HeaderParameterParser().parse(header);
            } else {
                tag = header;
            }

            String lang;
            String country = null;

            p = scan(header, SUB_TAG_SEPARATOR);
            if (charAtIs(header, p, SUB_TAG_SEPARATOR)) {
                lang = tag.substring(0, p);
                country = tag.substring(p + 1);
            } else {
                lang = tag;
            }

            if (params == null || params.get(QVALUE) == null) {
                return new AcceptLanguage(new Language(new Locale(lang, nullToEmpty(country))));
            }

            return new AcceptLanguage(new Locale(lang, nullToEmpty(country)), parseQualityValue(params.get(QVALUE)));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Accept language header malformed");
        }
    }


    @Override
    public String toString(AcceptLanguage language) {
        if (language == null) {
            throw new IllegalArgumentException();
        }
        return language.getLanguage().toString();
    }
}
