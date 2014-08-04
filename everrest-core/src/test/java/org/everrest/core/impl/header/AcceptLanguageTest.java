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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author andrew00x
 */
public class AcceptLanguageTest {

    @Test
    public void testValueOfLanguageWithCountryWithQValue() {
        String al = "en-gb;q=0.8";
        AcceptLanguage acceptedLanguage = AcceptLanguage.valueOf(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("gb", acceptedLanguage.getSubTag());
        Assert.assertEquals(0.8F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testValueOfLanguageWithQValue() {
        String al = "en;q=0.8";
        AcceptLanguage acceptedLanguage = acceptedLanguage = AcceptLanguage.valueOf(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("", acceptedLanguage.getSubTag());
        Assert.assertEquals(0.8F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testValueOfLanguage() {
        String al = "en";
        AcceptLanguage acceptedLanguage = acceptedLanguage = AcceptLanguage.valueOf(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("", acceptedLanguage.getSubTag());
        Assert.assertEquals(1F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testValueOfLanguageWithCountry() {
        String al = "en-GB";
        AcceptLanguage acceptedLanguage = acceptedLanguage = AcceptLanguage.valueOf(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("gb", acceptedLanguage.getSubTag());
        Assert.assertEquals(1F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testFromStringLanguageWithCountryWithQValue() {
        AcceptLanguageHeaderDelegate hd = new AcceptLanguageHeaderDelegate();
        String al = "en-gb;q=0.8";
        AcceptLanguage acceptedLanguage = hd.fromString(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("gb", acceptedLanguage.getSubTag());
        Assert.assertEquals(0.8F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testFromStringLanguageWIthQValue() {
        AcceptLanguageHeaderDelegate hd = new AcceptLanguageHeaderDelegate();
        String al = "en;q=0.8";
        AcceptLanguage acceptedLanguage = hd.fromString(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("", acceptedLanguage.getSubTag());
        Assert.assertEquals(0.8F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testFromStringLanguage() {
        AcceptLanguageHeaderDelegate hd = new AcceptLanguageHeaderDelegate();
        String al = "en";
        AcceptLanguage acceptedLanguage = hd.fromString(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("", acceptedLanguage.getSubTag());
        Assert.assertEquals(1F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testFromStringLanguageWithCountry() {
        AcceptLanguageHeaderDelegate hd = new AcceptLanguageHeaderDelegate();
        String al = "en-GB";
        AcceptLanguage acceptedLanguage = hd.fromString(al);
        Assert.assertEquals("en", acceptedLanguage.getPrimaryTag());
        Assert.assertEquals("gb", acceptedLanguage.getSubTag());
        Assert.assertEquals(1F, acceptedLanguage.getQvalue(), 0.0F);
    }

    @Test
    public void testLanguageList() {
        List<AcceptLanguage> l = HeaderHelper.createAcceptedLanguageList(null);
        Assert.assertEquals(1, l.size());
        l = HeaderHelper.createAcceptedLanguageList("");
        Assert.assertEquals(1, l.size());

        String ln = "da;q=0.825,   en-GB,  en;q=0.8";
        l = HeaderHelper.createAcceptedLanguageList(ln);
        Assert.assertEquals(3, l.size());

        Assert.assertEquals("en", l.get(0).getPrimaryTag());
        Assert.assertEquals("gb", l.get(0).getSubTag());
        Assert.assertEquals(1.0F, l.get(0).getQvalue(), 0.0F);

        Assert.assertEquals("da", l.get(1).getPrimaryTag());
        Assert.assertEquals("", l.get(1).getSubTag());
        Assert.assertEquals(0.825F, l.get(1).getQvalue(), 0.0F);

        Assert.assertEquals("en", l.get(2).getPrimaryTag());
        Assert.assertEquals("", l.get(2).getSubTag());
        Assert.assertEquals(0.8F, l.get(2).getQvalue(), 0.0F);
    }

}
