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

import org.everrest.core.impl.header.MediaTypeHelper;

import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Keeps sorted values.
 *
 * @param <T>
 *         actual value type
 * @author andrew00x
 */
public class MediaTypeMap<T> extends ConcurrentSkipListMap<MediaType, T> {
    /** Serial Version UID. */
    private static final long serialVersionUID = -4713556573521776577L;

    /** Create new instance of MediaTypeMap with {@link Comparator}. */
    public MediaTypeMap() {
        super(COMPARATOR);
    }

    /** See {@link Comparator}. */
    static final Comparator<MediaType> COMPARATOR = new Comparator<MediaType>() {
        /**
         * Compare two {@link MediaType}.
         *
         * @param o1 first MediaType to be compared
         * @param o2 second MediaType to be compared
         * @return result of comparison
         * @see Comparator#compare(Object, Object)
         * @see MediaTypeHelper
         * @see MediaType
         */
        @Override
        public int compare(MediaType o1, MediaType o2) {
            int r = MediaTypeHelper.MEDIA_TYPE_COMPARATOR.compare(o1, o2);
            // If media type has the same 'weight' (i.e. 'application/xml' and
            // 'text/xml' has the same 'weight'), then order does not matter but
            // should e compared lexicographically, otherwise new entry with the
            // same 'weight' will be not added in map.
            if (r == 0) {
                r = toString(o1).compareToIgnoreCase(toString(o2));
            }
            return r;
        }

        private String toString(MediaType mime) {
            return mime.getType() + "/" + mime.getSubtype();
        }
    };
}
