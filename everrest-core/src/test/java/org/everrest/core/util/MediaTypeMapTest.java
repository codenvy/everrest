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

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Iterator;

/**
 * @author andrew00x
 */
public class MediaTypeMapTest {

    @Test
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
        Assert.assertEquals("text/plain", values.next());
        Assert.assertEquals("text/xml", values.next());
        Assert.assertEquals("application/atom+*", values.next());
        Assert.assertEquals("application/*+xml", values.next());
        Assert.assertEquals("application/*", values.next());
        Assert.assertEquals("text/*", values.next());
        Assert.assertEquals("*/*", values.next());
    }
}
