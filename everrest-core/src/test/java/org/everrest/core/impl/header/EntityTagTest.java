/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
