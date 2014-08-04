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
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        Assert.assertEquals("a", map.getFirst("k"));
        Assert.assertEquals(Arrays.asList("a", "b"), map.get("k"));
        Assert.assertEquals("c", map.getFirst("E"));
        Assert.assertEquals(Arrays.asList("d", "e"), map.get("Y"));
    }

    @Test
    public void testGetList() {
        Assert.assertEquals(Arrays.asList("a", "b"), map.getList("k"));
        List<String> list = map.getList("x");
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
    }

    public void tesClear() {
        try {
            map.clear();
            Assert.fail("UnsupportedOperationException should be thrown fro 'clear'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testRemove() {
        try {
            map.remove("k");
            Assert.fail("UnsupportedOperationException should be thrown for 'remove'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testPut() {
        try {
            map.put("k", new ArrayList<String>());
            Assert.fail("UnsupportedOperationException should be thrown for 'put'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testPutAll() {
        try {
            map.putAll(new MultivaluedMapImpl());
            Assert.fail("UnsupportedOperationException should be thrown for 'puAll'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testPutSingle() {
        try {
            map.putSingle("k", "value");
            Assert.fail("UnsupportedOperationException should be thrown for 'putSingle'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testAdd() {
        try {
            map.add("k", "value");
            Assert.fail("UnsupportedOperationException should be thrown for 'add'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testEntryRemove() {
        try {
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
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testEntryRemoveAll() {
        try {
            map.entrySet().removeAll(new ArrayList());
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testEntryRetainAll() {
        try {
            map.entrySet().retainAll(new ArrayList());
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testEntryIteratorRemove() {
        try {
            Iterator<Map.Entry<String, List<String>>> i = map.entrySet().iterator();
            while (i.hasNext()) {
                i.remove();
            }
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testEntryUpdateValue() {
        try {
            map.entrySet().iterator().next().setValue(new ArrayList<String>());
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testKeysRemove() {
        try {
            map.keySet().remove("K");
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testKeysRemoveAll() {
        try {
            map.keySet().removeAll(Arrays.asList("k", "y", "e"));
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testKeysRetainAll() {
        try {
            map.keySet().retainAll(Arrays.asList("k", "y"));
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testKeysIteratorRemove() {
        try {
            Iterator<String> i = map.keySet().iterator();
            while (i.hasNext()) {
                i.remove();
            }
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testClearList() {
        try {
            map.get("k").clear();
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testClearKeys() {
        try {
            map.keySet().clear();
            Assert.fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }
}
