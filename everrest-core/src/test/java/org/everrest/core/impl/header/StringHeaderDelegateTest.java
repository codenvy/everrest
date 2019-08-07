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
package org.everrest.core.impl.header;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringHeaderDelegateTest {
    private StringHeaderDelegate stringHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        stringHeaderDelegate = new StringHeaderDelegate();
    }

    @Test
    public void testValueOf() throws Exception {
        assertEquals("to be or not to be", stringHeaderDelegate.fromString("to be or not to be"));
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("to be or not to be", stringHeaderDelegate.toString("to be or not to be"));
    }
}