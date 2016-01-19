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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author andrew00x
 */
public class AcceptTokenTest {

    @Test
    public void testListAcceptCharsetNull() {
        String cs = null;
        List<AcceptToken> l = HeaderHelper.createAcceptedCharsetList(cs);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(l.get(0).getToken(), "*");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
    }

    @Test
    public void testListAcceptCharsetEmptyString() {
        String cs = "";
        List<AcceptToken> l = HeaderHelper.createAcceptedCharsetList(cs);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(l.get(0).getToken(), "*");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
    }

    @Test
    public void testListAcceptCharset() {
        String cs = "Windows-1251,utf-8; q   =0.9,*;q=0.7";
        List<AcceptToken> l = HeaderHelper.createAcceptedCharsetList(cs);
        Assert.assertEquals(3, l.size());
        Assert.assertEquals(l.get(0).getToken(), "windows-1251");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
        Assert.assertEquals(l.get(1).getToken(), "utf-8");
        Assert.assertEquals(l.get(1).getQvalue(), 0.9F, 0.0F);
        Assert.assertEquals(l.get(2).getToken(), "*");
        Assert.assertEquals(l.get(2).getQvalue(), 0.7F, 0.0F);
    }

    @Test
    public void testListAcceptEncodingNull() {
        String en = null;
        List<AcceptToken> l = HeaderHelper.createAcceptedEncodingList(en);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(l.get(0).getToken(), "*");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
    }

    @Test
    public void testListAcceptEncodingEmptyString() {
        String en = "";
        List<AcceptToken> l = HeaderHelper.createAcceptedEncodingList(en);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(l.get(0).getToken(), "*");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
    }

    @Test
    public void testListAcceptEncoding() {
        String en = "compress;q=0.5, gzip;q=1.0";
        List<AcceptToken> l = HeaderHelper.createAcceptedCharsetList(en);
        Assert.assertEquals(2, l.size());
        Assert.assertEquals(l.get(0).getToken(), "gzip");
        Assert.assertEquals(l.get(0).getQvalue(), 1.0F, 0.0F);
        Assert.assertEquals(l.get(1).getToken(), "compress");
        Assert.assertEquals(l.get(1).getQvalue(), 0.5F, 0.0F);
    }
}
