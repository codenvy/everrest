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
