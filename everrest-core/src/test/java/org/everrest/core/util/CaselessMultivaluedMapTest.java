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
package org.everrest.core.util;

import org.everrest.core.ExtMultivaluedMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
public class CaselessMultivaluedMapTest {

    private ExtMultivaluedMap<String, String> map;

    @Before
    public void setUp() throws Exception {
        map = new CaselessMultivaluedMap<>();
    }

    @Test
    public void testPut() {
        List<String> list = newArrayList("a", "b", "c");
        map.put("kEy1", list);
        assertEquals(1, map.size());
        assertEquals(list, map.get("key1"));
    }

    @Test
    public void testAdd() {
        map.add("KeY1", "a");
        assertEquals(1, map.size());
        assertEquals("a", map.getFirst("key1"));
        assertEquals(1, map.get("KEY1").size());
    }

    @Test
    public void testPutSingle() {
        map.put("kEy1", newArrayList("a", "b", "c"));
        map.putSingle("key1", "value");
        assertEquals(1, map.size());
        assertEquals("value", map.getFirst("key1"));
        assertEquals(1, map.get("KEY1").size());
    }

    @Test
    public void testContainsKey() {
        map.put("kEy1", newArrayList("a", "b", "c"));
        map.put("KEy2", newArrayList("e", "f"));
        assertEquals(2, map.size());
        assertTrue(map.containsKey("KEY1"));
        assertTrue(map.containsKey("key2"));
    }

    @Test
    public void testRemove() {
        map.put("kEy1", newArrayList("a", "b", "c"));
        map.put("KEy2", newArrayList("e", "f"));
        assertEquals(2, map.size());
        assertEquals(new ArrayList<>(Arrays.asList("e", "f")), map.remove("KEY2"));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("kEy2"));
    }

    @Test
    public void testGetList() {
        assertEquals(0, map.size());
        List<String> list = map.getList("key1");
        assertNotNull(list);
        assertEquals(0, list.size());
        assertEquals(1, map.size());
    }

    @Test
    public void testEntrySet() {
        Set<Entry<String, List<String>>> entries = map.entrySet();
        map.put("k", newArrayList("a", "b"));
        map.put("e", newArrayList("c", "d"));
        map.put("Y", newArrayList("e", "f"));
        assertEquals(3, map.size());
        assertEquals(3, entries.size());

        assertTrue(entries.remove(new java.util.Map.Entry<String, List<String>>() {
            public String getKey() {
                return "E";
            }

            public List<String> getValue() {
                return Arrays.asList("c", "d");
            }

            public List<String> setValue(List<String> value) {
                return Arrays.asList("c", "d");
            }
        }));
        assertEquals(2, map.size());
        assertEquals(2, entries.size());
        assertTrue(map.containsKey("K"));
        assertTrue(map.containsKey("y"));
        assertFalse(map.containsKey("e"));
        entries.clear();
        assertEquals(0, map.size());
        assertEquals(0, entries.size());
    }

    @Test
    public void testKeySet() {
        Set<String> keys = map.keySet();
        map.put("k", newArrayList("a", "b"));
        map.put("e", newArrayList("c", "d"));
        map.put("Y", newArrayList("e", "f"));
        assertEquals(3, map.size());
        assertEquals(3, keys.size());
        assertTrue(keys.contains("K"));
        assertTrue(keys.contains("Y"));
        assertTrue(keys.contains("e"));
        assertTrue(keys.remove("Y"));
        assertEquals(2, map.size());
        assertEquals(2, keys.size());
        assertTrue(keys.contains("K"));
        assertTrue(keys.contains("e"));
        assertFalse(keys.contains("Y"));
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }
        assertEquals(0, map.size());
        assertEquals(0, keys.size());
    }
}
