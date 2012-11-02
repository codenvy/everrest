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

import java.util.Arrays;

/**
 * Output message.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class OutputMessage extends Message
{
   private int status;

   public OutputMessage()
   {
   }

   /**
    * Get response status.
    *
    * @return response status.
    */
   public int getStatus()
   {
      return status;
   }

   /**
    * Get response status.
    *
    * @param status
    *    response status.
    */
   public void setStatus(int status)
   {
      this.status = status;
   }

   @Override
   public String toString()
   {
      return "OutputMessage{" +
         "status=" + status +
         ", uuid='" + getUuid() + '\'' +
         ", channel='" + getChannel() + '\'' +
         ", method='" + getMethod() + '\'' +
         ", path='" + getPath() + '\'' +
         ", headers=" + (getHeaders() == null ? null : Arrays.asList(getHeaders())) +
         ", body='" + getBody() + '\'' +
         ", base64Encoded=" + isBodyEncodedBase64() +
         '}';
   }
}
