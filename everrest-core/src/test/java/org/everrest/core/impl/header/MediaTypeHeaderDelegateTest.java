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

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

/**
 * @author andrew00x
 */
@RunWith(DataProviderRunner.class)
public class MediaTypeHeaderDelegateTest {

    private MediaTypeHeaderDelegate mediaTypeHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        mediaTypeHeaderDelegate = new MediaTypeHeaderDelegate();
    }

    @DataProvider
    public static Object[][] forFromString() {
        return new Object[][]{
                {"text", new MediaType("text", "*")},

                {"text/plain", new MediaType("text", "plain")},

                {"text / plain", new MediaType("text", "plain")},

                {"text;charset =     utf8", new MediaType("text", "*", ImmutableMap.of("charset", "utf8"))},

                {"text/plain;   charset   =  utf-8  ;  test=hello",
                 new MediaType("text", "plain", ImmutableMap.of("charset", "utf-8", "test", "hello"))},

                {"; charset=utf8", new MediaType("*", "*", ImmutableMap.of("charset", "utf8"))}
        };
    }

    @UseDataProvider("forFromString")
    @Test
    public void fromString(String header, MediaType expectedResult) {
        MediaType mediaType = mediaTypeHeaderDelegate.fromString(header);
        assertEquals(expectedResult, mediaType);

    }

    @DataProvider
    public static Object[][] forTestToString() {
        return new Object[][]{
                {new MediaType("text", "plain"), "text/plain"},

                {new MediaType("text", "plain", ImmutableMap.of("charset", "utf8")), "text/plain;charset=utf8"},

                {new MediaType("text", "plain", ImmutableMap.of("charset", "utf8", "test", "hello")), "text/plain;charset=utf8;test=hello"},
        };
    }

    @UseDataProvider("forTestToString")
    @Test
    public void testToString(MediaType mediaType, String expectedResult) {
        String header = mediaTypeHeaderDelegate.toString(mediaType);
        assertEquals(expectedResult, header);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenContentTypeHeaderIsNull() throws Exception {
        mediaTypeHeaderDelegate.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenMediaTypeIsNull() throws Exception {
        mediaTypeHeaderDelegate.toString(null);
    }
}
