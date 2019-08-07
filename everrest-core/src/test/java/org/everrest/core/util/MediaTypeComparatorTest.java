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
package org.everrest.core.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MediaTypeComparatorTest {
    @Parameterized.Parameters(name = "{index} => Left: {0}; Right: {1}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {new MediaType("*", "*"),        new MediaType("*", "*"),         0},
                {new MediaType("text", "*"),     new MediaType("*", "*"),        -1},
                {new MediaType("*", "*"),        new MediaType("text", "*"),      1},
                {new MediaType("text", "plain"), new MediaType("*", "*"),        -1},
                {new MediaType("*", "*"),        new MediaType("text", "plain"),  1},
                {new MediaType("text", "plain"), new MediaType("text", "*"),     -1},
                {new MediaType("text", "*"),     new MediaType("text", "plain"),  1},

                {new MediaType("application", "xml"),      new MediaType("application", "xml+*"),    -1},
                {new MediaType("application", "xml+*"),    new MediaType("application", "xml"),       1},
                {new MediaType("application", "xml"),      new MediaType("application", "atom+xml"), -1},
                {new MediaType("application", "atom+xml"), new MediaType("application", "xml"),       1},
                {new MediaType("application", "atom+*"),   new MediaType("application", "*+xml"),    -1},
                {new MediaType("application", "*+xml"),    new MediaType("application", "atom+*"),    1},
        });
    }


    @Parameter(0) public MediaType left;
    @Parameter(1) public MediaType right;
    @Parameter(2) public int       expectedResult;

    private MediaTypeComparator mediaTypeComparator = new MediaTypeComparator();

    @Test
    public void testCompare() throws Exception {
       assertEquals(expectedResult, mediaTypeComparator.compare(left, right));
    }
}