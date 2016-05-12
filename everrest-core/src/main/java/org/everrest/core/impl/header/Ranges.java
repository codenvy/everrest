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

import com.google.common.base.MoreObjects;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Collections;
import java.util.List;

/**
 * Represents set of ranges provided by client in 'Range' header.
 *
 * @author andrew00x
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
         * Normalize and validate range. The calling this method has next effect:
         * <ul>
         * <li>If {@code start} of range is -1 (range without start position)
         * {@code start} position should be set to
         * <code>length - (-1 * start)</code>, {@code end} will be set to
         * <code>length - 1</code></li>
         * <li>If {@code end} of range is -1 (range without end position)
         * {@code end} will be set to <code>length - 1</code></li>
         * <li>If {@code end} of range is greater then {@code length}
         * will be set to <code>length - 1</code></li>
         * </ul>
         *
         * @param length
         *         total length of content
         * @return {@code true} if range is valid and {@code false}
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Range)) {
                return false;
            }

            Range range = (Range)o;
            return start == range.start && end == range.end;

        }

        @Override
        public int hashCode() {
            int hashcode = 8;
            hashcode = 31 * hashcode + (int)(start ^ (start >>> 32));
            hashcode = 31 * hashcode + (int)(end ^ (end >>> 32));
            return hashcode;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                              .add("start", start)
                              .add("end", end)
                              .toString();
        }
    }

    public static Ranges valueOf(String range) {
        return RuntimeDelegate.getInstance().createHeaderDelegate(Ranges.class).fromString(range);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ranges)) {
            return false;
        }
        return ranges.equals(((Ranges)o).ranges);

    }

    @Override
    public int hashCode() {
        int hashcode = 8;
        hashcode = 31 * hashcode + ranges.hashCode();
        return hashcode;
    }
}
