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
package org.everrest.websockets;

import org.everrest.websockets.message.MessageConversionException;
import org.everrest.websockets.message.OutputMessage;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpSession;

/**
 * Web socket connection abstraction.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface WSConnection
{
   /**
    * Get unique connection identifier.
    *
    * @return unique connection identifier
    */
   Long getId();

   /**
    * Get HTTP session associated to this connection.
    *
    * @return HTTP session associated to this connection
    */
   HttpSession getHttpSession();

   /**
    * Subscribe this connection to specified channel.
    *
    * @param channel
    *    channel name
    * @return <code>true</code> if this connection is subscribed to channel successfully and <code>false</code> if
    *         connection already subscribed to specified channel
    * @see WSConnectionContext#sendMessage(org.everrest.websockets.message.ChannelBroadcastMessage)
    */
   boolean subscribeToChannel(String channel);

   /**
    * Unsubscribe this connection from specified channel.
    *
    * @param channel
    *    channel name
    * @return <code>true</code> if this connection is unsubscribed from channel successfully and <code>false</code> if
    *         connection is not subscribed to specified channel
    * @see WSConnectionContext#sendMessage(org.everrest.websockets.message.ChannelBroadcastMessage)
    */
   boolean unsubscribeFromChannel(String channel);

   /**
    * Get optional set of channels assigned for this connection. Channel may be used for sending the same message to
    * more than one connection at one time.
    *
    * @return unmodifiable set of channel names this connection subscribed
    * @see WSConnectionContext#sendMessage(org.everrest.websockets.message.ChannelBroadcastMessage)
    */
   Collection<String> getChannels();

   /**
    * Check connection state.
    *
    * @return <code>true</code> if connection is alive and <code>false</code> if connection already closed.
    */
   boolean isConnected();

   /**
    * Get connection close status. If connection is alive or not opened yet this method return <code>0</code>.
    *
    * @return connection close status or <code>0</code> if connection is alive or not opened yet
    */
   int getCloseStatus();

   /**
    * Close this connection.
    *
    * @throws IOException
    *    if any i/o error occurs
    */
   void close() throws IOException;

   /**
    * Send message to client.
    *
    * @param output
    *    output message
    * @throws MessageConversionException
    *    if message cannot be serialized
    * @throws IOException
    *    if any i/o error occurs when try to send message to client
    */
   void sendMessage(OutputMessage output) throws MessageConversionException, IOException;

   /**
    * Register new WSMessageReceiver for this connection.
    *
    * @param messageReceiver
    *    message receiver
    */
   void registerMessageReceiver(WSMessageReceiver messageReceiver);

   /**
    * Unregister WSMessageReceiver.
    *
    * @param messageReceiver
    *    message receiver
    */
   void removeMessageReceiver(WSMessageReceiver messageReceiver);
}
