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

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Collection;
import org.everrest.websockets.message.OutputMessage;

/**
 * Web socket connection abstraction.
 *
 * @author andrew00x
 */
public interface WSConnection {
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

  Session getWsSession();

  /**
   * Subscribe this connection to specified channel.
   *
   * @param channel channel name
   * @return <code>true</code> if this connection is subscribed to channel successfully and <code>
   *     false</code> if connection already subscribed to specified channel
   * @see WSConnectionContext#sendMessage(org.everrest.websockets.message.ChannelBroadcastMessage)
   */
  boolean subscribeToChannel(String channel);

  /**
   * Unsubscribe this connection from specified channel.
   *
   * @param channel channel name
   * @return <code>true</code> if this connection is unsubscribed from channel successfully and
   *     <code>false</code> if connection is not subscribed to specified channel
   * @see WSConnectionContext#sendMessage(org.everrest.websockets.message.ChannelBroadcastMessage)
   */
  boolean unsubscribeFromChannel(String channel);

  /**
   * Get optional set of channels assigned for this connection. Channel may be used for sending the
   * same message to more than one connection at one time.
   *
   * @return unmodifiable set of channel names this connection subscribed
   * @see WSConnectionContext#sendMessage(org.everrest.websockets.message.ChannelBroadcastMessage)
   */
  Collection<String> getChannels();

  /**
   * Check connection state.
   *
   * @return <code>true</code> if connection is alive and <code>false</code> if connection already
   *     closed.
   */
  boolean isConnected();

  /**
   * Get connection close status. If connection is alive or not opened yet this method return <code>
   * 0</code>.
   *
   * @return connection close status or <code>0</code> if connection is alive or not opened yet
   */
  int getCloseStatus();

  /**
   * Close this connection with 1000 status which indicates a normal closure.
   *
   * @throws IOException if any i/o error occurs
   */
  void close() throws IOException;

  /**
   * Close this connection with specified status and message.
   *
   * @param status connection close status
   * @param message optional message
   * @throws IOException if any i/o error occurs
   */
  void close(int status, String message) throws IOException;

  /**
   * Send message to client.
   *
   * @param output output message
   * @throws EncodeException if message cannot be serialized
   * @throws IOException if any i/o error occurs when try to send message to client
   */
  void sendMessage(OutputMessage output) throws EncodeException, IOException;

  /**
   * Register new WSMessageReceiver for this connection.
   *
   * @param messageReceiver message receiver
   */
  void registerMessageReceiver(WSMessageReceiver messageReceiver);

  /**
   * Unregister WSMessageReceiver.
   *
   * @param messageReceiver message receiver
   */
  void removeMessageReceiver(WSMessageReceiver messageReceiver);

  /**
   * Returns the object bound with the specified name in this connection, or <code>null</code> if no
   * object is bound under the name.
   *
   * @param name a string specifying the name of the object
   * @return the object with the specified name
   */
  public Object getAttribute(String name);

  /**
   * Binds an object to this connection, using the name specified. If an object of the same name is
   * already bound to the connection, the object is replaced.
   *
   * <p>
   *
   * @param name the name to which the object is bound; cannot be null
   * @param value the object to be bound
   */
  public void setAttribute(String name, Object value);

  /**
   * Removes the object bound with the specified name from this connection. If the connection does
   * not have an object bound with the specified name, this method does nothing.
   *
   * <p>
   *
   * @param name the name of the object to remove from this connection
   */
  public void removeAttribute(String name);
}
