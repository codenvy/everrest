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
package org.everrest.core.impl.uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PathSegmentImplTest {
    @Parameterized.Parameters(name = "{index} Path: {0}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {null,            false, "",  newArrayList()},
                {"",              false, "",  newArrayList()},
                {"x",             false, "x", newArrayList()},
                {"x;",            false, "x", newArrayList()},
                {"x;;",           false, "x", newArrayList()},
                {"x;=b",          false, "x", newArrayList()},
                {"x;a=",          false, "x", newArrayList("a", "")},
                {"x;a",           false, "x", newArrayList("a", "")},
                {"x;a;",          false, "x", newArrayList("a", "")},
                {"x;a;;",         false, "x", newArrayList("a", "")},
                {"x;;a",          false, "x", newArrayList("a", "")},
                {"x;a=;",         false, "x", newArrayList("a", "")},
                {"x;a=;;",        false, "x", newArrayList("a", "")},
                {"x;a=b",         false, "x", newArrayList("a", "b")},
                {"x;a=b;",        false, "x", newArrayList("a", "b")},
                {"x;a=b;;",       false, "x", newArrayList("a", "b")},
                {"x;a=b;c=d",     false, "x", newArrayList("a", "b", "c", "d")},
                {"x;a=b;;c=d",    false, "x", newArrayList("a", "b", "c", "d")},
                {"x;+a+=%20b%20", true,  "x", newArrayList(" a ", " b ")},
                {"x;+a+=%20b%20", false, "x", newArrayList(" a ", "%20b%20")},
                {";a=b;c=d",      false, "",  newArrayList("a", "b", "c", "d")},
        });
    }


    @Parameter(0) public String       pathSegmentString;
    @Parameter(1) public boolean      decode;
    @Parameter(2) public String       expectedPath;
    @Parameter(3) public List<String> expectedMatrixParameters;

    @Test
    public void parsesPathSegmentsFromString() {
        PathSegment pathSegment = PathSegmentImpl.fromString(pathSegmentString, decode);
        MultivaluedMap<String, String> matrixParameters = pathSegment.getMatrixParameters();

        assertEquals(expectedPath, pathSegment.getPath());
        assertEquals(expectedMatrixParameters.size() / 2, matrixParameters.size());

        for (int i = 0; i < expectedMatrixParameters.size(); i += 2) {
            String expectedMatrixParameterName = expectedMatrixParameters.get(i);
            String expectedMatrixParameterValue = expectedMatrixParameters.get(i + 1);
            assertEquals(expectedMatrixParameterValue, matrixParameters.getFirst(expectedMatrixParameterName));
        }
    }
}