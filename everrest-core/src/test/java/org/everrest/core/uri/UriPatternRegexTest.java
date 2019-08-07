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
package org.everrest.core.uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriPatternRegexTest {

    @Parameterized.Parameters(name = "{index} When URI template is {0} then regex is {1}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"/", "(/.*)?"},
                {"/a", "/a(/.*)?"},
                {"a", "/a(/.*)?"},
                {"/a/", "/a(/.*)?"},
                {"/a/{x}", "/a/([^/]+?)(/.*)?"},
                {"/a/{x}/", "/a/([^/]+?)(/.*)?"},
                {"/a/{x:}/", "/a/([^/]+?)(/.*)?"},
                {"/a/{x  :   .*}/", "/a/(.*)(/.*)?"},
        });
    }

    @Parameterized.Parameter(0)
    public String uriTemplate;
    @Parameterized.Parameter(1)
    public String expectedRegex;

    @Test
    public void testRegex() {
        UriPattern pattern = new UriPattern(uriTemplate);
        assertEquals(expectedRegex, pattern.getRegex());
    }
}
