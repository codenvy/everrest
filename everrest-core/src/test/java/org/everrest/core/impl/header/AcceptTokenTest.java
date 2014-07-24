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

import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class AcceptTokenTest extends BaseTest {

    public void testListAcceptCharset() {
        String cs = null;
        List<AcceptToken> l = HeaderHelper.createAcceptedCharsetList(cs);
        assertEquals(1, l.size());
        assertEquals(l.get(0).getToken(), "*");
        assertEquals(l.get(0).getQvalue(), 1.0F);

        cs = "";
        l = HeaderHelper.createAcceptedCharsetList(cs);
        assertEquals(1, l.size());
        assertEquals(l.get(0).getToken(), "*");
        assertEquals(l.get(0).getQvalue(), 1.0F);

        cs = "Windows-1251,utf-8; q   =0.9,*;q=0.7";
        l = HeaderHelper.createAcceptedCharsetList(cs);
        assertEquals(3, l.size());
        assertEquals(l.get(0).getToken(), "windows-1251");
        assertEquals(l.get(0).getQvalue(), 1.0F);
        assertEquals(l.get(1).getToken(), "utf-8");
        assertEquals(l.get(1).getQvalue(), 0.9F);
        assertEquals(l.get(2).getToken(), "*");
        assertEquals(l.get(2).getQvalue(), 0.7F);
    }

    public void testListAcceptEncoding() {
        String en = null;
        List<AcceptToken> l = HeaderHelper.createAcceptedEncodingList(en);
        assertEquals(1, l.size());
        assertEquals(l.get(0).getToken(), "*");
        assertEquals(l.get(0).getQvalue(), 1.0F);

        en = "";
        l = HeaderHelper.createAcceptedEncodingList(en);
        assertEquals(1, l.size());
        assertEquals(l.get(0).getToken(), "*");
        assertEquals(l.get(0).getQvalue(), 1.0F);

        en = "compress;q=0.5, gzip;q=1.0";
        l = HeaderHelper.createAcceptedCharsetList(en);
        assertEquals(2, l.size());
        assertEquals(l.get(0).getToken(), "gzip");
        assertEquals(l.get(0).getQvalue(), 1.0F);
        assertEquals(l.get(1).getToken(), "compress");
        assertEquals(l.get(1).getQvalue(), 0.5F);
    }

}
