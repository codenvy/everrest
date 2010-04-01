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
package org.everrest.core.impl.header;

import junit.framework.TestCase;

import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.MultivaluedMapImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class InputHeadersMapTest extends TestCase
{

   public void testCaseInsensitive()
   {
      InputHeadersMap um;
      try
      {
         um = new InputHeadersMap(null);
         fail("NullPointerException should be here");
      }
      catch (NullPointerException e)
      {
      }
      MultivaluedMap<String, String> m = new MultivaluedMapImpl();
      m.add("hello", "world");
      m.add("hello", "foo");
      m.add("hello", "bar");
      m.add("bar", "bar");
      m.add("bar", "foo");
      m.add("bar", "hello");

      um = new InputHeadersMap(m);
      assertEquals("world", um.getFirst("hello"));
      assertEquals("foo", um.get("heLLo").get(1));
      assertEquals("foo", um.get("bar").get(1));
      assertEquals("hello", um.get("BAR").get(2));

   }

   public void testUnsupportedOperation()
   {
      MultivaluedMap<String, String> m = new MultivaluedMapImpl();
      m.add("hello", "world");
      InputHeadersMap um = new InputHeadersMap(m);

      try
      {
         um.clear();
         fail("UnsupportedOperationException should be thrown fro 'clear'");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.remove("hello");
         fail("UnsupportedOperationException should be thrown for 'remove'");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.put("bar", new ArrayList<String>());
         fail("UnsupportedOperationException should be thrown for 'put'");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.putAll(new MultivaluedMapImpl());
         fail("UnsupportedOperationException should be thrown for 'puAll'");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.putSingle("foo", "bar");
         fail("UnsupportedOperationException should be thrown for 'putSingle'");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.add("foo", "bar");
         fail("UnsupportedOperationException should be thrown for 'add'");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.entrySet().remove(null);
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.entrySet().removeAll(null);
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.entrySet().retainAll(null);
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         Iterator<Map.Entry<String, List<String>>> i = um.entrySet().iterator();
         while (i.hasNext())
            i.remove();
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.entrySet().iterator().next().setValue(null);
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.keySet().remove("hello");
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.keySet().removeAll(null);
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.keySet().retainAll(null);
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         Iterator<String> i = um.keySet().iterator();
         while (i.hasNext())
            i.remove();
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.get("hello").clear();
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.values().clear();
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

      try
      {
         um.keySet().clear();
         fail("UnsupportedOperationException should be thrown");
      }
      catch (UnsupportedOperationException e)
      {
      }

   }

}
