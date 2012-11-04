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
package org.everrest.websockets.message;

/**
 * Base class for input and output messages.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class Message
{
   private String uuid;
   private String body;

   /**
    * Get message UUID. If specified for input message then output message gets the same UUID.
    *
    * @return message unique identifier.
    */
   public String getUuid()
   {
      return uuid;
   }

   /**
    * Set message UUID. If specified fro input message then output message gets the same UUID.
    *
    * @param uuid
    *    message unique identifier.
    */
   public void setUuid(String uuid)
   {
      this.uuid = uuid;
   }

   /**
    * Get message body.
    *
    * @return message body
    */
   public String getBody()
   {
      return body;
   }

   /**
    * Set message body.
    *
    * @param body
    *    message body
    */
   public void setBody(String body)
   {
      this.body = body;
   }
}
