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

import org.junit.Test;

/**
 * @author andrew00x
 */
public class DateHeaderTest {
    @Test
    public void testParseANSI() {
        // EEE MMM d HH:mm:ss yyyy
        String date = "THU DEC 29 12:03:50 2011";
        HeaderHelper.parseDateHeader(date);
    }

    @Test
    public void testParseRFC_1036() {
        // EEEE, dd-MMM-yy HH:mm:ss zzz
        String date = "Thursday, 29-Dec-11 12:03:50 EST";
        HeaderHelper.parseDateHeader(date);
    }

    @Test
    public void testParseRFC_1123() {
        // EEE, dd MMM yyyy HH:mm:ss zzz
        String date = "Thu, 29 Dec 2011 12:03:50 GMT";
        HeaderHelper.parseDateHeader(date);
    }
}
