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
public class AcceptLanguageTest extends BaseTest {

    public void testValueOf() {
        String al = "en-gb;q=0.8";
        AcceptLanguage acceptedLanguage = AcceptLanguage.valueOf(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("gb", acceptedLanguage.getSubTag());
        assertEquals(0.8F, acceptedLanguage.getQvalue());

        al = "en;q=0.8";
        acceptedLanguage = AcceptLanguage.valueOf(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("", acceptedLanguage.getSubTag());
        assertEquals(0.8F, acceptedLanguage.getQvalue());

        al = "en";
        acceptedLanguage = AcceptLanguage.valueOf(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("", acceptedLanguage.getSubTag());
        assertEquals(1F, acceptedLanguage.getQvalue());

        al = "en-GB";
        acceptedLanguage = AcceptLanguage.valueOf(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("gb", acceptedLanguage.getSubTag());
        assertEquals(1F, acceptedLanguage.getQvalue());
    }

    public void testFromString() {
        AcceptLanguageHeaderDelegate hd = new AcceptLanguageHeaderDelegate();
        String al = "en-gb;q=0.8";
        AcceptLanguage acceptedLanguage = hd.fromString(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("gb", acceptedLanguage.getSubTag());
        assertEquals(0.8F, acceptedLanguage.getQvalue());

        al = "en;q=0.8";
        acceptedLanguage = hd.fromString(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("", acceptedLanguage.getSubTag());
        assertEquals(0.8F, acceptedLanguage.getQvalue());

        al = "en";
        acceptedLanguage = hd.fromString(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("", acceptedLanguage.getSubTag());
        assertEquals(1F, acceptedLanguage.getQvalue());

        al = "en-GB";
        acceptedLanguage = hd.fromString(al);
        assertEquals("en", acceptedLanguage.getPrimaryTag());
        assertEquals("gb", acceptedLanguage.getSubTag());
        assertEquals(1F, acceptedLanguage.getQvalue());

    }

    public void testListProducer() {
        List<AcceptLanguage> l = HeaderHelper.createAcceptedLanguageList(null);
        assertEquals(1, l.size());
        l = HeaderHelper.createAcceptedLanguageList("");
        assertEquals(1, l.size());

        String ln = "da;q=0.825,   en-GB,  en;q=0.8";
        l = HeaderHelper.createAcceptedLanguageList(ln);
        assertEquals(3, l.size());

        assertEquals("en", l.get(0).getPrimaryTag());
        assertEquals("gb", l.get(0).getSubTag());
        assertEquals(1.0F, l.get(0).getQvalue());

        assertEquals("da", l.get(1).getPrimaryTag());
        assertEquals("", l.get(1).getSubTag());
        assertEquals(0.825F, l.get(1).getQvalue());

        assertEquals("en", l.get(2).getPrimaryTag());
        assertEquals("", l.get(2).getSubTag());
        assertEquals(0.8F, l.get(2).getQvalue());
    }

}
