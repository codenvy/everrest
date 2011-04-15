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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Scanner of Groovy scripts on local file system.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class FileSystemScriptFinder implements ScriptFinder
{

   private static final String PROTOCOL = "file";

   /**
    * {@inheritDoc}
    */
   public Set<URL> find(URLFilter filter, URL root) throws MalformedURLException
   {
      Set<URL> result = new LinkedHashSet<URL>();
      // Be sure protocol is supported.
      if (PROTOCOL.equals(root.getProtocol()))
      {
         try
         {
            File file = new File(root.toURI());
            find(file, filter, result);
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException(e.getMessage());
         }
      }
      return result;
   }

   private void find(File f, URLFilter filter, Set<URL> result) throws MalformedURLException
   {
      for (File s : f.listFiles())
      {
         if (!s.isFile())
         {
            find(s, filter, result);
         }
         else
         {
            URL url = s.toURI().toURL();
            if (filter.accept(url))
            {
               result.add(url);
            }
         }
      }
   }

}
