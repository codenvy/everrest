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
package org.everrest.core.util;

import org.everrest.core.impl.BaseTest;

import javax.ws.rs.core.MediaType;
import java.util.Iterator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class MediaTypeMapTest extends BaseTest {

    public void testSort() {
        MediaTypeMap<Object> m = new MediaTypeMap<Object>();
        m.put(new MediaType(), "*/*");
        m.put(new MediaType("text", "*"), "text/*");
        m.put(new MediaType("text", "plain"), "text/plain");
        m.put(new MediaType("application", "*+xml"), "application/*+xml");
        m.put(new MediaType("text", "xml"), "text/xml");
        m.put(new MediaType("application", "*"), "application/*");
        m.put(new MediaType("application", "atom+*"), "application/atom+*");
        System.out.println(m.values());
        Iterator<Object> values = m.values().iterator();
        assertEquals("text/plain", values.next());
        assertEquals("text/xml", values.next());
        assertEquals("application/atom+*", values.next());
        assertEquals("application/*+xml", values.next());
        assertEquals("application/*", values.next());
        assertEquals("text/*", values.next());
        assertEquals("*/*", values.next());
    }

}
