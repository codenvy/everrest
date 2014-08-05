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

import org.everrest.core.ExtMultivaluedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        map.put("kEy1", list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(list, map.get("key1"));
    }

    @Test
    public void testAdd() {
        map.add("KeY1", "a");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("a", map.getFirst("key1"));
        Assert.assertEquals(1, map.get("KEY1").size());
    }

    @Test
    public void testPutSingle() {
        map.put("kEy1", new ArrayList<>(Arrays.asList("a", "b", "c")));
        map.putSingle("key1", "value");
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("value", map.getFirst("key1"));
        Assert.assertEquals(1, map.get("KEY1").size());
    }

    @Test
    public void testContainsKey() {
        map.put("kEy1", new ArrayList<>(Arrays.asList("a", "b", "c")));
        map.put("KEy2", new ArrayList<>(Arrays.asList("e", "f")));
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey("KEY1"));
        Assert.assertTrue(map.containsKey("key2"));
    }

    @Test
    public void testRemove() {
        map.put("kEy1", new ArrayList<>(Arrays.asList("a", "b", "c")));
        map.put("KEy2", new ArrayList<>(Arrays.asList("e", "f")));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(new ArrayList<>(Arrays.asList("e", "f")), map.remove("KEY2"));
        Assert.assertEquals(1, map.size());
        Assert.assertTrue(map.containsKey("key1"));
        Assert.assertFalse(map.containsKey("kEy2"));
    }

    @Test
    public void testGetList() {
        Assert.assertEquals(0, map.size());
        List<String> list = map.getList("key1");
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        Assert.assertEquals(1, map.size());
    }

    @Test
    public void testEntrySet() {
        Set<Entry<String, List<String>>> entries = map.entrySet();
        map.put("k", new ArrayList<>(Arrays.asList("a", "b")));
        map.put("e", new ArrayList<>(Arrays.asList("c", "d")));
        map.put("Y", new ArrayList<>(Arrays.asList("e", "f")));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(3, entries.size());

        Assert.assertTrue(entries.remove(new java.util.Map.Entry<String, List<String>>() {
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
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(2, entries.size());
        Assert.assertTrue(map.containsKey("K"));
        Assert.assertTrue(map.containsKey("y"));
        Assert.assertFalse(map.containsKey("e"));
        entries.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, entries.size());
    }

    @Test
    public void testKeySet() {
        Set<String> keys = map.keySet();
        map.put("k", new ArrayList<>(Arrays.asList("a", "b")));
        map.put("e", new ArrayList<>(Arrays.asList("c", "d")));
        map.put("Y", new ArrayList<>(Arrays.asList("e", "f")));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(3, keys.size());
        Assert.assertTrue(keys.contains("K"));
        Assert.assertTrue(keys.contains("Y"));
        Assert.assertTrue(keys.contains("e"));
        Assert.assertTrue(keys.remove("Y"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("K"));
        Assert.assertTrue(keys.contains("e"));
        Assert.assertFalse(keys.contains("Y"));
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(0, keys.size());
    }
}
