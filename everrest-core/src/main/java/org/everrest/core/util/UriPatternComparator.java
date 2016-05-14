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
package org.everrest.core.util;

import org.everrest.core.uri.UriPattern;

import java.util.Comparator;

/**
 * Helps to sort the templates according to the string comparison of the template regular expressions.
 * <p>
 * JSR-311 specification: "Sort the set of matching resource classes using the number of characters in the regular
 * expression not resulting from template variables as the primary key and the number of matching groups as a
 * secondary key"
 * </p>
 */
public class UriPatternComparator implements Comparator<UriPattern> {

    @Override
    public int compare(UriPattern uriPatternOne, UriPattern uriPatternTwo) {
        if (uriPatternOne == null && uriPatternTwo == null) {
            return 0;
        }
        if (uriPatternOne == null) {
            return 1;
        }
        if (uriPatternTwo == null) {
            return -1;
        }

        if (uriPatternOne.getTemplate().isEmpty() && uriPatternTwo.getTemplate().isEmpty()) {
            return 0;
        }
        if (uriPatternOne.getTemplate().isEmpty()) {
            return 1;
        }
        if (uriPatternTwo.getTemplate().isEmpty()) {
            return -1;
        }

        if (uriPatternOne.getNumberOfLiteralCharacters() < uriPatternTwo.getNumberOfLiteralCharacters()) {
            return 1;
        }
        if (uriPatternOne.getNumberOfLiteralCharacters() > uriPatternTwo.getNumberOfLiteralCharacters()) {
            return -1;
        }

        // pattern with two variables less the pattern with four variables
        if (uriPatternOne.getParameterNames().size() < uriPatternTwo.getParameterNames().size()) {
            return 1;
        }
        if (uriPatternOne.getParameterNames().size() > uriPatternTwo.getParameterNames().size()) {
            return -1;
        }

        return uriPatternOne.getRegex().compareTo(uriPatternTwo.getRegex());
    }
}
