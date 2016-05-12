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
package org.everrest.core.impl.method;

import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringProducerTest {
    private StringProducer producer;

    @Before
    public void setUp() throws Exception {
        producer = new StringProducer();
    }

    @Test
    public void createsInstanceAndUsesSingleValueFromMap() throws Exception {
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.putSingle("foo", "bar");

        Object result = producer.createValue("foo", values, null);
        assertEquals("bar", result);
    }

    @Test
    public void createsInstanceAndUsesFirstValueFromMap() throws Exception {
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("foo", "bar", "baz");

        Object result = producer.createValue("foo", values, null);
        assertEquals("bar", result);
    }

    @Test
    public void createsInstanceAndUsesDefault() throws Exception {
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();

        Object result = producer.createValue("foo", values, "baz");
        assertEquals("baz", result);
    }

    @Test
    public void returnsNullWhenMapDoesNotContainRequiredValueAndDefaultValueIsNull() throws Exception {
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();

        Object result = producer.createValue("foo", values, null);
        assertNull(result);
    }
}
