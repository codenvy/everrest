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
package org.everrest.core.impl;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: FileCollector.java -1   $
 */
public final class FileCollector
{

   private static class FileCollectorHolder
   {
      private static final FileCollector INSTANCE =
         new FileCollector(System.getProperty("java.io.tmpdir") + File.separator + "ws_jaxrs");
   }

   public static FileCollector getInstance()
   {
      return FileCollectorHolder.INSTANCE;
   }

   private final File store;

   private FileCollector(String pathname)
   {
      store = new File(pathname);
      if (!store.exists())
         store.mkdirs();
      Runtime.getRuntime().addShutdownHook(new Thread()
      {
         public void run()
         {
            clean();
         }
      });
   }

   /**
    * Clean all files in storage.
    */
   public void clean()
   {
      for (File file : store.listFiles())
         delete(file);
   }

   /**
    * Create file with specified <code>fileName</code> in storage.
    *  
    * @param fileName file name
    * @return newly created file
    * @throws IOException if any i/o error occurs
    */
   public File createFile(String fileName) throws IOException
   {
      return new File(store, fileName);
   }

   /**
    * Create new file with generated name in storage.
    *  
    * @param fileName file name
    * @return newly created file
    * @throws IOException if any i/o error occurs
    */
   public File createFile() throws IOException
   {
      return File.createTempFile("jaxrs", ".tmp", store);
   }
   
   public File getStore()
   {
      return store;
   }

   private void delete(File file)
   {
      if (file.isDirectory())
      {
         File[] children = file.listFiles();
         if (children.length > 0)
         {
            for (File ch : children)
               delete(ch);
         }
      }
      file.delete();
   }

}
