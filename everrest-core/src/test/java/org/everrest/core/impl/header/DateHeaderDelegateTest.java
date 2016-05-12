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

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateHeaderDelegateTest {
    private DateHeaderDelegate dateHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        dateHeaderDelegate = new DateHeaderDelegate();
    }

    @Test
    public void parsesANSI() {
        String dateHeader = "THU DEC 29 12:03:50 2011";
        Date expectedDate = date(2011, 12, 29, 12, 3, 50, "GMT");

        Date date =  dateHeaderDelegate.fromString(dateHeader);
        assertTrue(String.format("Dates are not equal. Expected %s, actual %s", expectedDate, date),
                   Math.abs(expectedDate.getTime() - date.getTime()) < 1000);
    }

    @Test
    public void parsesRFC_1036() {
        String dateHeader = "Thursday, 29-Dec-11 12:03:50 EST";
        Date expectedDate = date(2011, 12, 29, 12, 3, 50, "EST");

        Date date = dateHeaderDelegate.fromString(dateHeader);

        assertTrue(String.format("Dates are not equal, %s and %s", expectedDate, date),
                   Math.abs(expectedDate.getTime() - date.getTime()) < 1000);
    }

    @Test
    public void parsesRFC_1123() {
        String dateHeader = "Thu, 29 Dec 2011 12:03:50 GMT";
        Date expectedDate = date(2011, 12, 29, 12, 3, 50, "GMT");

        Date date = dateHeaderDelegate.fromString(dateHeader);

        assertTrue(String.format("Dates are not equal, %s and %s", expectedDate, date),
                   Math.abs(expectedDate.getTime() - date.getTime()) < 1000);
    }

    private Date date(int year, int month, int day, int hours, int minutes, int seconds, String timeZone) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);

        return calendar.getTime();
    }

    @Test
    public void testToString() throws Exception {
        Date date = date(2010, 1, 8, 2, 5, 0, "EET");
        assertEquals("Fri, 08 Jan 2010 00:05:00 GMT", dateHeaderDelegate.toString(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsParseDateIfItDoesNotMatchToAnySupportedFormat() {
        String dateHeader = "12:03:50 GMT";
        dateHeaderDelegate.fromString(dateHeader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenHeaderIsNull() throws Exception {
        dateHeaderDelegate.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenDateIsNull() throws Exception {
        dateHeaderDelegate.toString(null);
    }
}