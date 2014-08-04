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

import org.everrest.core.impl.header.Ranges.Range;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author andrew00x
 */
public class RangeTest {

    /* Total length of content. */
    private final long length = 500;

    @Test
    public void testOneRange() {
        Ranges ranges = Ranges.valueOf("bytes=49-100");
        Assert.assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);
        Assert.assertTrue(rangeItem.validate(length));
        Assert.assertEquals(49, rangeItem.getStart());
        Assert.assertEquals(100, rangeItem.getEnd());
    }

    @Test
    public void testOneRangeOverTotalLength() {
        Ranges ranges = Ranges.valueOf("bytes=49-1000");
        Assert.assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);

        Assert.assertEquals(49, rangeItem.getStart());
        Assert.assertEquals(1000, rangeItem.getEnd());

        // validate range since end of range if over total length
        Assert.assertTrue(rangeItem.validate(length));

        Assert.assertEquals(49, rangeItem.getStart());
        // must be changed to the end of content
        Assert.assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void testOneRangeNoStart() {
        Ranges ranges = Ranges.valueOf("bytes=-100");
        Assert.assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);

        Assert.assertEquals(-100, rangeItem.getStart());
        Assert.assertEquals(-1, rangeItem.getEnd());

        // validate range since start of range is not set
        Assert.assertTrue(rangeItem.validate(length));

        // 100 bytes from the end
        Assert.assertEquals(400, rangeItem.getStart());
        // must be changed to the end of content
        Assert.assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void testOneRangeNoEnd() {
        Ranges ranges = Ranges.valueOf("bytes=100-");
        Assert.assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);

        Assert.assertEquals(100, rangeItem.getStart());
        Assert.assertEquals(-1, rangeItem.getEnd());

        // validate range since end of range is not set
        Assert.assertTrue(rangeItem.validate(length));

        Assert.assertEquals(100, rangeItem.getStart());
        // must be changed to the end of content
        Assert.assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void testOneByte() {
        Ranges ranges = Ranges.valueOf("bytes=100-100");
        Assert.assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);
        Assert.assertTrue(rangeItem.validate(length));
        Assert.assertEquals(100, rangeItem.getStart());
        Assert.assertEquals(100, rangeItem.getEnd());
    }

    @Test
    public void testMultiRange() {
        Ranges ranges = Ranges.valueOf("bytes=49-100,150-250");
        Assert.assertEquals(2, ranges.getRanges().size());

        // first range
        Range rangeItem = ranges.getRanges().get(0);
        Assert.assertTrue(rangeItem.validate(length));
        Assert.assertEquals(49, rangeItem.getStart());
        Assert.assertEquals(100, rangeItem.getEnd());

        // second range
        rangeItem = ranges.getRanges().get(1);
        Assert.assertTrue(rangeItem.validate(length));
        Assert.assertEquals(150, rangeItem.getStart());
        Assert.assertEquals(250, rangeItem.getEnd());
    }

    @Test
    public void testInvalidRange() {
        Ranges ranges = Ranges.valueOf("bytes=101-100");
        Assert.assertEquals(1, ranges.getRanges().size());
        Range rangeItem = ranges.getRanges().get(0);
        // start of range is over end of range
        Assert.assertFalse(rangeItem.validate(length));
        Assert.assertEquals(101, rangeItem.getStart());
        Assert.assertEquals(100, rangeItem.getEnd());
    }
}
