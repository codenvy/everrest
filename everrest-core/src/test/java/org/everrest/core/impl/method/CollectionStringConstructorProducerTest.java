/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.method;

import junit.framework.TestCase;

import org.everrest.core.impl.MultivaluedMapImpl;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CollectionStringConstructorProducerTest extends TestCase
{

   @SuppressWarnings("unchecked")
   public void testList() throws Exception
   {
      CollectionStringConstructorProducer collectionStringConstructorProducer =
         new CollectionStringConstructorProducer(List.class, Integer.class.getConstructor(String.class));
      MultivaluedMap<String, String> multivaluedMap = new MultivaluedMapImpl();
      multivaluedMap.putSingle("number", "2147483647");
      List l1 = (List)collectionStringConstructorProducer.createValue("number", multivaluedMap, null);
      assertEquals(1, l1.size());
      assertEquals(2147483647, l1.get(0));
      // test with default value
      List l2 = (List)collectionStringConstructorProducer.createValue("_number_", multivaluedMap, "-2147483647");
      assertEquals(1, l2.size());
      assertEquals(-2147483647, l2.get(0));
   }

   @SuppressWarnings("unchecked")
   public void testSet() throws Exception
   {
      CollectionStringConstructorProducer collectionStringConstructorProducer =
         new CollectionStringConstructorProducer(Set.class, Integer.class.getConstructor(String.class));
      MultivaluedMap<String, String> multivaluedMap = new MultivaluedMapImpl();
      multivaluedMap.putSingle("number", "2147483647");
      Set s1 = (Set)collectionStringConstructorProducer.createValue("number", multivaluedMap, null);
      assertEquals(1, s1.size());
      assertEquals(2147483647, s1.iterator().next());
      // test with default value
      Set s2 = (Set)collectionStringConstructorProducer.createValue("_number_", multivaluedMap, "-2147483647");
      assertEquals(1, s2.size());
      assertEquals(-2147483647, s2.iterator().next());
   }

   @SuppressWarnings("unchecked")
   public void testSortedSet() throws Exception
   {
      CollectionStringConstructorProducer collectionStringConstructorProducer =
         new CollectionStringConstructorProducer(SortedSet.class, Integer.class.getConstructor(String.class));
      MultivaluedMap<String, String> multivaluedMap = new MultivaluedMapImpl();
      multivaluedMap.putSingle("number", "2147483647");
      SortedSet ss1 =
         (SortedSet)collectionStringConstructorProducer.createValue("number", multivaluedMap, null);
      assertEquals(1, ss1.size());
      assertEquals(2147483647, ss1.iterator().next());
      // test with default value
      SortedSet ss2 =
         (SortedSet)collectionStringConstructorProducer.createValue("_number_", multivaluedMap, "-2147483647");
      assertEquals(1, ss2.size());
      assertEquals(-2147483647, ss2.iterator().next());
   }
}
