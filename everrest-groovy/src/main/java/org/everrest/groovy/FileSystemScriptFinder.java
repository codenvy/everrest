/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.groovy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Scanner of Groovy scripts on local file system.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class FileSystemScriptFinder implements ScriptFinder
{
   /**
    * {@inheritDoc}
    */
   public URL[] find(URLFilter filter, URL root) throws MalformedURLException
   {
      // Be sure protocol is supported.
      if ("file".equals(root.getProtocol()))
      {
         File file = new File(URI.create(root.toString()));
         if (file.isDirectory())
         {
            return find(file, filter);
         }
      }
      return new URL[0];
   }

   private URL[] find(File directory, URLFilter filter) throws MalformedURLException
   {
      List<URL> files = new ArrayList<URL>();
      LinkedList<File> q = new LinkedList<File>();
      q.add(directory);
      while (!q.isEmpty())
      {
         File current = q.pop();
         File[] list = current.listFiles();
         if (list != null)
         {
            for (int i = 0; i < list.length; i++)
            {
               final File f = list[i];
               if (f.isDirectory())
               {
                  q.push(f);
               }
               else
               {
                  URL url = f.toURI().toURL();
                  if (filter.accept(url))
                  {
                     files.add(url);
                  }
               }
            }
         }
      }
      return files.toArray(new URL[files.size()]);
   }
}
