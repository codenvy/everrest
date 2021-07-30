/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.provider.json;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class JsonValueFactoryTest {
    private JsonValueFactory jsonValueFactory;
    
    @Before
    public void setUp() throws Exception {
        jsonValueFactory = new JsonValueFactory();
    }

    @Test
    public void createsStringValueWithoutQuotesFromString() {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("\"to be or not to be\"");

        assertTrue(jsonValue.isString());
        assertEquals("to be or not to be", jsonValue.getStringValue());
    }

    @Test
    public void createsStringValueFromString() {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("to be or not to be");

        assertTrue(jsonValue.isString());
        assertEquals("to be or not to be", jsonValue.getStringValue());
    }

    @Test
    public void createsBooleanValueFromString() {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("true");

        assertTrue(jsonValue.isBoolean());
        assertTrue(jsonValue.getBooleanValue());
    }

    @Test
    public void createsBooleanValueIgnoringCaseFromString() {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("True");

        assertTrue(jsonValue.isBoolean());
        assertTrue(jsonValue.getBooleanValue());
    }

    @Test
    public void createsNullValueFromString() {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("null");

        assertTrue(jsonValue.isNull());
    }

    @Test
    public void createsNullValueIgnoringCaseFromString() {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("Null");

        assertTrue(jsonValue.isNull());
    }

    @DataProvider
    public static Object[][] numericValueData() {
        return new Object[][] {
                {"123", false, 123.0},
                {"0123", false, 83.0},
                {"0x123", false, 291.0},
                {"0xaff", false, 2815.0},
                {"+123", false, 123.0},
                {"-123", false, -123.0},
                {"1.23", true, 1.23},
                {"01.23", true, 1.23},
                {".123", true, 0.123},
                };
    }

    @UseDataProvider("numericValueData")
    @Test
    public void createsNumericValueFromString(String rawString, boolean isDouble, Number number) throws Exception {
        JsonValue jsonValue = jsonValueFactory.createJsonValue(rawString);

        assertEquals(isDouble, jsonValue.isDouble());
        assertEquals(number, jsonValue.getNumberValue());
    }

    @Test
    public void createsStringValueWhenFailsToParseStringAsHexNUmber() throws Exception {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("0xzzz");

        assertTrue(jsonValue.isString());
        assertEquals("0xzzz", jsonValue.getStringValue());
    }

    @Test
    public void createsStringValueWhenFailsToParseStringAsOctNUmber() throws Exception {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("0zzz");

        assertTrue(jsonValue.isString());
        assertEquals("0zzz", jsonValue.getStringValue());
    }

    @Test
    public void createsStringValueWhenFailsToParseStringAsNUmber() throws Exception {
        JsonValue jsonValue = jsonValueFactory.createJsonValue("123x");

        assertTrue(jsonValue.isString());
        assertEquals("123x", jsonValue.getStringValue());
    }
}