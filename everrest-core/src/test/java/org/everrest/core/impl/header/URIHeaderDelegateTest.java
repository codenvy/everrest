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