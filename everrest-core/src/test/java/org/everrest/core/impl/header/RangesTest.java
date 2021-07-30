/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.header;

import org.everrest.core.impl.header.Ranges.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author andrew00x
 */
public class RangesTest {

    private final long totalLengthOfContent = 500;

    private HeaderDelegate<Ranges> headerDelegate;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        headerDelegate = mock(HeaderDelegate.class);

        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(Ranges.class)).thenReturn(headerDelegate);

        RuntimeDelegate.setInstance(runtimeDelegate);
    }

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void validatesSimpleRange() {
        Range rangeItem = new Range(49, 100);
        assertTrue(rangeItem.validate(totalLengthOfContent));
    }

    @Test
    public void validatesRangeThatExceedsTotalLength() {
        Range rangeItem = new Range(49, 1000);
        assertTrue(rangeItem.validate(totalLengthOfContent));

        assertEquals(49, rangeItem.getStart());
        assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void validatesRangeWithoutEnd() {
        Range rangeItem = new Range(100, -1);

        assertTrue(rangeItem.validate(totalLengthOfContent));

        assertEquals(100, rangeItem.getStart());
        assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void validatesRangeWithNegativeStartAndWithoutEnd() {
        Range rangeItem = new Range(-100, -1);

        assertTrue(rangeItem.validate(totalLengthOfContent));

        assertEquals(400, rangeItem.getStart());
        assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void validatesRangeForOneByte() {
        Range rangeItem = new Range(100, 100);
        assertTrue(rangeItem.validate(totalLengthOfContent));

        assertEquals(100, rangeItem.getStart());
        assertEquals(100, rangeItem.getEnd());
    }

    @Test
    public void validatesRangeWhenStartINegativeAndExceedsContentLengthByAbsoluteValue() {
        Range rangeItem = new Range(-1000, -1);
        assertTrue(rangeItem.validate(totalLengthOfContent));

        assertEquals(0, rangeItem.getStart());
        assertEquals(499, rangeItem.getEnd());
    }

    @Test
    public void validatesRangeWhenStartExceedsEnd() {
        Range rangeItem = new Range(101, 100);
        assertFalse(rangeItem.validate(totalLengthOfContent));
        assertEquals(101, rangeItem.getStart());
        assertEquals(100, rangeItem.getEnd());
    }

    @Test
    public void testValueOf() {
        Ranges ranges = new Ranges(newArrayList(new Range(49, 100), new Range(150, 250)));
        when(headerDelegate.fromString("bytes=49-100,150-250")).thenReturn(ranges);

        assertSame(ranges, Ranges.valueOf("bytes=49-100,150-250"));
    }
}
