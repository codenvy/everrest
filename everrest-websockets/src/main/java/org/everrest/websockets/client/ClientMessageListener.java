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
package org.everrest.websockets.client;

/**
 * Implementation of this interface passed to WSClient. After open connection to remote server listener notified about
 * incoming messages from server.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface ClientMessageListener
{
   /**
    * Notify listener about test message.
    *
    * @param data
    *    text message
    */
   void onMessage(String data);

   /**
    * Notify listener about binary message.
    *
    * @param data
    *    binary message
    */
   void onMessage(byte[] data);

   /**
    * Notify listener about connection open and WSClient is ready to use.
    *
    * @param client
    *    websocket client
    */
   void onOpen(WSClient client);

   /**
    * Notify listener that connection closed.
    *
    * @param status
    *    connection closed status or <code>0</code> if unknown
    * @param message
    *    optional message MAY be passed if connection closed abnormally
    */
   void onClose(int status, String message);
}
