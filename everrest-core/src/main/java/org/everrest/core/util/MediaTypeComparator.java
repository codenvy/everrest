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
package org.everrest.core.util;

import org.everrest.core.impl.header.MediaTypeHelper;

import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.regex.Matcher;

/**
 * Compare two media types. The main rule for sorting media types is:
 * <p/>
 * <li>n/m</li>
 * <li>n&#47;*</li>
 * <li>*&#47;*</li>
 * <p/>
 */
public class MediaTypeComparator implements Comparator<MediaType> {
    @Override
    public int compare(MediaType mediaTypeOne, MediaType mediaTypeTwo) {

        if (mediaTypeOne.isWildcardType() && !mediaTypeTwo.isWildcardType()) {
            return 1;
        }
        if (mediaTypeOne.isWildcardSubtype() && !mediaTypeTwo.isWildcardSubtype()) {
            return 1;
        }
        if (!mediaTypeOne.isWildcardType() && mediaTypeTwo.isWildcardType()) {
            return -1;
        }
        if (!mediaTypeOne.isWildcardSubtype() && mediaTypeTwo.isWildcardSubtype()) {
            return -1;
        }

        Matcher extSubtypeMatcherOne = MediaTypeHelper.EXT_SUBTYPE_PATTERN.matcher(mediaTypeOne.getSubtype());
        Matcher extSubtypeMatcherTwo = MediaTypeHelper.EXT_SUBTYPE_PATTERN.matcher(mediaTypeTwo.getSubtype());
        boolean extSubtypeMatcherOneMatches = extSubtypeMatcherOne.matches();
        boolean extSubtypeMatcherTwoMatches = extSubtypeMatcherTwo.matches();

        if (extSubtypeMatcherOneMatches && !extSubtypeMatcherTwoMatches) {
            return 1;
        }
        if (!extSubtypeMatcherOneMatches && extSubtypeMatcherTwoMatches) {
            return -1;
        }

        extSubtypeMatcherOne = MediaTypeHelper.EXT_PREFIX_SUBTYPE_PATTERN.matcher(mediaTypeOne.getSubtype());
        extSubtypeMatcherTwo = MediaTypeHelper.EXT_PREFIX_SUBTYPE_PATTERN.matcher(mediaTypeTwo.getSubtype());
        extSubtypeMatcherOneMatches = extSubtypeMatcherOne.matches();
        extSubtypeMatcherTwoMatches = extSubtypeMatcherTwo.matches();

        if (extSubtypeMatcherOneMatches && !extSubtypeMatcherTwoMatches) {
            return 1;
        }
        if (!extSubtypeMatcherOneMatches && extSubtypeMatcherTwoMatches) {
            return -1;
        }

        extSubtypeMatcherOne = MediaTypeHelper.EXT_SUFFIX_SUBTYPE_PATTERN.matcher(mediaTypeOne.getSubtype());
        extSubtypeMatcherTwo = MediaTypeHelper.EXT_SUFFIX_SUBTYPE_PATTERN.matcher(mediaTypeTwo.getSubtype());
        extSubtypeMatcherOneMatches = extSubtypeMatcherOne.matches();
        extSubtypeMatcherTwoMatches = extSubtypeMatcherTwo.matches();

        if (extSubtypeMatcherOneMatches && !extSubtypeMatcherTwoMatches) {
            return 1;
        }
        if (!extSubtypeMatcherOneMatches && extSubtypeMatcherTwoMatches) {
            return -1;
        }

        return toString(mediaTypeOne).compareToIgnoreCase(toString(mediaTypeTwo));
    }

    private String toString(MediaType mime) {
        return mime.getType() + "/" + mime.getSubtype();
    }
}
