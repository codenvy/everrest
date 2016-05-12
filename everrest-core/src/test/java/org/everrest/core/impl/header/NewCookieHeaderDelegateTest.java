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

import javax.ws.rs.core.NewCookie;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class NewCookieHeaderDelegateTest {
    private NewCookieHeaderDelegate newCookieHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        newCookieHeaderDelegate = new NewCookieHeaderDelegate();
    }

    @Test
    public void testToString() throws Exception {
        NewCookie cookie = new NewCookie("foo",
                                         "bar",
                                         "/aaa",
                                         "andrew.com",
                                         1,
                                         "comment",
                                         300,
                                         date(2010, 1, 8, 2, 5, 0, "EET"),
                                         true,
                                         true);
        assertEquals("foo=bar;Version=1;Comment=comment;Domain=andrew.com;Path=/aaa;Max-Age=300;Expires=Fri, 08 Jan 2010 00:05:00 GMT;Secure;HttpOnly",
                     newCookieHeaderDelegate.toString(cookie));
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
    public void testFromString() throws Exception {
        NewCookie expectedNewCookie = new NewCookie("company", "codenvy", "/path", "codenvy.com", 1, "comment", 300, null, true, true);
        NewCookie parsedNewCookie = newCookieHeaderDelegate.fromString("company=codenvy;version=1;paTh=/path;Domain=codenvy.com;comment=\"comment\";max-age=300;HttpOnly;secure");

        assertEquals(expectedNewCookie, parsedNewCookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenNewCookieIsNull() throws Exception {
        newCookieHeaderDelegate.toString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenNewCookieHeaderIsNull() throws Exception {
        newCookieHeaderDelegate.fromString(null);
    }
}