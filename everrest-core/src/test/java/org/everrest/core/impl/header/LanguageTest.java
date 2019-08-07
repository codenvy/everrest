/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.header;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class LanguageTest {

    @DataProvider
    public static Object[][] forTestIsCompatible() {
        return new Object[][]{
                {language("en", ""),   null,                 false},
                {language("*", ""),    language("en", ""),   true},
                {language("en", ""),   language("en", ""),   true},
                {language("en", "gb"), language("en", ""),   false},
                {language("en", ""),   language("en", "gb"), true},
                {language("en", "us"), language("en", "gb"), false},
                {language("en", ""),   language("fr", ""),   false},
                };
    }

    @UseDataProvider("forTestIsCompatible")
    @Test
    public void testIsCompatible(Language language, Language checkLanguage, boolean expectedResult) {
        assertEquals(expectedResult, language.isCompatible(checkLanguage));
    }

    @DataProvider
    public static Object[][] forTestLanguageToString() {
        return new Object[][]{
                {language("en", "gb"), "en-gb"},
                {language("en", ""),   "en"},
                {language("en", ""),   "en"},
                {language("en", "GB"), "en-gb"}
        };
    }

    @UseDataProvider("forTestLanguageToString")
    @Test
    public void testLanguageToString(Language language, String expectedResult) {
        assertEquals(expectedResult, language.toString());
    }

    private static Language language(String primaryTag, String subTag) {
        return new Language(new Locale(primaryTag, subTag));
    }
}