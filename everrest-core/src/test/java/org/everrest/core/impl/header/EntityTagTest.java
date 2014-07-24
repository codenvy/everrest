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

import javax.ws.rs.core.EntityTag;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class EntityTagTest extends BaseTest {

    public void testToString() {
        EntityTag entityTag = new EntityTag("test", true);
        assertEquals("W/\"test\"", entityTag.toString());

        entityTag = new EntityTag("test \"test\"", true);
        assertEquals("W/\"test \\\"test\\\"\"", entityTag.toString());

        entityTag = new EntityTag("test \"test\"", false);
        assertEquals("\"test \\\"test\\\"\"", entityTag.toString());
    }

    public void testFromString() {
        String header = "W/\"test\"";
        EntityTag entityTag = EntityTag.valueOf(header);
        assertTrue(entityTag.isWeak());
        assertEquals("test", entityTag.getValue());

        header = "\"test\"";
        entityTag = EntityTag.valueOf(header);
        assertFalse(entityTag.isWeak());
        assertEquals("test", entityTag.getValue());

        header = "W/\"test \\\"test\\\"\"";
        entityTag = EntityTag.valueOf(header);
        assertTrue(entityTag.isWeak());
        assertEquals("test \"test\"", entityTag.getValue());
    }

}
