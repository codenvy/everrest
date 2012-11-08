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

import org.apache.catalina.websocket.Constants;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.everrest.websockets.message.MessageConversionException;
import org.everrest.websockets.message.MessageConverter;
import org.everrest.websockets.message.OutputMessage;
import org.everrest.websockets.message.RESTfulInputMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WSConnectionImpl extends MessageInbound implements WSConnection
{
   private static final AtomicLong counter = new AtomicLong(1);
   private static final Charset UTF8_CS = Charset.forName("UTF-8");

   private final long connectionId = counter.getAndIncrement();
   private final String httpSessionId;
   private final String channel;
   private final MessageConverter messageConverter;
   private final List<WSMessageReceiver> messageReceivers = new CopyOnWriteArrayList<WSMessageReceiver>();

   WSConnectionImpl(String httpSessionId, String channel, MessageConverter messageConverter)
   {
      this.httpSessionId = httpSessionId;
      this.channel = channel;
      this.messageConverter = messageConverter;
   }

   //

   @Override
   protected void onBinaryMessage(ByteBuffer message) throws IOException
   {
      getWsOutbound().close(Constants.STATUS_UNEXPECTED_DATA_TYPE, UTF8_CS.encode("Binary messages is not supported. "));
   }

   @Override
   protected void onTextMessage(CharBuffer message) throws IOException
   {
      try
      {
         RESTfulInputMessage input = messageConverter.fromString(message.toString(), RESTfulInputMessage.class);
         for (WSMessageReceiver receiver : messageReceivers)
         {
            receiver.onMessage(input);
         }
      }
      catch (MessageConversionException e)
      {
         for (WSMessageReceiver receiver : messageReceivers)
         {
            receiver.onError(e);
         }
      }
   }

   @Override
   protected void onOpen(WsOutbound outbound)
   {
      // Notify connection listeners about this connection is ready to use.
      for (WSConnectionListener connectionListener : WSConnectionContext.connectionListeners)
      {
         connectionListener.onOpen(this);
      }
   }

   @Override
   protected void onClose(int status)
   {
      for (WSConnectionListener connectionListener : WSConnectionContext.connectionListeners)
      {
         connectionListener.onClose(connectionId, status);
      }
   }

   //

   public Long getConnectionId()
   {
      return connectionId;
   }

   @Override
   public String getHttpSessionId()
   {
      return httpSessionId;
   }

   @Override
   public String getChannel()
   {
      return channel;
   }

   @Override
   public void close() throws IOException
   {
      getWsOutbound().close(Constants.STATUS_CLOSE_NORMAL, null);
   }

   @Override
   public void sendMessage(OutputMessage output) throws MessageConversionException, IOException
   {
      CharBuffer message = CharBuffer.wrap(messageConverter.toString(output));
      WsOutbound out = getWsOutbound();
      out.writeTextMessage(message);
      out.flush();
   }

   @Override
   public void registerMessageReceiver(WSMessageReceiver messageReceiver)
   {
      messageReceivers.add(messageReceiver);
   }

   @Override
   public void removeMessageReceiver(WSMessageReceiver messageReceiver)
   {
      messageReceivers.remove(messageReceiver);
   }

   @Override
   public String toString()
   {
      return "WSConnection{" +
         "connectionId=" + connectionId +
         ", httpSessionId='" + httpSessionId + '\'' +
         ", channel='" + channel + '\'' +
         '}';
   }
}
