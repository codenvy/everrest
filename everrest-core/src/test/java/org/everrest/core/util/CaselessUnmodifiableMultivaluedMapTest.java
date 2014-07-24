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
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CaselessUnmodifiableMultivaluedMapTest extends BaseTest {
    private ExtMultivaluedMap<String, String> map;

    public void setUp() throws Exception {
        super.setUp();
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("K", "a");
        m.add("K", "b");
        m.add("e", "c");
        m.add("y", "d");
        m.add("y", "e");
        map = new CaselessUnmodifiableMultivaluedMap<String>(m);
    }

    public void testGet() {
        assertEquals("a", map.getFirst("k"));
        assertEquals(Arrays.asList("a", "b"), map.get("k"));
        assertEquals("c", map.getFirst("E"));
        assertEquals(Arrays.asList("d", "e"), map.get("Y"));
    }

    public void testGetList() {
        assertEquals(Arrays.asList("a", "b"), map.getList("k"));
        List<String> list = map.getList("x");
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    public void tesClear() {
        try {
            map.clear();
            fail("UnsupportedOperationException should be thrown fro 'clear'");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testRemove() {
        try {
            map.remove("k");
            fail("UnsupportedOperationException should be thrown for 'remove'");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testPut() {
        try {
            map.put("k", new ArrayList<String>());
            fail("UnsupportedOperationException should be thrown for 'put'");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testPutAll() {
        try {
            map.putAll(new MultivaluedMapImpl());
            fail("UnsupportedOperationException should be thrown for 'puAll'");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testPutSingle() {
        try {
            map.putSingle("k", "value");
            fail("UnsupportedOperationException should be thrown for 'putSingle'");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testAdd() {
        try {
            map.add("k", "value");
            fail("UnsupportedOperationException should be thrown for 'add'");
        } catch (UnsupportedOperationException e) {
        }
    }

    @SuppressWarnings("rawtypes")
    public void testEntryRemove() {
        try {
            map.entrySet().remove(new java.util.Map.Entry<String, List>() {
                public String getKey() {
                    return "K";
                }

                public List getValue() {
                    return Arrays.asList("a", "b");
                }

                public List setValue(List value) {
                    return Arrays.asList("a", "b");
                }
            });
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @SuppressWarnings("rawtypes")
    public void testEntryRemoveAll() {
        try {
            map.entrySet().removeAll(new ArrayList());
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    @SuppressWarnings("rawtypes")
    public void testEntryRetainAll() {
        try {
            map.entrySet().retainAll(new ArrayList());
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testEntryIteratorRemove() {
        try {
            Iterator<Map.Entry<String, List<String>>> i = map.entrySet().iterator();
            while (i.hasNext())
                i.remove();
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testEntryUpdateValue() {
        try {
            map.entrySet().iterator().next().setValue(new ArrayList<String>());
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testKeysRemove() {
        try {
            map.keySet().remove("K");
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testKeysRemoveAll() {
        try {
            map.keySet().removeAll(Arrays.asList("k", "y", "e"));
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testKeysRetainAll() {
        try {
            map.keySet().retainAll(Arrays.asList("k", "y"));
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testKeysIteratorRemove() {
        try {
            Iterator<String> i = map.keySet().iterator();
            while (i.hasNext())
                i.remove();
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testClearList() {
        try {
            map.get("k").clear();
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testClearKeys() {
        try {
            map.keySet().clear();
            fail("UnsupportedOperationException should be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }

}
