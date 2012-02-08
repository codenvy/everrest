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
import java.security.SecureRandom;

/**
 * Provides store for temporary files.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class FileCollector
{
   private static final String PREF = "everrest";
   private static final String SUFF = ".tmp";

   private static class FileCollectorHolder
   {
      private static final String name = PREF + Long.toString(Math.abs(new SecureRandom().nextLong()));
      private static final FileCollector collector = new FileCollector( //
         new File(System.getProperty("java.io.tmpdir"), name));
   }

   public static FileCollector getInstance()
   {
      return FileCollectorHolder.collector;
   }

   private final File store;
   private final Thread cleaner = new Thread()
   {
      public void run()
      {
         clean();
      }
   };

   private FileCollector(File store)
   {
      this.store = store;
      try
      {
         Runtime.getRuntime().addShutdownHook(cleaner);
      }
      catch (IllegalStateException ignored)
      {
      }
   }

   /** Clean all files in storage. */
   public void clean()
   {
      if (store.exists())
      {
         delete(store);
      }
   }

   public void stop()
   {
      clean();
      try
      {
         Runtime.getRuntime().removeShutdownHook(cleaner);
      }
      catch (IllegalStateException ignored)
      {
      }
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
      checkStore();
      return new File(store, fileName);
   }

   /**
    * Create new file with generated name in storage.
    *
    * @return newly created file
    * @throws IOException if any i/o error occurs
    */
   public File createFile() throws IOException
   {
      checkStore();
      return File.createTempFile(PREF, SUFF, store);
   }

   public File getStore()
   {
      checkStore();
      return store;
   }

   private void checkStore()
   {
      if (!store.exists())
      {
         store.mkdirs();
      }
   }

   private void delete(File fileOrDirectory)
   {
      if (fileOrDirectory.isDirectory())
      {
         File[] children = fileOrDirectory.listFiles();
         if (children.length > 0)
         {
            for (File ch : children)
            {
               delete(ch);
            }
         }
      }
      fileOrDirectory.delete();
   }
}
