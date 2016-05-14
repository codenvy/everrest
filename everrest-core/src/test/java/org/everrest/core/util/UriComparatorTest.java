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
package org.everrest.core.util;

import org.everrest.core.uri.UriPattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriComparatorTest {
    @Parameters(name = "{index} => Left: {0}; Right: {1}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"/a",             "a/{b}",          1},
                {"a/{b}",          "/a/b/{c}/d/{e}", 1},
                {"/a/b/{c}/d/{e}", "/a/b/c/d/{e}",   1},
                {"/a/b/c/d/{e}",   "/a/b/c/d/e",     1},
        });
    }

    @Parameter(0) public String left;
    @Parameter(1) public String right;
    @Parameter(2) public int    expectedResult;

    private UriPatternComparator uriPatternComparator = new UriPatternComparator();

    @Test
    public void testName() throws Exception {
       assertEquals(1, uriPatternComparator.compare(new UriPattern(left), new UriPattern(right)));
    }
}
