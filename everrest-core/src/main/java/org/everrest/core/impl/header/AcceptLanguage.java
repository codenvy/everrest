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

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.util.Locale;

/**
 * @author andrew00x
 */
public class AcceptLanguage extends Language implements QualityValue {
    /** Default accepted language, it minds any language is acceptable. */
    public static final AcceptLanguage DEFAULT = new AcceptLanguage(new Locale("*"));

    /** Quality value for 'accepted' HTTP headers, e. g. en-gb;0.9 */
    private final float qValue;

    /** See {@link RuntimeDelegate#createHeaderDelegate(Class)}. */
    private static final HeaderDelegate<AcceptLanguage> DELEGATE =
            RuntimeDelegate.getInstance().createHeaderDelegate(AcceptLanguage.class);

    /**
     * Creates a new instance of AcceptedLanguage by parsing the supplied string.
     *
     * @param header
     *         accepted language string
     * @return AcceptedLanguage
     */
    public static AcceptLanguage valueOf(String header) {
        return DELEGATE.fromString(header);
    }

    /**
     * Constructs new instance of accepted language with default quality value.
     *
     * @param locale
     *         the language
     */
    public AcceptLanguage(Locale locale) {
        super(locale);
        qValue = DEFAULT_QUALITY_VALUE;
    }

    /**
     * Constructs new instance of accepted language with quality value.
     *
     * @param locale
     *         the language
     * @param qValue
     *         quality value
     */
    public AcceptLanguage(Locale locale, float qValue) {
        super(locale);
        this.qValue = qValue;
    }

    // QualityValue

    @Override
    public float getQvalue() {
        return qValue;
    }
}
