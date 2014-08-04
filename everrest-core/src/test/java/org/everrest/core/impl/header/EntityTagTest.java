/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import org.everrest.core.impl.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.EntityTag;

/**
 * @author andrew00x
 */
public class EntityTagTest extends BaseTest {

    @Test
    public void testToString() {
        EntityTag entityTag = new EntityTag("test \"test\"", false);
        Assert.assertEquals("\"test \\\"test\\\"\"", entityTag.toString());
    }

    @Test
    public void testToStringWeak() {
        EntityTag entityTag = new EntityTag("test", true);
        Assert.assertEquals("W/\"test\"", entityTag.toString());
    }

    @Test
    public void testToString2() {
        EntityTag entityTag = new EntityTag("test \"test\"", false);
        Assert.assertEquals("\"test \\\"test\\\"\"", entityTag.toString());
    }

    @Test
    public void testFromString() {
        String header = "\"test\"";
        EntityTag entityTag = EntityTag.valueOf(header);
        Assert.assertFalse(entityTag.isWeak());
        Assert.assertEquals("test", entityTag.getValue());
    }

    @Test
    public void testFromStringWeak() {
        String header = "W/\"test\"";
        EntityTag entityTag = EntityTag.valueOf(header);
        Assert.assertTrue(entityTag.isWeak());
        Assert.assertEquals("test", entityTag.getValue());
    }

    @Test
    public void testFromStringWeak2() {
        String header = "W/\"test \\\"test\\\"\"";
        EntityTag entityTag = EntityTag.valueOf(header);
        Assert.assertTrue(entityTag.isWeak());
        Assert.assertEquals("test \"test\"", entityTag.getValue());
    }
}
