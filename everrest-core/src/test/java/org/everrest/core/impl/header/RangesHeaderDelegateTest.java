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
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class RangesHeaderDelegateTest {
    private RangesHeaderDelegate rangesHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        rangesHeaderDelegate = new RangesHeaderDelegate();
    }

    @Test
    public void parsesSingleRange() {
        Ranges ranges = rangesHeaderDelegate.fromString("bytes=49-100");
        assertEquals(newArrayList(new Range(49, 100)), ranges.getRanges());
    }

    @Test
    public void parsesSingleRangeWithoutStart() {
        Ranges ranges = rangesHeaderDelegate.fromString("bytes=-100");
        assertEquals(newArrayList(new Range(-100, -1)), ranges.getRanges());
    }

    @Test
    public void parsesSingleRangeWithoutEnd() {
        Ranges ranges = rangesHeaderDelegate.fromString("bytes=100-");
        assertEquals(newArrayList(new Range(100, -1)), ranges.getRanges());
    }

    @Test
    public void parsesMultiRangeWithoutStart() {
        Ranges ranges = rangesHeaderDelegate.fromString("bytes=49-100,150-250");
        assertEquals(newArrayList(new Range(49, 100), new Range(150, 250)), ranges.getRanges());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenByteRangesHeaderIsNull() throws Exception {
        rangesHeaderDelegate.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenByteRangesHeaderDoesNotStartWith_bytes() throws Exception {
        rangesHeaderDelegate.fromString("49-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenByteRangesHeaderDoesNotContainEqual() throws Exception {
        rangesHeaderDelegate.fromString("bytes49-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenByteRangesHeaderDoesNotContainDash() throws Exception {
        rangesHeaderDelegate.fromString("bytes=100");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doesNotSupportToStringForRanges() throws Exception {
        Ranges ranges = new Ranges(newArrayList(new Range(1, 100)));
        rangesHeaderDelegate.toString(ranges);
    }
}