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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class AcceptLanguageTest {
    private RuntimeDelegate.HeaderDelegate<AcceptLanguage> headerDelegate;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        headerDelegate = mock(RuntimeDelegate.HeaderDelegate.class);

        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(AcceptLanguage.class)).thenReturn(headerDelegate);

        RuntimeDelegate.setInstance(runtimeDelegate);
    }

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void testValueOf() {
        AcceptLanguage acceptLanguage = acceptLanguage("en", "us");
        when(headerDelegate.fromString("en-us")).thenReturn(acceptLanguage);

        assertSame(acceptLanguage, AcceptLanguage.valueOf("en-us"));
    }

    @Test
    public void testToString() {
        AcceptLanguage acceptLanguage = acceptLanguage("en", "us");
        when(headerDelegate.toString(acceptLanguage)).thenReturn("en-us");

        assertEquals("en-us", acceptLanguage.toString());
        verify(headerDelegate).toString(acceptLanguage);
    }

    @DataProvider
    public static Object[][] forTestIsCompatible() {
        return new Object[][]{
                {acceptLanguage("en", ""),   null,                       false},
                {acceptLanguage("*", ""),    acceptLanguage("en", ""),   true},
                {acceptLanguage("en", ""),   acceptLanguage("en", ""),   true},
                {acceptLanguage("en", "gb"), acceptLanguage("en", ""),   false},
                {acceptLanguage("en", ""),   acceptLanguage("en", "gb"), true},
                {acceptLanguage("en", "us"), acceptLanguage("en", "gb"), false},
                {acceptLanguage("en", ""),   acceptLanguage("fr", ""),   false},
                };
    }

    @UseDataProvider("forTestIsCompatible")
    @Test
    public void testIsCompatible(AcceptLanguage acceptLanguage, AcceptLanguage checkAcceptLanguage, boolean expectedResult) {
        assertEquals(expectedResult, acceptLanguage.isCompatible(checkAcceptLanguage));
    }

    @Test
    public void retrievesLocaleFromLanguage() {
        Locale locale = new Locale("en", "us");
        AcceptLanguage acceptLanguage = new AcceptLanguage(new Language(locale));
        assertEquals(locale, acceptLanguage.getLocale());
    }

    private static AcceptLanguage acceptLanguage(String primaryTag, String subTag) {
        return new AcceptLanguage(new Locale(primaryTag, subTag));
    }
}
