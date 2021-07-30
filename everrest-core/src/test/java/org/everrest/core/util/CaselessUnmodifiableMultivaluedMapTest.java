/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.util;

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author andrew00x
 */
public class CaselessUnmodifiableMultivaluedMapTest {
    private ExtMultivaluedMap<String, String> map;

    @Before
    public void setUp() throws Exception {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("K", "a");
        m.add("K", "b");
        m.add("e", "c");
        m.add("y", "d");
        m.add("y", "e");
        map = new CaselessUnmodifiableMultivaluedMap<>(m);
    }

    @Test
    public void testGet() {
        assertEquals("a", map.getFirst("k"));
        assertEquals(Arrays.asList("a", "b"), map.get("k"));
        assertEquals("c", map.getFirst("E"));
        assertEquals(Arrays.asList("d", "e"), map.get("Y"));
    }

    @Test
    public void testGetList() {
        assertEquals(Arrays.asList("a", "b"), map.getList("k"));
        List<String> list = map.getList("x");
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void tesClear() {
        map.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        map.remove("k");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        map.put("k", new ArrayList<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll() {
        map.putAll(new MultivaluedMapImpl());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutSingle() {
        map.putSingle("k", "value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAdd() {
        map.add("k", "value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntryRemove() {
        map.entrySet().remove(new java.util.Map.Entry<String, List<String>>() {
            public String getKey() {
                return "K";
            }

            public List<String> getValue() {
                return Arrays.asList("a", "b");
            }

            public List<String> setValue(List<String> value) {
                return Arrays.asList("a", "b");
            }
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntryRemoveAll() {
        map.entrySet().removeAll(new ArrayList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntryRetainAll() {
        map.entrySet().retainAll(new ArrayList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntryIteratorRemove() {
        Iterator<Map.Entry<String, List<String>>> i = map.entrySet().iterator();
        i.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntryUpdateValue() {
        map.entrySet().iterator().next().setValue(new ArrayList<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeysRemove() {
        map.keySet().remove("K");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeysRemoveAll() {
        map.keySet().removeAll(Arrays.asList("k", "y", "e"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeysRetainAll() {
        map.keySet().retainAll(Arrays.asList("k", "y"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeysIteratorRemove() {
        Iterator<String> i = map.keySet().iterator();
        i.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClearList() {
        map.get("k").clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClearKeys() {
        map.keySet().clear();
    }
}
