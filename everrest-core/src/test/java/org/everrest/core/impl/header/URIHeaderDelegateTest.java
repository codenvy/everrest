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
package org.everrest.core.impl.header;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class URIHeaderDelegateTest {
    private URIHeaderDelegate uriHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        uriHeaderDelegate = new URIHeaderDelegate();
    }

    @Test
    public void testValueOf() throws Exception {
        assertEquals(URI.create("http://localhost:8080/test"), uriHeaderDelegate.fromString("http://localhost:8080/test"));
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("http://localhost:8080/test", uriHeaderDelegate.toString(URI.create("http://localhost:8080/test")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenUriHeaderIsNull() throws Exception {
        uriHeaderDelegate.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenUriIsNull() throws Exception {
        uriHeaderDelegate.toString(null);
    }
}