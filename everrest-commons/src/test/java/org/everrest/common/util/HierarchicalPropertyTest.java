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
package org.everrest.common.util;

import org.everrest.common.util.HierarchicalProperty;

import junit.framework.TestCase;

import javax.xml.namespace.QName;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class HierarchicalPropertyTest extends TestCase
{

   protected void setUp() throws Exception
   {
   }

   public void testQNameEquality() throws Exception
   {
      QName name1 = new QName("namespace", "localpart", "prefix");
      QName name2 = new QName("namespace", "localpart");
      assertTrue(name1.equals(name2));
   }

   public void testPropertyFromQName() throws Exception
   {

      QName name = new QName("namespace", "localpart", "prefix");
      HierarchicalProperty hp = new HierarchicalProperty(name, "value");
      assertEquals(name, hp.getName());
      assertEquals("prefix:localpart", hp.getStringName());
      assertEquals("value", hp.getValue());

      assertTrue(hp.getAttributes().size() == 0);
      assertTrue(hp.getChildren().size() == 0);
   }

   public void testPropertyFromString() throws Exception
   {

      QName name = new QName("namespace", "localpart", "prefix");
      HierarchicalProperty hp = new HierarchicalProperty("prefix:localpart", "value", "namespace");
      assertEquals(name, hp.getName());
      assertEquals("prefix:localpart", hp.getStringName());
      assertEquals("value", hp.getValue());

      assertTrue(hp.getAttributes().size() == 0);
      assertTrue(hp.getChildren().size() == 0);
   }
}
