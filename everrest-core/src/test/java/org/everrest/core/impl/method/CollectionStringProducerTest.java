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
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CollectionStringProducerTest {
    @Test
    public void createsListAndUsesValuesFromMap() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(List.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("string", "hello", "world");

        Object result = producer.createValue("string", values, null);

        assertEquals(newArrayList("hello", "world"), result);
    }

    @Test
    public void createsListAndUsesDefaultValue() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(List.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("string", "hello", "world");

        Object result = producer.createValue("__string", values, "hello world");

        assertEquals(newArrayList("hello world"), result);
    }

    @Test
    public void createsSetAndUsesValuesFromMap() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(Set.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("string", "hello", "world");

        Object result = producer.createValue("string", values, null);

        assertEquals(newHashSet("hello", "world"), result);
    }

    @Test
    public void createsSetAndUsesDefaultValue() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(Set.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("string", "hello", "world");

        Object result = producer.createValue("__string", values, "hello world");

        assertEquals(newHashSet("hello world"), result);
    }

    @Test
    public void createsSortedSetAndUsesValuesFromMap() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(SortedSet.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("string", "hello", "world");

        Object result = producer.createValue("string", values, null);

        assertEquals(newTreeSet(newArrayList("hello", "world")), result);
    }

    @Test
    public void createsSortedSetAndUsesDefaultValue() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(SortedSet.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();
        values.addAll("string", "hello", "world");

        Object result = producer.createValue("__string", values, "hello world");

        assertEquals(newTreeSet(newArrayList("hello world")), result);
    }

    @Test
    public void returnsNullWhenMapDoesNotContainRequiredValueAndDefaultValueIsNull() throws Exception {
        CollectionStringProducer producer = new CollectionStringProducer(List.class);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl();

        Object result = producer.createValue("number", values, null);
        assertNull(result);
    }
}