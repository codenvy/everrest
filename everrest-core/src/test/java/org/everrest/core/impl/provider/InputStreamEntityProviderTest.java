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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class InputStreamEntityProviderTest extends BaseTest
{

   @SuppressWarnings("unchecked")
   public void testRead() throws Exception
   {
      MessageBodyReader reader = providers.getMessageBodyReader(InputStream.class, null, null, null);
      assertNotNull(reader);
      assertTrue(reader.isReadable(InputStream.class, null, null, null));
      byte[] data = new byte[16];
      for (int i = 0; i < data.length; i++)
         data[i] = (byte)i;
      InputStream in = new ByteArrayInputStream(data);
      InputStream result = (InputStream)reader.readFrom(InputStream.class, null, null, null, null, in);
      byte[] data2 = new byte[16];
      result.read(data2);
      assertTrue(Arrays.equals(data, data2));
   }

   @SuppressWarnings("unchecked")
   public void testWrite() throws Exception
   {
      MessageBodyWriter writer = providers.getMessageBodyWriter(InputStream.class, null, null, null);
      assertNotNull(writer);
      assertTrue(writer.isWriteable(InputStream.class, null, null, null));
      byte[] data = new byte[16];
      for (int i = data.length - 1; i >= 0; i--)
         data[i] = (byte)i;
      InputStream source = new ByteArrayInputStream(data);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writer.writeTo(source, InputStream.class, null, null, null, null, out);
      assertTrue(Arrays.equals(data, out.toByteArray()));
   }

}
