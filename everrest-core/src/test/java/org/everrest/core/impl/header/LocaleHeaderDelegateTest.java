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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class LocaleHeaderDelegateTest {

    private LocaleHeaderDelegate localeHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        localeHeaderDelegate = new LocaleHeaderDelegate();
    }

    @DataProvider
    public static Object[][] forFomString() {
        return new Object[][]{
                {"en-GB",             new Locale("en", "GB")},
                {"en-US,      en-GB", new Locale("en", "US")},
                {"en-,      en-GB",   new Locale("en", "")},
                {"en,       en-GB",   new Locale("en", "")},
                {"en ,       en-GB",  new Locale("en", "")},
                {"en- ,       en-GB", new Locale("en", "")}
                };
    }

    @UseDataProvider("forFomString")
    @Test
    public void fromString(String header, Locale expectedResult) {
        Locale locale = localeHeaderDelegate.fromString(header);
        assertEquals(expectedResult, locale);
    }

    @DataProvider
    public static Object[][] forTestToString() {
        return new Object[][]{
                {new Locale(""),         null},
                {new Locale("*"),        null},
                {new Locale("en", "GB"), "en-gb"},
                {new Locale("en", ""),   "en"},
                {new Locale("en"),       "en"}
        };
    }

    @UseDataProvider("forTestToString")
    @Test
    public void testToString(Locale locale, String expectedResult) {
        String header = localeHeaderDelegate.toString(locale);
        assertEquals(expectedResult, header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenLanguageHeaderIsNull() throws Exception {
        localeHeaderDelegate.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenLocaleIsNull() throws Exception {
        localeHeaderDelegate.toString(null);
    }
}
