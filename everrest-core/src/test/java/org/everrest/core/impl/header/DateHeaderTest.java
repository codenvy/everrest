/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.header;

import org.everrest.core.impl.BaseTest;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class DateHeaderTest extends BaseTest {
    public void testParseANSI() throws Exception {
        // EEE MMM d HH:mm:ss yyyy
        String date = "THU DEC 29 12:03:50 2011";
        HeaderHelper.parseDateHeader(date);
    }

    public void testParseRFC_1036() throws Exception {
        // EEEE, dd-MMM-yy HH:mm:ss zzz
        String date = "Thursday, 29-Dec-11 12:03:50 EST";
        HeaderHelper.parseDateHeader(date);
    }

    public void testParseRFC_1123() throws Exception {
        // EEE, dd MMM yyyy HH:mm:ss zzz
        String date = "Thu, 29 Dec 2011 12:03:50 GMT";
        HeaderHelper.parseDateHeader(date);
    }
}
