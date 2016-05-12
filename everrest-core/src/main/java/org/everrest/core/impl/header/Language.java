/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import java.util.Locale;

/**
 * HTTP language tag.
 *
 * @author andrew00x
 */
public class Language {
    private final Locale locale;
    private final String primaryTag;
    private final String subTag;

    /**
     * Constructs new instance of Language.
     *
     * @param locale
     *         Locale
     * @see {@link Locale}
     */
    public Language(Locale locale) {
        this.locale = locale;

        primaryTag = locale.getLanguage().toLowerCase();
        subTag = locale.getCountry().toLowerCase();
    }

    /**
     * Gets primary-tag of language tag, e. g. if Language tag 'en-gb' then 'en' is primary-tag. See <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec3.10" >HTTP/1.1 documentation</a>.
     *
     * @return the primary-tag of Language tag
     */
    public String getPrimaryTag() {
        return primaryTag;
    }

    /**
     * Gets sub-tag of language tag, e. g. if Language tag 'en-gb' then 'gb' is sub-tag. See <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec3.10" >HTTP/1.1 documentation</a>.
     *
     * @return the sub-tag of Language tag
     */
    public String getSubTag() {
        return subTag;
    }

    /** @return @see {@link Locale} */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Checks that two Languages instance are compatible.
     *
     * @param other
     *         checked language
     * @return {@code true} if given Language is compatible with current {@code false} otherwise
     */
    public boolean isCompatible(Language other) {
        if (other == null) {
            return false;
        }
        if ("*".equals(primaryTag)) {
            return true;
        }
        // primary tags match and sub-tag not specified (any matches)
        // if 'accept-language' is 'en' then 'en-us' and 'en-gb' is matches
        return primaryTag.equals(other.primaryTag)
               && (subTag.isEmpty() || subTag.equals(other.subTag));
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(primaryTag);
        if (!subTag.isEmpty()) {
            sb.append('-').append(subTag);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Language)) {
            return false;
        }

        Language other = (Language)o;
        return locale.equals(other.locale);
    }

    @Override
    public int hashCode() {
        int hashcode = 8;
        return hashcode * 31 + locale.hashCode();
    }
}
