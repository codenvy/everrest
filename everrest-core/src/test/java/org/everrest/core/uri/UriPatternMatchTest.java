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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public class UriPatternMatchTest {
    @Parameterized.Parameters(name = "{index} When URI template is {0} an URI is {1} then URI parameters are {2}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"/", "/a/b", newArrayList("/a/b")},
                {"/", "/a/b/", newArrayList("/a/b/")},
                {"/a", "/a/b", newArrayList("/b")},
                {"a", "/a/b", newArrayList("/b")},
                {"/a/{x}", "/a/b", newArrayList("b", null)},
                {"/a/{x}", "/a/b/", newArrayList("b", "/")},
                {"/a/{x:.*}", "/a/b/", newArrayList("b/", null)},
                {"/a/{x:.*}", "/a/b", newArrayList("b", null)},
                {"/a/{x:.*}", "/a/b/c/d/e", newArrayList("b/c/d/e", null)},
                {"/a/{x:.*}", "/a/b/c/d/e/", newArrayList("b/c/d/e/", null)},
                {"/a{x:.*}", "/a/b/c/d/e", newArrayList("/b/c/d/e", null)},
                {"/a{x:.*}", "/a/b/c/d/e/", newArrayList("/b/c/d/e/", null)},
                {"/a/{x}{y:.*}", "/a/b/c/d/e", newArrayList("b", "/c/d/e", null)},
                {"/a/{x}/{y:.*}", "/a/b/c/d/e", newArrayList("b", "c/d/e", null)},
                {"/a/{x}/{y:.*}/{z}", "/a/b/c/d/e", newArrayList("b", "c/d", "e", null)},
                {"/{a}/{b}/{c}/{x:.*}/{e}/{f}/{g}/", "/a/b/c/1/2/3/4/5/e/f/g/",
                 newArrayList("a", "b", "c", "1/2/3/4/5", "e", "f", "g", "/")},
                {"/{a}/{b}/{c}/{x:.*}/{e}/{f}/{g}/", "/a/b/c/1/2/3/4/5/e/f/g",
                 newArrayList("a", "b", "c", "1/2/3/4/5", "e", "f", "g", null)},
                {"/a /{x}{y:(/)?}", "/a%20/b/", newArrayList("b", "/", null)},
                {"/a/{x}{y:(/)?}", "/a/b", newArrayList("b", "", null)},
                {"/{x:\\d+}.{y:\\d+}", "/111.222", newArrayList("111", "222", null)},
                {"/{x:\\d+}.{y:\\d+}", "/111.222/", newArrayList("111", "222", "/")},
                {"/a/b/{x}{y:(/)?}", "/a/b/c", newArrayList("c", "", null)},
                {"/a/b/{x}{y:(/)?}", "/a/b/c/", newArrayList("c", "/", null)},
                {"/a/b/{x}/{y}/{z}/{X:.*}", "/a/b/c/d/e/f/g/h", newArrayList("c", "d", "e", "f/g/h", null)},
                {"/a/b/{x}/{y}/{z}/{X:.*}", "/a/b/c/d/e/f/g/h/", newArrayList("c", "d", "e", "f/g/h/", null)},
                {"/a/b/{x}/{y}/{z}/{X:.*}{Y:[/]+?}", "/a/b/c/d/e/f/g/h/", newArrayList("c", "d", "e", "f/g/h", "/", null)},
                {"/a/b/{x}/{y}/{z}/{X:.*}{Y:/+?}", "/a/b/c/d/e/f/g/h/", newArrayList("c", "d", "e", "f/g/h", "/", null)},
                {"/a/b/{x}/{X:.*}/{y}/{z}", "/a/b/c/d/e/f/g/h", newArrayList("c", "d/e/f", "g", "h", null)},
                {"/a/b/{x}/{X:.*}/{y}", "/a/b/c/d/e/f/g/h", newArrayList("c", "d/e/f/g", "h", null)},
                {"/a/b/{x}/{X:.*}/{y}", "/a/b/c/d/e/f/g/h/", newArrayList("c", "d/e/f/g", "h", "/")}
        });
    }


    @Parameter(0)
    public String       uriPattern;
    @Parameter(1)
    public String       uri;
    @Parameter(2)
    public List<String> expectedUriParameters;

    @Test
    public void testMatch() {
        UriPattern pattern = new UriPattern(uriPattern);
        List<String> newArrayList = new ArrayList<>();
        assertTrue(pattern.match(uri, newArrayList));
        assertEquals(expectedUriParameters, newArrayList);
    }
}
