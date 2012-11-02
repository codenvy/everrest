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
 * Input message.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class InputMessage extends Message
{
   private boolean encodeResponseBodyBase64;

   public InputMessage()
   {
   }

   /**
    * Indicate should response body for this message to be base64 encoded or not.
    *
    * @return <code>true</code> if response body should be encoded and <code>false</code> otherwise
    */
   public boolean isEncodeResponseBodyBase64()
   {
      return encodeResponseBodyBase64;
   }

   /**
    * Indicate should response body for this message to be base64 encoded or not.
    *
    * @param encodeResponseBodyBase64
    *    <code>true</code> if response body should be encoded and <code>false</code> otherwise
    */
   public void setEncodeResponseBodyBase64(boolean encodeResponseBodyBase64)
   {
      this.encodeResponseBodyBase64 = encodeResponseBodyBase64;
   }

   @Override
   public String toString()
   {
      return "InputMessage{" +
         "uuid='" + getUuid() + '\'' +
         ", channel='" + getChannel() + '\'' +
         ", method='" + getMethod() + '\'' +
         ", path='" + getPath() + '\'' +
         ", headers=" + (getHeaders() == null ? null : Arrays.asList(getHeaders())) +
         ", body='" + getBody() + '\'' +
         ", bodyBase64Encoded=" + isBodyEncodedBase64() +
         ", encodeResponseBodyBase64=" + encodeResponseBodyBase64 +
         '}';
   }
}
