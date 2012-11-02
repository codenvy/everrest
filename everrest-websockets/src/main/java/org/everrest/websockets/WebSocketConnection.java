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
import org.apache.commons.codec.binary.Base64;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.async.AsynchronousJob;
import org.everrest.core.impl.async.AsynchronousJobListener;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.tools.SecurityContextRequest;
import org.everrest.core.util.Logger;
import org.everrest.websockets.message.InputMessage;
import org.everrest.websockets.message.MessageConverter;
import org.everrest.websockets.message.MessageConverterException;
import org.everrest.websockets.message.OutputMessage;
import org.everrest.websockets.message.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WebSocketConnection extends MessageInbound
{
   private static final Logger LOG = Logger.getLogger(WebSocketConnection.class);
   private static final ConcurrentMap<String, Set<WebSocketConnection>> connections =
      new ConcurrentHashMap<String, Set<WebSocketConnection>>();
   private static final ConcurrentMap<String, List<OutputMessage>> unsentMessages =
      new ConcurrentHashMap<String, List<OutputMessage>>();

   private static final AtomicLong num = new AtomicLong(1);

   /**
    * Open new web socket connection.
    *
    * @param httpSessionId
    *    id of HTTP session associated to this connection. If few connections open for the same HTTP session all of
    *    them receive response for request sent to one of connections
    * @param channel
    *    optional name on channel. If channel is specified for InputMessage then only connections with the same channel
    *    name may see response
    * @param securityContext
    *    security related information
    * @param everrestProcessor
    *    instance of EverrestProcessor
    * @param asynchronousPool
    *    instance of AsynchronousJobPool
    * @param messageConverter
    *    converts input messages from raw message represented by CharBuffer to InputMessage and converts back
    *    OutputMessage to CharBuffer
    * @return new WebSocketConnection
    */
   public static WebSocketConnection open(String httpSessionId,
                                          String channel,
                                          SecurityContext securityContext,
                                          EverrestProcessor everrestProcessor,
                                          AsynchronousJobPool asynchronousPool,
                                          MessageConverter messageConverter)
   {
      if (httpSessionId == null)
      {
         throw new IllegalArgumentException("HTTP Session Id required. ");
      }
      if (everrestProcessor == null)
      {
         throw new IllegalArgumentException("EverrestProcessor required. ");
      }
      if (asynchronousPool == null)
      {
         throw new IllegalArgumentException("AsynchronousJobPool required. ");
      }
      if (messageConverter == null)
      {
         throw new IllegalArgumentException("MessageConverter required. ");
      }
      WebSocketConnection newConnection = new WebSocketConnection(httpSessionId, channel, securityContext,
         everrestProcessor, asynchronousPool, messageConverter);
      Set<WebSocketConnection> connectionsList = connections.get(httpSessionId);
      if (connectionsList == null)
      {
         CopyOnWriteArraySet<WebSocketConnection> newConnectionsList = new CopyOnWriteArraySet<WebSocketConnection>();
         connectionsList = connections.putIfAbsent(httpSessionId, newConnectionsList);
         if (connectionsList == null)
         {
            connectionsList = newConnectionsList;
         }
      }
      connectionsList.add(newConnection);
      LOG.debug("Open connection {} ", newConnection);
      return newConnection;
   }

   /**
    * Close all connections associated with specified HTTP session Id.
    *
    * @param httpSessionId
    *    HTTP session Id
    */
   public static void close(String httpSessionId)
   {
      Set<WebSocketConnection> connectionsList = connections.get(httpSessionId);
      if (connectionsList != null)
      {
         for (WebSocketConnection connection : connectionsList)
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
      }
      unsentMessages.remove(httpSessionId);
   }


   private final long uniqueNum = num.getAndIncrement();
   private final String httpSessionId;
   private final String channel;
   private final SecurityContext securityContext;
   private final EverrestProcessor everrestProcessor;
   private final AsynchronousJobPool asynchronousPool;
   private final MessageConverter messageConverter;

   private WebSocketConnection(String httpSessionId,
                               String channel,
                               SecurityContext securityContext,
                               EverrestProcessor everrestProcessor,
                               AsynchronousJobPool asynchronousPool,
                               MessageConverter messageConverter)
   {
      this.httpSessionId = httpSessionId;
      this.channel = channel;
      this.securityContext = securityContext;
      this.everrestProcessor = everrestProcessor;
      this.asynchronousPool = asynchronousPool;
      this.messageConverter = messageConverter;
   }

   //

   @Override
   protected void onBinaryMessage(ByteBuffer message) throws IOException
   {
      throw new UnsupportedOperationException("Binary messages is not supported. ");
   }

   @Override
   protected void onTextMessage(CharBuffer message) throws IOException
   {
      final InputMessage input;
      try
      {
         input = unwrapInputMessage(message);
      }
      catch (MessageConverterException e)
      {
         // Cannot parse input message. Probably input message is malformed.
         LOG.error(e.getMessage(), e);
         OutputMessage output = new OutputMessage();
         output.setBody(e.getMessage());
         output.setStatus(400);
         // Cannot provide other parameters since parsing of input message is failed.
         sendMessage(output);
         return;
      }

      final String internalUuid = UUID.randomUUID().toString();

      // This listener called when asynchronous task is done.
      asynchronousPool.registerListener(new AsynchronousJobListener()
      {
         @Override
         public void done(AsynchronousJob job)
         {
            MultivaluedMap<String, String> requestHeaders = ((ContainerRequest)job.getContext()
               .get("org.everrest.async.request")).getRequestHeaders();

            if ("websocket".equals(requestHeaders.getFirst("x-everrest-protocol"))
               && internalUuid.equals(requestHeaders.getFirst("x-everrest-websocket-tracker-id")))
            {
               URI requestUri = UriBuilder.fromPath("/async/" + job.getJobId()).build();
               ContainerRequest req = new SecurityContextRequest("GET", requestUri, URI.create(""), null,
                  new InputHeadersMap(), securityContext);

               OutputMessage output = newOutputMessage(input);
               ContainerResponse resp = new ContainerResponse(
                  new MessageWriter(output, input.isEncodeResponseBodyBase64()));

               try
               {
                  everrestProcessor.process(req, resp, new EnvironmentContext());
               }
               catch (Exception e)
               {
                  LOG.error(e.getMessage(), e);
               }
               finally
               {
                  // Not need this listener any more.
                  asynchronousPool.unregisterListener(this);
               }

               sendMessage(output);
            }
         }
      });

      InputStream data = null;
      String body = input.getBody();
      if (body != null)
      {
         byte[] bytes;
         try
         {
            bytes = body.getBytes("UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            // Should never happen since UTF-8 is supported.
            throw new IllegalStateException(e.getMessage(), e);
         }
         if (input.isBodyEncodedBase64())
         {
            bytes = Base64.decodeBase64(bytes);
         }
         data = new ByteArrayInputStream(bytes);
      }

      URI requestUri = UriBuilder.fromPath("/").path(input.getPath()).build();
      MultivaluedMap<String, String> headers = Pair.toMap(input.getHeaders());
      if (data != null)
      {
         // Always know content length since we use ByteArrayInputStream.
         headers.putSingle("content-length", Integer.toString(data.available()));
      }

      // Put some additional 'helper' headers.
      headers.putSingle("x-everrest-async", "true");
      headers.putSingle("x-everrest-protocol", "websocket");
      headers.putSingle("x-everrest-websocket-tracker-id", internalUuid);

      ContainerRequest req = new SecurityContextRequest(input.getMethod(), requestUri, URI.create(""), data,
         new InputHeadersMap(headers), securityContext);

      OutputMessage output = newOutputMessage(input);
      ContainerResponse resp = new ContainerResponse(
         new MessageWriter(output, input.isEncodeResponseBodyBase64()));

      try
      {
         everrestProcessor.process(req, resp, new EnvironmentContext());
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
      }

      sendMessage(output);
   }

   @Override
   protected void onOpen(WsOutbound outbound)
   {
      List<OutputMessage> failed = unsentMessages.get(httpSessionId);
      if (failed != null)
      {
         sendMessages(failed);
      }
   }

   @Override
   protected void onClose(int status)
   {
      Set<WebSocketConnection> connectionsList = connections.get(httpSessionId);
      connectionsList.remove(this);
      if (connectionsList.isEmpty())
      {
         connections.remove(httpSessionId, connectionsList);
      }
      LOG.debug("Close connection {} ", this);
   }

   public void close() throws IOException
   {
      getWsOutbound().close(Constants.STATUS_CLOSE_NORMAL, null);
   }

   //

   private OutputMessage newOutputMessage(InputMessage input)
   {
      OutputMessage output = new OutputMessage();
      output.setUuid(input.getUuid());
      output.setChannel(input.getChannel());
      output.setMethod(input.getMethod());
      output.setPath(input.getPath());
      return output;
   }

   private InputMessage unwrapInputMessage(CharBuffer input) throws MessageConverterException
   {
      return messageConverter.read(input);
   }

   private CharBuffer wrapOutputMessage(OutputMessage output) throws MessageConverterException
   {
      return messageConverter.write(output);
   }

   private void sendMessage(OutputMessage output)
   {
      CharBuffer message;
      try
      {
         message = wrapOutputMessage(output);
      }
      catch (MessageConverterException e)
      {
         LOG.error(e.getMessage(), e);
         // Do nothing for message that cannot serialize.
         return;
      }

      boolean sent = false;
      Set<WebSocketConnection> connectionsList = connections.get(httpSessionId);
      if (connectionsList != null)
      {
         final String outputChannel = output.getChannel();
         for (WebSocketConnection connection : connectionsList)
         {
            if (outputChannel == null || outputChannel.equals(connection.channel))
            {
               try
               {
                  WsOutbound out = connection.getWsOutbound();
                  out.writeTextMessage(message.duplicate());
                  out.flush();
                  sent = true;
               }
               catch (IOException e)
               {
                  LOG.error(e.getMessage(), e);
               }
            }
         }
      }
      // Assumes message is sent successfully if it sent to at least one connection.
      if (!sent)
      {
         saveUnsentMessage(output);
      }
   }

   private void saveUnsentMessage(OutputMessage output)
   {
      List<OutputMessage> unsentList = unsentMessages.get(httpSessionId);
      if (unsentList == null)
      {
         List<OutputMessage> newUnsentList = new CopyOnWriteArrayList<OutputMessage>();
         unsentList = unsentMessages.putIfAbsent(httpSessionId, newUnsentList);
         if (unsentList == null)
         {
            unsentList = newUnsentList;
         }
      }
      unsentList.add(output);
   }

   private void sendMessages(Collection<OutputMessage> toSend)
   {
      List<OutputMessage> remove = new ArrayList<OutputMessage>();
      for (OutputMessage output : toSend)
      {
         final String outputChannel = output.getChannel();
         if (outputChannel == null || outputChannel.equals(this.channel))
         {
            CharBuffer message;
            try
            {
               message = wrapOutputMessage(output);
            }
            catch (MessageConverterException e)
            {
               LOG.error(e.getMessage(), e);
               remove.add(output); // Do not keep message which we cannot serialize.
               continue;
            }
            try
            {
               getWsOutbound().writeTextMessage(message);
               remove.add(output);
            }
            catch (IOException ignored)
            {
               // Will try send later.
            }
         }
      }
      // Remove all messages that we cannot serialize and messages we sent successfully.
      toSend.removeAll(remove);
   }

   @Override
   public String toString()
   {
      return "WebSocketConnection{" +
         "httpSessionId='" + httpSessionId + '\'' +
         ", uniqueNum=" + uniqueNum +
         ", channel='" + channel + '\'' +
         '}';
   }
}
