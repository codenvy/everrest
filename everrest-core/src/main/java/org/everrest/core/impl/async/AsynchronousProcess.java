/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.everrest.core.impl.async;

/**
 * Description of AsynchronousJob. It may be serialized to JSON or plain text format to make possible for client to see
 * what asynchronous jobs in progress.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class AsynchronousProcess
{
   private final String owner;
   private final String id;
   private final String path;
   private final String status;

   public AsynchronousProcess(String owner, String id, String path, String status)
   {
      this.owner = owner;
      this.id = id;
      this.path = path;
      this.status = status;
   }

   public String getOwner()
   {
      return owner;
   }

   public String getId()
   {
      return id;
   }

   public String getPath()
   {
      return path;
   }

   public String getStatus()
   {
      return status;
   }
}
