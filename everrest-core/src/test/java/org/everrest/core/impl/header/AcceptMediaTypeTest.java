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

import org.everrest.core.impl.BaseTest;

import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AcceptMediaTypeTest extends BaseTest
{

   public void testValueOf()
   {
      String mt = "text/xml;charset=utf8;q=0.825";
      AcceptMediaType acceptedMediaType = AcceptMediaType.valueOf(mt);
      assertEquals("text", acceptedMediaType.getType());
      assertEquals("xml", acceptedMediaType.getSubtype());
      assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
      assertEquals(0.825F, acceptedMediaType.getQvalue());

      mt = "text/xml;charset=utf8";
      acceptedMediaType = AcceptMediaType.valueOf(mt);
      assertEquals("text", acceptedMediaType.getType());
      assertEquals("xml", acceptedMediaType.getSubtype());
      assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
      assertEquals(1.0F, acceptedMediaType.getQvalue());
   }

   public void testFromString()
   {
      String mt = "text/xml;charset=utf8;q=0.825";
      AcceptMediaTypeHeaderDelegate hd = new AcceptMediaTypeHeaderDelegate();

      AcceptMediaType acceptedMediaType = hd.fromString(mt);
      assertEquals("text", acceptedMediaType.getType());
      assertEquals("xml", acceptedMediaType.getSubtype());
      assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
      assertEquals(0.825F, acceptedMediaType.getQvalue());

      mt = "text/xml;charset=utf8";
      acceptedMediaType = hd.fromString(mt);
      assertEquals("text", acceptedMediaType.getType());
      assertEquals("xml", acceptedMediaType.getSubtype());
      assertEquals("utf8", acceptedMediaType.getParameters().get("charset"));
      assertEquals(1.0F, acceptedMediaType.getQvalue());
   }

   public void testListProducer()
   {
      List<AcceptMediaType> l = HeaderHelper.createAcceptedMediaTypeList(null);
      assertEquals(1, l.size());
      assertEquals(l.get(0).getType(), "*");
      assertEquals(l.get(0).getSubtype(), "*");
      assertEquals(l.get(0).getQvalue(), 1.0F);

      l = HeaderHelper.createAcceptedMediaTypeList("");
      assertEquals(1, l.size());
      assertEquals(l.get(0).getType(), "*");
      assertEquals(l.get(0).getSubtype(), "*");
      assertEquals(l.get(0).getQvalue(), 1.0F);

      String mt = "text/xml;  charset=utf8;q=0.825,    text/html;charset=utf8,  text/plain;charset=utf8;q=0.8";
      l = HeaderHelper.createAcceptedMediaTypeList(mt);
      assertEquals(3, l.size());

      assertEquals(l.get(0).getType(), "text");
      assertEquals(l.get(0).getSubtype(), "html");
      assertEquals(l.get(0).getQvalue(), 1.0F);

      assertEquals(l.get(1).getType(), "text");
      assertEquals(l.get(1).getSubtype(), "xml");
      assertEquals(l.get(1).getQvalue(), 0.825F);

      assertEquals(l.get(2).getType(), "text");
      assertEquals(l.get(2).getSubtype(), "plain");
      assertEquals(l.get(2).getQvalue(), 0.8F);
   }

}
