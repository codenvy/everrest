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
import org.everrest.websockets.message.MessageConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
         public void onClose(Long connectionId, int code)
         {
            LOG.debug("Close connection {} with code {} ", connectionId, code);
            connections.remove(connectionId);
         }
      });
   }

   // --- Internal ---

   /**
    * Create new web socket connection. This method designed for internal usage only. Third part code should never use
    * it directly. After creation connection may not be ready to use yet. Always need register WSConnectionListener and
    * use connection passed in method {@link WSConnectionListener#onOpen(WSConnection)}.
    *
    * @param httpSessionId
    *    id of HTTP session associated to this connection. If few connections open for the same HTTP session all of
    *    them receive response for request sent to one of connections
    * @param channel
    *    optional name on channel
    * @param messageConverter
    *    converts input messages from raw message represented by String to InputMessage and converts back OutputMessage
    *    to String
    * @see #registerConnectionListener(WSConnectionListener)
    * @see #removeConnectionListener(WSConnectionListener)
    */
   static WSConnectionImpl open(String httpSessionId, String channel, MessageConverter messageConverter)
   {
      if (httpSessionId == null)
      {
         throw new IllegalArgumentException("HTTP Session Id required. ");
      }
      if (messageConverter == null)
      {
         throw new IllegalArgumentException("MessageConverter required. ");
      }
      WSConnectionImpl newConnection = new WSConnectionImpl(httpSessionId, channel, messageConverter);
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
         throw new IllegalArgumentException("HTTP session id may not be null. ");
      }
      List<WSConnectionImpl> result = new ArrayList<WSConnectionImpl>();
      for (WSConnectionImpl connection : connections.values())
      {
         if (httpSessionId.equals(connection.getHttpSessionId()))
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
