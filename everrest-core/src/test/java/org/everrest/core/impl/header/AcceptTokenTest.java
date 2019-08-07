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

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class AcceptTokenTest {

    @DataProvider
    public static Object[][] forTestValueOf() {
        return new Object[][]{
                {"utf8",         new AcceptToken("utf8", 1.0f)},
                {"gzip;q=0.825", new AcceptToken("gzip", 0.825f)},
                {"*",            new AcceptToken("*", 1.0f)}
        };
    }

    @UseDataProvider("forTestValueOf")
    @Test
    public void testValueOf(String header, AcceptToken expectedResult) {
        assertEquals(expectedResult, AcceptToken.valueOf(header));
    }

    @DataProvider
    public static Object[][] forTestIsCompatible() {
        return new Object[][]{
                {new AcceptToken("utf8"), new AcceptToken("utf8"), true},
                {new AcceptToken("utf8"), new AcceptToken("gzip"), false},
                {new AcceptToken("*"),    new AcceptToken("*"), true},
                {new AcceptToken("*"),    new AcceptToken("utf8"), true},
                {new AcceptToken("utf8"), new AcceptToken("*"), false},
        };
    }

    @UseDataProvider("forTestIsCompatible")
    @Test
    public void testIsCompatible(AcceptToken acceptToken, AcceptToken checkAcceptToken, boolean expectedResult) throws Exception {
        assertEquals(expectedResult, acceptToken.isCompatible(checkAcceptToken));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenAcceptTokenHeaderIsNull() {
        AcceptToken.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenAcceptTokenHeaderContainsInvalidCharacters() {
        AcceptToken.valueOf("utf[8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenAcceptTokenHeaderContainsNonUS_ASCIICharacters() {
        AcceptToken.valueOf("\u041f\u0440\u0438\u0432\u0456\u0442");
    }
}