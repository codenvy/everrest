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
package org.everrest.core.uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriTemplateParserTest {
    @Parameterized.Parameters(name = "{index} When URI template is {0} then Regex is {1}, number of literal characters is {2} and parameter names are: {3}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"/a",                    "/a",                       2, newArrayList()},
                {"a",                     "a",                        1, newArrayList()},
                {"/a/",                   "/a/",                      3, newArrayList()},
                {"/a/{x}",                "/a/([^/]+?)",              3, newArrayList("x")},
                {"/a/{x}/b",              "/a/([^/]+?)/b",            5, newArrayList("x")},
                {"/{x}",                  "/([^/]+?)",                1, newArrayList("x")},
                {"/a/{x}/b/{y}/c",        "/a/([^/]+?)/b/([^/]+?)/c", 8, newArrayList("x", "y")},
                {"/a/{x}/{y}/b/",         "/a/([^/]+?)/([^/]+?)/b/",  7, newArrayList("x", "y")},
                {"/a/{x}/{y}/",           "/a/([^/]+?)/([^/]+?)/",    5, newArrayList("x", "y")},
                {"/{x}/a/{y}/",           "/([^/]+?)/a/([^/]+?)/",    5, newArrayList("x", "y")},
                {"/a/{x:}",               "/a/([^/]+?)",              3, newArrayList("x")},
                {"/a/{x : }",             "/a/([^/]+?)",              3, newArrayList("x")},
                {"/a/{  x  :  }",         "/a/([^/]+?)",              3, newArrayList("x")},
                {"/a/{x:.+}",             "/a/(.+)",                  3, newArrayList("x")},
                {"/a/{x:(/)?}",           "/a/((/)?)",                3, newArrayList("x")},
                {"/{x}/{y:.+}/{z:.*}",    "/([^/]+?)/(.+)/(.*)",      3, newArrayList("x", "y", "z")},
                {"/a /{x}/{y:.+}/{z:.*}", "/a%20/([^/]+?)/(.+)/(.*)", 6, newArrayList("x", "y", "z")}
        });
    }

    @Parameter(0) public String       uriTemplate;
    @Parameter(1) public String       regex;
    @Parameter(2) public int          numberOfLiteralCharacters;
    @Parameter(3) public List<String> expectedParameterNames;

    @Test
    public void failsCreateUriPatternWith() throws Exception {
        UriTemplateParser parser = new UriTemplateParser(uriTemplate);
        assertEquals(regex, parser.getRegex());
        assertEquals(numberOfLiteralCharacters, parser.getNumberOfLiteralCharacters());
        assertEquals(expectedParameterNames, parser.getParameterNames());
    }
}
