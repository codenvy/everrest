/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.impl.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CaselessMultivaluedMapTest extends BaseTest
{

   private ExtMultivaluedMap<String, String> map = new CaselessMultivaluedMap<String>();

   public void setUp() throws Exception
   {
      super.setUp();
   }

   public void testPut()
   {
      List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
      map.put("kEy1", list);
      assertEquals(1, map.size());
      assertEquals(list, map.get("key1"));
   }

   public void testAdd()
   {
      map.add("KeY1", "a");
      assertEquals(1, map.size());
      assertEquals("a", map.getFirst("key1"));
      assertEquals(1, map.get("KEY1").size());
   }

   public void testPutSingle()
   {
      map.put("kEy1", new ArrayList<String>(Arrays.asList("a", "b", "c")));
      map.putSingle("key1", "value");
      assertEquals(1, map.size());
      assertEquals("value", map.getFirst("key1"));
      assertEquals(1, map.get("KEY1").size());
   }

   public void testContainsKey()
   {
      map.put("kEy1", new ArrayList<String>(Arrays.asList("a", "b", "c")));
      map.put("KEy2", new ArrayList<String>(Arrays.asList("e", "f")));
      assertEquals(2, map.size());
      assertTrue(map.containsKey("KEY1"));
      assertTrue(map.containsKey("key2"));
   }

   public void testRemove()
   {
      map.put("kEy1", new ArrayList<String>(Arrays.asList("a", "b", "c")));
      map.put("KEy2", new ArrayList<String>(Arrays.asList("e", "f")));
      assertEquals(2, map.size());
      assertEquals(new ArrayList<String>(Arrays.asList("e", "f")), map.remove("KEY2"));
      assertEquals(1, map.size());
      assertTrue(map.containsKey("key1"));
      assertFalse(map.containsKey("kEy2"));
   }

   public void testGetList()
   {
      assertEquals(0, map.size());
      List<String> list = map.getList("key1");
      assertNotNull(list);
      assertEquals(0, list.size());
      assertEquals(1, map.size());
   }

   public void testEntrySet()
   {
      Set<Entry<String, List<String>>> entries = map.entrySet();
      map.put("k", new ArrayList<String>(Arrays.asList("a", "b")));
      map.put("e", new ArrayList<String>(Arrays.asList("c", "d")));
      map.put("Y", new ArrayList<String>(Arrays.asList("e", "f")));
      assertEquals(3, map.size());
      assertEquals(3, entries.size());
      assertTrue(entries.remove(new java.util.Map.Entry<String, List>()
      {
         public String getKey()
         {
            return "E";
         }

         public List getValue()
         {
            return Arrays.asList("c", "d");
         }

         public List setValue(List value)
         {
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

   public void testKeySet()
   {
      Set<String> keys = map.keySet();
      map.put("k", new ArrayList<String>(Arrays.asList("a", "b")));
      map.put("e", new ArrayList<String>(Arrays.asList("c", "d")));
      map.put("Y", new ArrayList<String>(Arrays.asList("e", "f")));
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
      for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();)
      {
         iterator.next();
         iterator.remove();
      }
      assertEquals(0, map.size());
      assertEquals(0, keys.size());
   }
}
