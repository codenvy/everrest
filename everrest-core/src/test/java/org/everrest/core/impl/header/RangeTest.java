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

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.header.Ranges.Range;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RangeTest extends BaseTest {

    /* Total length of content. */
    private final long length = 500;

    public void testOneRange() {
        Ranges ranges = Ranges.valueOf("bytes=49-100");
        assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);
        assertTrue(rangeItem.validate(length));
        assertEquals(49, rangeItem.getStart());
        assertEquals(100, rangeItem.getEnd());
    }

    public void testOneRangeOverTotalLength() {
        Ranges ranges = Ranges.valueOf("bytes=49-1000");
        assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);

        assertEquals(49, rangeItem.getStart());
        assertEquals(1000, rangeItem.getEnd());

        // validate range since end of range if over total length
        assertTrue(rangeItem.validate(length));

        assertEquals(49, rangeItem.getStart());
        // must be changed to the end of content
        assertEquals(499, rangeItem.getEnd());
    }

    public void testOneRangeNoStart() {
        Ranges ranges = Ranges.valueOf("bytes=-100");
        assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);

        assertEquals(-100, rangeItem.getStart());
        assertEquals(-1, rangeItem.getEnd());

        // validate range since start of range is not set
        assertTrue(rangeItem.validate(length));

        // 100 bytes from the end
        assertEquals(400, rangeItem.getStart());
        // must be changed to the end of content
        assertEquals(499, rangeItem.getEnd());
    }

    public void testOneRangeNoEnd() {
        Ranges ranges = Ranges.valueOf("bytes=100-");
        assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);

        assertEquals(100, rangeItem.getStart());
        assertEquals(-1, rangeItem.getEnd());

        // validate range since end of range is not set
        assertTrue(rangeItem.validate(length));

        assertEquals(100, rangeItem.getStart());
        // must be changed to the end of content
        assertEquals(499, rangeItem.getEnd());
    }

    public void testOneByte() {
        Ranges ranges = Ranges.valueOf("bytes=100-100");
        assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);
        assertTrue(rangeItem.validate(length));
        assertEquals(100, rangeItem.getStart());
        assertEquals(100, rangeItem.getEnd());
    }

    public void testMultiRange() {
        Ranges ranges = Ranges.valueOf("bytes=49-100,150-250");
        assertEquals(2, ranges.getRanges().size());

        // first range
        Range rangeItem = ranges.getRanges().get(0);
        assertTrue(rangeItem.validate(length));
        assertEquals(49, rangeItem.getStart());
        assertEquals(100, rangeItem.getEnd());

        // second range
        rangeItem = ranges.getRanges().get(1);
        assertTrue(rangeItem.validate(length));
        assertEquals(150, rangeItem.getStart());
        assertEquals(250, rangeItem.getEnd());
    }

    public void testInvalidRange() {
        Ranges ranges = Ranges.valueOf("bytes=101-100");
        assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);
        // start of range is over end of range
        assertFalse(rangeItem.validate(length));
        assertEquals(101, rangeItem.getStart());
        assertEquals(100, rangeItem.getEnd());
    }
}
