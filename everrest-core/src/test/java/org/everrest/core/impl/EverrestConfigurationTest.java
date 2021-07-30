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
package org.everrest.core.impl;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class EverrestConfigurationTest {
    private EverrestConfiguration everrestConfiguration;

    @Before
    public void setUp() throws Exception {
        everrestConfiguration = new EverrestConfiguration();
    }

    @Test
    public void testDefaultEverrestConfiguration() {
        assertTrue(everrestConfiguration.isHttpMethodOverride());
        assertTrue(everrestConfiguration.isAsynchronousSupported());
        assertEquals("/async", everrestConfiguration.getAsynchronousServicePath());
        assertEquals(10, everrestConfiguration.getAsynchronousPoolSize());
        assertTrue(everrestConfiguration.isCheckSecurity());
        assertFalse(everrestConfiguration.isNormalizeUri());
        assertEquals(100, everrestConfiguration.getAsynchronousQueueSize());
        assertEquals(512, everrestConfiguration.getAsynchronousCacheSize());
        assertEquals(60, everrestConfiguration.getAsynchronousJobTimeout());
        assertEquals(204800, everrestConfiguration.getMaxBufferSize());
    }

    @Test
    public void copiesAllPropertiesFromOtherEverrestConfiguration() {
        everrestConfiguration.setProperty("foo", "bar");
        everrestConfiguration.setProperty("foo2", "bar2");
        EverrestConfiguration newEverrestConfiguration = new EverrestConfiguration(everrestConfiguration);

        assertEquals(everrestConfiguration.getAllProperties(), newEverrestConfiguration.getAllProperties());
    }

    @Test
    public void removesPropertyIfNullValueProvided() {
        everrestConfiguration.setProperty("foo", "bar");
        everrestConfiguration.setProperty("foo2", "bar2");
        everrestConfiguration.setProperty("foo2", null);

        assertEquals(ImmutableMap.of("foo", "bar"), everrestConfiguration.getAllProperties());
    }

    @DataProvider
    public static Object[][] forBooleanPropertyTest() {
        return new Object[][] {
                {"1", true},
                {"yes", true},
                {"true", true},
                {"on", true},
                {"Yes", true},
                {"True", true},
                {"On", true},
                {"", false},
                {"0", false},
                {"Off", false},
                {null, false}
        };
    }

    @Test
    @UseDataProvider("forBooleanPropertyTest")
    public void testBooleanProperty(String booleanPropertyAsString, boolean expectedBooleanRepresentation) {
        everrestConfiguration.setProperty("foo", booleanPropertyAsString);
        assertEquals(expectedBooleanRepresentation, everrestConfiguration.getBooleanProperty("foo", false));
    }

    @Test
    public void ignoresNumberPropertiesWithInvalidFormat() {
        everrestConfiguration.setProperty("foo", "bar");
        assertEquals(11.0, everrestConfiguration.getNumberProperty("foo", 11.0), 0.0);
    }

    @Test
    public void setsCustomPropertiesForConfiguration() {
        everrestConfiguration.setCheckSecurity(false);
        everrestConfiguration.setHttpMethodOverride(false);
        everrestConfiguration.setNormalizeUri(true);
        everrestConfiguration.setAsynchronousSupported(false);
        everrestConfiguration.setAsynchronousServicePath("/async2");
        everrestConfiguration.setAsynchronousPoolSize(20);
        everrestConfiguration.setAsynchronousQueueSize(256);
        everrestConfiguration.setAsynchronousCacheSize(100);
        everrestConfiguration.setAsynchronousJobTimeout(10);
        everrestConfiguration.setMaxBufferSize(2048);

        assertFalse(everrestConfiguration.isHttpMethodOverride());
        assertFalse(everrestConfiguration.isAsynchronousSupported());
        assertEquals("/async2", everrestConfiguration.getAsynchronousServicePath());
        assertEquals(20, everrestConfiguration.getAsynchronousPoolSize());
        assertFalse(everrestConfiguration.isCheckSecurity());
        assertTrue(everrestConfiguration.isNormalizeUri());
        assertEquals(256, everrestConfiguration.getAsynchronousQueueSize());
        assertEquals(100, everrestConfiguration.getAsynchronousCacheSize());
        assertEquals(10, everrestConfiguration.getAsynchronousJobTimeout());
        assertEquals(2048, everrestConfiguration.getMaxBufferSize());
    }
}