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
import org.everrest.core.impl.header.MediaTypeHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class FileEntityProviderTest extends BaseTest
{

   @SuppressWarnings({"unchecked", "rawtypes"})
   public void testRead() throws Exception
   {
      MessageBodyReader reader = providers.getMessageBodyReader(File.class, null, null, MediaTypeHelper.DEFAULT_TYPE);
      assertNotNull(reader);
      assertNotNull(providers.getMessageBodyReader(File.class, null, null, null));
      assertTrue(reader.isReadable(File.class, null, null, null));
      String data = "to be or not to be";
      File result =
         (File)reader.readFrom(File.class, null, null, null, null, new ByteArrayInputStream(data.getBytes("UTF-8")));
      assertTrue(result.exists());
      assertTrue(result.length() > 0);
      FileInputStream fdata = new FileInputStream(result);
      Reader freader = new InputStreamReader(fdata, "UTF-8");
      char[] c = new char[1024];
      int b = freader.read(c);
      String resstr = new String(c, 0, b);
      System.out.println(getClass().getName() + " : " + resstr);
      assertEquals(data, resstr);
      if (result.delete())
         System.out.println("Tmp file removed");
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public void testWrite() throws Exception
   {
      MessageBodyWriter writer = providers.getMessageBodyWriter(File.class, null, null, MediaTypeHelper.DEFAULT_TYPE);
      assertNotNull(writer);
      assertNotNull(providers.getMessageBodyWriter(File.class, null, null, null));
      assertTrue(writer.isWriteable(File.class, null, null, null));
      byte[] data = "to be or not to be".getBytes("UTF-8");
      File source = File.createTempFile("fileentitytest", null);
      FileOutputStream fout = new FileOutputStream(source);
      fout.write(data);
      fout.close();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writer.writeTo(source, File.class, null, null, null, null, out);
      // compare as bytes
      assertTrue(Arrays.equals(data, out.toByteArray()));
      if (source.delete())
         System.out.println("Tmp file removed");
   }

}
