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

import javax.ws.rs.core.EntityTag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityTagHeaderDelegateTest {
    private EntityTagHeaderDelegate entityTagHeaderDelegate;

    @Before
    public void setUp() throws Exception {
        entityTagHeaderDelegate = new EntityTagHeaderDelegate();
    }

    @Test
    public void testToStringSimple() {
        EntityTag entityTag = new EntityTag("test", false);
        assertEquals("\"test\"", entityTagHeaderDelegate.toString(entityTag));
    }

    @Test
    public void testToStringSimpleWeak() {
        EntityTag entityTag = new EntityTag("test", true);
        assertEquals("W/\"test\"", entityTagHeaderDelegate.toString(entityTag));
    }

    @Test
    public void testToStringWithQuotedString() {
        EntityTag entityTag = new EntityTag("test \"test\"", false);
        assertEquals("\"test \\\"test\\\"\"", entityTagHeaderDelegate.toString(entityTag));
    }

    @Test
    public void parsesString() {
        EntityTag entityTag = entityTagHeaderDelegate.fromString("\"test\"");

        assertFalse(entityTag.isWeak());
        assertEquals("test", entityTag.getValue());
    }

    @Test
    public void parsesStringQuoted() {
        EntityTag entityTag = entityTagHeaderDelegate.fromString("\"test \\\"test\\\"\"");

        assertFalse(entityTag.isWeak());
        assertEquals("test \"test\"", entityTag.getValue());
    }

    @Test
    public void parsesStringWeak() {
        EntityTag entityTag = entityTagHeaderDelegate.fromString("W/\"test\"");

        assertTrue(entityTag.isWeak());
        assertEquals("test", entityTag.getValue());
    }

    @Test
    public void parsesStringQuotedWeak() {
        EntityTag entityTag = entityTagHeaderDelegate.fromString("W/\"test \\\"test\\\"\"");

        assertTrue(entityTag.isWeak());
        assertEquals("test \"test\"", entityTag.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenEntityTagHeaderIsNull() throws Exception {
        entityTagHeaderDelegate.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenEntityTagIsNull() throws Exception {
        entityTagHeaderDelegate.toString(null);
    }
}
