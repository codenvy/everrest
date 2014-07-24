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
import org.everrest.core.header.QualityValue;

import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AcceptLanguageHeaderDelegate.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class AcceptLanguageHeaderDelegate extends AbstractHeaderDelegate<AcceptLanguage> {
    /** {@inheritDoc} */
    @Override
    public Class<AcceptLanguage> support() {
        return AcceptLanguage.class;
    }

    /** {@inheritDoc} */
    public AcceptLanguage fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        try {
            header = HeaderHelper.removeWhitespaces(header);
            String tag;
            Map<String, String> m = null;

            int p = header.indexOf(';');
            if (p != -1 && p < header.length() - 1) { // header has quality value
                tag = header.substring(0, p);
                m = new HeaderParameterParser().parse(header);
            } else { // no quality value
                tag = header;
            }

            p = tag.indexOf('-');
            String primaryTag;
            String subTag = null;

            if (p != -1 && p < tag.length() - 1) { // has sub-tag
                primaryTag = tag.substring(0, p);
                subTag = tag.substring(p + 1);
            } else { // no sub-tag
                primaryTag = tag;
            }

            if (m == null) // no quality value
            {
                return new AcceptLanguage(new Locale(primaryTag, subTag != null ? subTag : ""));
            } else {
                return new AcceptLanguage(new Locale(primaryTag, subTag != null ? subTag : ""), HeaderHelper
                        .parseQualityValue(m.get(QualityValue.QVALUE)));
            }

        } catch (ParseException e) {
            throw new IllegalArgumentException("Accept language header malformed");
        }
    }

    /** {@inheritDoc} */
    public String toString(AcceptLanguage language) {
        throw new UnsupportedOperationException("Accepted language header used only for request.");
    }
}
