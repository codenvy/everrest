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
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.util.Collections;
import java.util.List;

/**
 * Represents set of ranges provided by client in 'Range' header.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class Ranges {
    /** Represents one range from provided by client in HTTP header 'Range'. */
    public static class Range {
        /** Start of range. */
        private long start;

        /** End of range. */
        private long end;

        Range(long start, long end) {
            this.start = start;
            this.end = end;
        }

        /**
         * Get start of range. This method may return -1 if start of range was not
         * provided in HTTP header, e.g. range was specified as '-100' (100 bytes
         * from the end of content). Range may be normalized by using method
         * {@link #validate(long)}.
         *
         * @return end of range
         */
        public long getStart() {
            return start;
        }

        /**
         * Get end of range. This method may return -1 if end of range was not
         * provided in HTTP header, e.g. range was specified as '100-' (from byte
         * 100 end to the end of content). Range may be normalized by using method
         * {@link #validate(long)}.
         *
         * @return end of range
         */
        public long getEnd() {
            return end;
        }

        /**
         * Normalize and validate range. The calling this method should have the
         * next effect:
         * <ul>
         * <li>If <code>start</code> of range is -1 (range without start position)
         * <code>start</code> position should be set to
         * <code>length - (-1 * start)</code>, <code>end</code> will be set to
         * <code>length - 1</code></li>
         * <li>If <code>end</code> of range is -1 (range without end position)
         * <code>end</code> will be set to <code>length - 1</code></li>
         * <li>If <code>end</code> of range is greater then <code>length</code>
         * will be set to <code>length - 1</code></li>
         * </ul>
         *
         * @param length
         *         total length of content
         * @return <code>true</code> if range is valid and <code>false</code>
         * otherwise
         */
        public boolean validate(long length) {
            // Range set as bytes:-100 - take 100 bytes from the end.
            if (start < 0 && end == -1) {
                if ((-1 * start) >= length) {
                    start = 0;
                } else {
                    start = length + start;
                }
                end = length - 1;
            } else if (start >= 0 && end == -1) {
                // Range set as bytes:100- - take from 100 to the end.
                end = length - 1;
            } else if (end >= length) {
                // Fragment of bytes:100-200, end can be greater then content-length
                end = length - 1;
            }
            return (start >= 0) && (end >= 0) && (end >= start);
        }
    }

    private static final HeaderDelegate<Ranges> DELEGATE =
            RuntimeDelegate.getInstance().createHeaderDelegate(Ranges.class);

    public static Ranges valueOf(String range) {
        return DELEGATE.fromString(range);
    }

    private final List<Range> ranges;

    Ranges(List<Range> ranges) {
        this.ranges = Collections.unmodifiableList(ranges);
    }

    /**
     * Get unmodifiable set of ranges.
     *
     * @return set of ranges
     */
    public List<Range> getRanges() {
        return ranges;
    }

}
