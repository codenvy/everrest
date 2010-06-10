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
package org.everrest.core.util;

import org.everrest.core.impl.BaseTest;

import java.util.Iterator;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MediaTypeMapTest extends BaseTest
{

   public void testSort()
   {
      MediaTypeMap<Object> m = new MediaTypeMap<Object>();
      Object o1 = new Object();
      Object o2 = new Object();
      Object o3 = new Object();
      Object o4 = new Object();
      Object o5 = new Object();
      int h1 = o1.hashCode();
      int h2 = o2.hashCode();
      int h3 = o3.hashCode();
      int h4 = o4.hashCode();
      int h5 = o5.hashCode();
      m.put(new MediaType(), o1);
      m.put(new MediaType("text", "*"), o2);
      m.put(new MediaType("text", "plain"), o3);
      m.put(new MediaType("text", "xml"), o4);
      m.put(new MediaType("application", "*"), o5);
      Iterator<Object> values = m.values().iterator();
      assertEquals(h3, values.next().hashCode());
      assertEquals(h4, values.next().hashCode());
      assertEquals(h5, values.next().hashCode());
      assertEquals(h2, values.next().hashCode());
      assertEquals(h1, values.next().hashCode());
   }

}
