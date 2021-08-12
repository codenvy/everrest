/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.websockets;

import jakarta.websocket.EncodeException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RestOutputMessage;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class WSConnectionContext {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WSConnectionContext.class);

  static final List<WSConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
  static final Map<Long, WSConnectionImpl> connections = new ConcurrentHashMap<>();

  public static boolean registerConnectionListener(WSConnectionListener listener) {
    return connectionListeners.add(listener);
  }

  public static boolean removeConnectionListener(WSConnectionListener listener) {
    return connectionListeners.remove(listener);
  }

  /**
   * Send message to all connections subscribed to the channel. Method tries to send message to as
   * many connections as possible. Even if method fails to send message to the first connection it
   * will try to send message to other connections, if any. After that a first occurred error is
   * rethrown.
   *
   * @param message message
   * @throws EncodeException if message cannot be serialized
   * @throws IOException if any i/o error occurs when try to send message to client
   * @see org.everrest.websockets.message.ChannelBroadcastMessage#getChannel()
   */
  public static void sendMessage(ChannelBroadcastMessage message)
      throws EncodeException, IOException {
    final String channel = message.getChannel();
    final RestOutputMessage transport = newRestOutputMessage(message);
    Exception error = null;
    for (WSConnectionImpl connection : connections.values()) {
      if (connection.getChannels().contains(channel)) {
        try {
          connection.sendMessage(transport);
        } catch (EncodeException | IOException e) {
          if (error == null) {
            error = e;
          }
        }
      }
    }
    if (error instanceof EncodeException) {
      throw (EncodeException) error;
    } else if (error != null) {
      // If error is not null then may be IOException only.
      throw (IOException) error;
    }
  }

  private static RestOutputMessage newRestOutputMessage(ChannelBroadcastMessage message) {
    final RestOutputMessage transport = new RestOutputMessage();
    transport.setUuid(message.getUuid());
    transport.setHeaders(
        new Pair[] {
          Pair.of("x-everrest-websocket-channel", message.getChannel()),
          Pair.of("x-everrest-websocket-message-type", message.getType().toString())
        });
    transport.setBody(message.getBody());
    return transport;
  }

  static {
    registerConnectionListener(
        new WSConnectionListener() {
          @Override
          public void onOpen(WSConnection connection) {
            LOG.debug("Open connection {} ", connection);
          }

          @Override
          public void onClose(WSConnection connection) {
            LOG.debug(
                "Close connection {} with status {} ", connection, connection.getCloseStatus());
            connections.remove(connection.getId());
          }
        });
  }

  private WSConnectionContext() {}
}
