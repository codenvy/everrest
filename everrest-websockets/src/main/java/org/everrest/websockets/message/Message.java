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
 * Base class for input and output message.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class Message
{
   private String uuid;
   private String channel;
   private String method;
   private String path;
   private Pair[] headers;
   private String body;
   private boolean bodyEncodedBase64;

   public Message()
   {
   }

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
    * Get optional name of channel. Name of chanel may be specified when new web socket opened. If this parameter
    * specified for input message then output messaged is propagated to connections with the same channel name only.
    *
    * @return name of channel
    */
   public String getChannel()
   {
      return channel;
   }

   /**
    * Set optional name of channel. Name of chanel may be specified when new web socket opened. If this parameter
    * specified for input message then output messaged is propagated to connections with the same channel name only.
    *
    * @param channel
    *    name of channel
    */
   public void setChannel(String channel)
   {
      this.channel = channel;
   }

   /**
    * Get name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
    *
    * @return name of HTTP method
    */
   public String getMethod()
   {
      return method;
   }

   /**
    * Set name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
    *
    * @param method
    *    name of HTTP method
    */
   public void setMethod(String method)
   {
      this.method = method;
   }

   /**
    * Get resource path.
    *
    * @return resource path
    */
   public String getPath()
   {
      return path;
   }

   /**
    * Set resource path.
    *
    * @param path
    *    resource path
    */
   public void setPath(String path)
   {
      this.path = path;
   }

   /**
    * Get HTTP headers.
    *
    * @return HTTP headers
    */
   public Pair[] getHeaders()
   {
      return headers;
   }

   /**
    * Set HTTP headers.
    *
    * @param headers
    *    HTTP headers
    */
   public void setHeaders(Pair[] headers)
   {
      this.headers = headers;
   }

   /**
    * Get message body. Body may be base64 encoded. If body encoded parameter <code>bodyEncodedBase64</code> must be
    * set to <code>true</code>.
    *
    * @return message body
    * @see #setBodyEncodedBase64(boolean)
    * @see #isBodyEncodedBase64()
    */
   public String getBody()
   {
      return body;
   }

   /**
    * Set message body. Body may be base64 encoded. If body encoded parameter <code>bodyEncodedBase64</code> must be
    * set to <code>true</code>.
    *
    * @param body
    *    message body
    * @see #setBodyEncodedBase64(boolean)
    * @see #isBodyEncodedBase64()
    */
   public void setBody(String body)
   {
      this.body = body;
   }

   /**
    * Indicate message body is base64 encoded.
    *
    * @return <code>true</code> if message body base64 encoded and <code>false</code> otherwise
    */
   public boolean isBodyEncodedBase64()
   {
      return bodyEncodedBase64;
   }

   /**
    * Indicate message body is base64 encoded.
    *
    * @param bodyEncodedBase64
    *    <code>true</code> if message body base64 encoded and <code>false</code> otherwise
    */
   public void setBodyEncodedBase64(boolean bodyEncodedBase64)
   {
      this.bodyEncodedBase64 = bodyEncodedBase64;
   }
}
