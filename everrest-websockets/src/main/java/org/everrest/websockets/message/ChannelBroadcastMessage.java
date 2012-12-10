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
 * This class intended for sending broadcast messages for all clients subscribed to the channel.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * @see org.everrest.websockets.WSConnection#subscribeToChannel(String)
 * @see org.everrest.websockets.WSConnection#unsubscribeFromChannel(String)
 * @see org.everrest.websockets.WSConnection#getChannels()
 * @see org.everrest.websockets.WSConnectionContext#sendMessage(ChannelBroadcastMessage)
 */
public class ChannelBroadcastMessage extends Message
{
   public enum Type
   {
      ERROR("error"),
      NONE("none");

      private final String value;

      private Type(String value)
      {
         this.value = value;
      }

      @Override
      public final String toString()
      {
         return value;
      }
   }

   private String channel;
   private Type type = Type.NONE;

   /**
    * Get name of channel to send request.
    *
    * @return name of channel to send request.
    */
   public String getChannel()
   {
      return channel;
   }

   /**
    * Set name of channel to send request.
    *
    * @param channel
    *    name of channel to send request.
    */
   public void setChannel(String channel)
   {
      this.channel = channel;
   }

   /**
    * Get message type. All messages by default have type 'none' if different type is not set by method {@link
    * #setType(org.everrest.websockets.message.ChannelBroadcastMessage.Type)}.
    *
    * @return message type
    */
   public Type getType()
   {
      return type;
   }

   /**
    * Set message type.
    *
    * @param type
    *    message type
    * @see #getType()
    */
   public void setType(Type type)
   {
      this.type = type;
   }
}
