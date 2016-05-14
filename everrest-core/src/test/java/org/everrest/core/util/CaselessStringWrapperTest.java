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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CaselessStringWrapperTest {
    private String                string         = "To Be Or Not To Be";
    private CaselessStringWrapper caselessString = new CaselessStringWrapper(string);

    @Test
    public void keepsOriginalStringAsIs() {
        assertEquals(string, caselessString.getString());
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(caselessString.equals(new CaselessStringWrapper(string.toUpperCase())));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(caselessString.hashCode(), new CaselessStringWrapper(string.toUpperCase()).hashCode());
    }
}