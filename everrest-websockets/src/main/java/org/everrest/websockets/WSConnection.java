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
   Long getConnectionId();

   /**
    * Get id of HTTP session associated to this connection.
    *
    * @return id of HTTP session associated to this connection
    */
   String getHttpSessionId();

   /**
    * Get optional name of this connection
    *
    * @return optional name of this connection
    */
   String getChannel();

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
    *    if any i/o/ error occurs when try to send message to client
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
