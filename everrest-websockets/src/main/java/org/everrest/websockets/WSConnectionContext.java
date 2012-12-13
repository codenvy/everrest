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

import org.everrest.core.util.Logger;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.everrest.websockets.message.MessageConversionException;
import org.everrest.websockets.message.MessageConverter;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RESTfulOutputMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WSConnectionContext
{
   private static final Logger LOG = Logger.getLogger(WSConnectionContext.class);
   static final List<WSConnectionListener> connectionListeners = new CopyOnWriteArrayList<WSConnectionListener>();
   static final Map<Long, WSConnectionImpl> connections = new ConcurrentHashMap<Long, WSConnectionImpl>();

   public static boolean registerConnectionListener(WSConnectionListener listener)
   {
      return connectionListeners.add(listener);
   }

   public static boolean removeConnectionListener(WSConnectionListener listener)
   {
      return connectionListeners.remove(listener);
   }

   /**
    * Send message to all connections subscribed to the channel. Method tries to send message to as many connections as
    * possible. Even if method fails to send message to the first connection it will try to send message to other
    * connections, if any. After that a first occurred error is rethrown.
    *
    * @param message
    *    message
    * @throws MessageConversionException
    *    if message cannot be serialized
    * @throws IOException
    *    if any i/o error occurs when try to send message to client
    * @see org.everrest.websockets.message.ChannelBroadcastMessage#getChannel()
    */
   public static void sendMessage(ChannelBroadcastMessage message) throws MessageConversionException, IOException
   {
      final String channel = message.getChannel();
      final RESTfulOutputMessage output = newRESTfulOutputMessage(message);
      Exception error = null;
      for (WSConnectionImpl connection : connections.values())
      {
         if (connection.getChannels().contains(channel))
         {
            try
            {
               connection.sendMessage(output);
            }
            catch (MessageConversionException e)
            {
               if (error == null)
               {
                  error = e;
               }
            }
            catch (IOException e)
            {
               if (error == null)
               {
                  error = e;
               }
            }
         }
      }
      if (error instanceof MessageConversionException)
      {
         throw (MessageConversionException)error;
      }
      else if (error != null)
      {
         // If error is not null then may be IOException only.
         throw (IOException)error;
      }
   }

   private static RESTfulOutputMessage newRESTfulOutputMessage(ChannelBroadcastMessage message)
   {
      RESTfulOutputMessage transport = new RESTfulOutputMessage();
      transport.setUuid(message.getUuid());
      transport.setHeaders(new Pair[]{new Pair("x-everrest-websocket-channel", message.getChannel()),
         new Pair("x-everrest-websocket-message-type", message.getType().toString())});
      transport.setBody(message.getBody());
      return transport;
   }

   static
   {
      registerConnectionListener(new WSConnectionListener()
      {
         @Override
         public void onOpen(WSConnection connection)
         {
            LOG.debug("Open connection {} ", connection);
         }

         @Override
         public void onClose(WSConnection connection)
         {
            LOG.debug("Close connection {} with status {} ", connection, connection.getCloseStatus());
            connections.remove(connection.getConnectionId());
         }
      });
   }

   // --- Internal ---

   /**
    * Create new web socket connection. This method designed for internal usage only. Third part code should never use
    * it directly. After creation connection may not be ready to use yet. Always need register WSConnectionListener and
    * use connection passed in method {@link WSConnectionListener#onOpen(WSConnection)}.
    *
    * @param httpSession
    *    HTTP session associated to this connection
    * @param messageConverter
    *    converts input messages from raw message represented by String to InputMessage and converts back OutputMessage
    *    to String
    * @see #registerConnectionListener(WSConnectionListener)
    * @see #removeConnectionListener(WSConnectionListener)
    */
   static WSConnectionImpl open(HttpSession httpSession, MessageConverter messageConverter)
   {
      if (httpSession == null)
      {
         throw new IllegalArgumentException("HTTP Session required. ");
      }
      if (messageConverter == null)
      {
         throw new IllegalArgumentException("MessageConverter required. ");
      }
      WSConnectionImpl newConnection = new WSConnectionImpl(httpSession, messageConverter);
      connections.put(newConnection.getConnectionId(), newConnection);
      return newConnection;
   }

   /**
    * Get all WSConnection associated with specified HTTP session.
    *
    * @param httpSessionId
    *    HTTP session Id
    * @return all WSConnection associated with specified HTTP session
    */
   static Collection<WSConnectionImpl> getAll(String httpSessionId)
   {
      if (httpSessionId == null)
      {
         throw new IllegalArgumentException("HTTP session may not be null. ");
      }
      List<WSConnectionImpl> result = new ArrayList<WSConnectionImpl>();
      for (WSConnectionImpl connection : connections.values())
      {
         if (httpSessionId.equals(connection.getHttpSession().getId()))
         {
            result.add(connection);
         }
      }
      return result;
   }

   /**
    * Close all connections associated with specified HTTP session Id.
    *
    * @param httpSessionId
    *    HTTP session Id
    */
   static void closeAll(String httpSessionId)
   {
      Collection<WSConnectionImpl> toRemove = getAll(httpSessionId);
      for (WSConnectionImpl connection : toRemove)
      {
         try
         {
            connection.close();
         }
         catch (IOException e)
         {
            LOG.error(e.getMessage(), e);
         }
      }
      connections.values().removeAll(toRemove);
   }

   private WSConnectionContext()
   {
   }
}
