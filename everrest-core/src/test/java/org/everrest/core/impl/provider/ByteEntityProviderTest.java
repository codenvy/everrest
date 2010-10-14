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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.MediaTypeHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ByteEntityProviderTest extends BaseTest
{

   @SuppressWarnings("unchecked")
   public void testRead() throws Exception
   {
      MessageBodyReader reader = providers.getMessageBodyReader(byte[].class, null, null, MediaTypeHelper.DEFAULT_TYPE);
      assertNotNull(reader);
      // without media type
      assertNotNull(providers.getMessageBodyReader(byte[].class, null, null, null));
      assertTrue(reader.isReadable(byte[].class, null, null, null));
      byte[] data = new byte[16];
      for (int i = 0; i < data.length; i++)
         data[i] = (byte)i;
      assertTrue(reader.isReadable(data.getClass(), null, null, null));
      byte[] result =
         (byte[])reader.readFrom(byte[].class, null, null, MediaTypeHelper.DEFAULT_TYPE, new MultivaluedMapImpl(),
            new ByteArrayInputStream(data));
      assertTrue(Arrays.equals(data, result));
   }

   @SuppressWarnings("unchecked")
   public void testWrite() throws Exception
   {
      MessageBodyWriter writer = providers.getMessageBodyWriter(byte[].class, null, null, MediaTypeHelper.DEFAULT_TYPE);
      assertNotNull(writer);
      // without media type
      assertNotNull(providers.getMessageBodyWriter(byte[].class, null, null, null));
      assertTrue(writer.isWriteable(byte[].class, null, null, null));
      byte[] data = new byte[16];
      for (int i = data.length - 1; i >= 0; i--)
         data[i] = (byte)i;
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writer.writeTo(data, byte[].class, null, null, MediaTypeHelper.DEFAULT_TYPE, new MultivaluedMapImpl(), out);
      assertTrue(Arrays.equals(data, out.toByteArray()));
   }

}
