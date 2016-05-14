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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class AcceptMediaTypeTest {
    private HeaderDelegate<AcceptMediaType> headerDelegate;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        headerDelegate = mock(HeaderDelegate.class);

        RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
        when(runtimeDelegate.createHeaderDelegate(AcceptMediaType.class)).thenReturn(headerDelegate);

        RuntimeDelegate.setInstance(runtimeDelegate);
    }

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void testValueOf() {
        AcceptMediaType acceptMediaType = acceptMediaType("text", "xml");
        when(headerDelegate.fromString("text/xml")).thenReturn(acceptMediaType);

        assertSame(acceptMediaType, AcceptMediaType.valueOf("text/xml"));
    }

    @Test
    public void testToString() {
        AcceptMediaType acceptMediaType = acceptMediaType("text", "xml");
        when(headerDelegate.toString(acceptMediaType)).thenReturn("text/xml");

        assertEquals("text/xml", acceptMediaType.toString());
        verify(headerDelegate).toString(acceptMediaType);
    }

    @DataProvider
    public static Object[][] forTestIsCompatible() {
        return new Object[][]{
                {acceptMediaType("text", "xml"), acceptMediaType("text", "xml"),   true},
                {acceptMediaType("text", "*"),   acceptMediaType("text", "xml"),   true},
                {acceptMediaType("text", "xml"), acceptMediaType("text", "plain"), false},
                {acceptMediaType("xxxx", "xml"), acceptMediaType("text", "xml"),   false},
                {acceptMediaType("*", "*"),      acceptMediaType("text", "xml"),   true},
                };
    }

    @UseDataProvider("forTestIsCompatible")
    @Test
    public void testIsCompatible(AcceptMediaType acceptMediaType, AcceptMediaType checkAcceptMediaType, boolean expectedResult) {
        assertEquals(expectedResult, acceptMediaType.isCompatible(checkAcceptMediaType));
    }

    private static AcceptMediaType acceptMediaType(String type, String subType) {
        return new AcceptMediaType(type,  subType);
    }
}
