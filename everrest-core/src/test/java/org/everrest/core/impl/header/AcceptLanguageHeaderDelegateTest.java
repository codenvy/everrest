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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class AcceptLanguageHeaderDelegateTest {

    @DataProvider
    public static Object[][] forTestParsingAcceptLanguageHeader() {
        return new Object[][]{
                {"en-gb;q=0.8", "en", "gb", 0.8F},
                {"en;q=0.8",    "en", "",   0.8F},
                {"en",          "en", "",   1.0F},
                {"en-GB",       "en", "gb", 1.0F},
                {"en-",         "en", "",   1.0F},
                {"en-;q=0.8",   "en", "",   0.8F}
        };
    }

    @UseDataProvider("forTestParsingAcceptLanguageHeader")
    @Test
    public void testParsingAcceptLanguageHeader(String acceptLanguageHeader, String language, String country, float qValue) {
        AcceptLanguageHeaderDelegate headerDelegate = new AcceptLanguageHeaderDelegate();
        AcceptLanguage acceptLanguage = headerDelegate.fromString(acceptLanguageHeader);

        assertEquals(language, acceptLanguage.getPrimaryTag());
        assertEquals(country, acceptLanguage.getSubTag());
        assertEquals(qValue, acceptLanguage.getQvalue(), 0.0F);
    }

    @DataProvider
    public static Object[][] forTestAcceptLanguageToString() {
        return new Object[][]{
                {acceptLanguage("en", "gb", 0.8F), "en-gb"},
                {acceptLanguage("en", "", 0.8F),   "en"},
                {acceptLanguage("en", "", 1.0F),   "en"},
                {acceptLanguage("en", "GB", 1.0F), "en-gb"}
        };
    }

    @UseDataProvider("forTestAcceptLanguageToString")
    @Test
    public void testAcceptLanguageToString(AcceptLanguage acceptLanguage, String expectedResult) {
        AcceptLanguageHeaderDelegate headerDelegate = new AcceptLanguageHeaderDelegate();
        assertEquals(expectedResult, headerDelegate.toString(acceptLanguage));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenAcceptLanguageHeaderIsNull() {
        new AcceptLanguageHeaderDelegate().fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenAcceptLanguageIsNull() {
        new AcceptLanguageHeaderDelegate().toString(null);
    }

    private static AcceptLanguage acceptLanguage(String primaryTag, String subTag, float qValue) {
        return new AcceptLanguage(new Locale(primaryTag, subTag), qValue);
    }
}