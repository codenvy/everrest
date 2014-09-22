/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.websockets;

import org.everrest.core.util.Logger;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.everrest.websockets.message.MessageConversionException;
import org.everrest.websockets.message.MessageConverter;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RESTfulOutputMessage;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author andrew00x
 */
public class WSConnectionContext {
    private static final Logger                      LOG                 = Logger.getLogger(WSConnectionContext.class);
    static final         List<WSConnectionListener>  connectionListeners = new CopyOnWriteArrayList<>();
    static final         Map<Long, WSConnectionImpl> connections         = new ConcurrentHashMap<>();

    public static boolean registerConnectionListener(WSConnectionListener listener) {
        return connectionListeners.add(listener);
    }

    public static boolean removeConnectionListener(WSConnectionListener listener) {
        return connectionListeners.remove(listener);
    }

    /**
     * Send message to all connections subscribed to the channel. Method tries to send message to as many connections as
     * possible. Even if method fails to send message to the first connection it will try to send message to other
     * connections, if any. After that a first occurred error is rethrown.
     *
     * @param message
     *         message
     * @throws MessageConversionException
     *         if message cannot be serialized
     * @throws IOException
     *         if any i/o error occurs when try to send message to client
     * @see org.everrest.websockets.message.ChannelBroadcastMessage#getChannel()
     */
    public static void sendMessage(ChannelBroadcastMessage message) throws MessageConversionException, IOException {
        final String channel = message.getChannel();
        final RESTfulOutputMessage output = newRESTfulOutputMessage(message);
        Exception error = null;
        for (WSConnectionImpl connection : connections.values()) {
            if (connection.getChannels().contains(channel)) {
                try {
                    connection.sendMessage(output);
                } catch (MessageConversionException e) {
                    if (error == null) {
                        error = e;
                    }
                } catch (IOException e) {
                    if (error == null) {
                        error = e;
                    }
                }
            }
        }
        if (error instanceof MessageConversionException) {
            throw (MessageConversionException)error;
        } else if (error != null) {
            // If error is not null then may be IOException only.
            throw (IOException)error;
        }
    }

    private static RESTfulOutputMessage newRESTfulOutputMessage(ChannelBroadcastMessage message) {
        final RESTfulOutputMessage transport = new RESTfulOutputMessage();
        transport.setUuid(message.getUuid());
        transport.setHeaders(new Pair[]{Pair.of("x-everrest-websocket-channel", message.getChannel()),
                                        Pair.of("x-everrest-websocket-message-type", message.getType().toString())});
        transport.setBody(message.getBody());
        return transport;
    }

    static {
        registerConnectionListener(new WSConnectionListener() {
            @Override
            public void onOpen(WSConnection connection) {
                LOG.debug("Open connection {} ", connection);
            }

            @Override
            public void onClose(WSConnection connection) {
                LOG.debug("Close connection {} with status {} ", connection, connection.getCloseStatus());
                connections.remove(connection.getId());
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
     *         HTTP session associated to this connection
     * @param messageConverter
     *         converts input messages from raw message represented by String to InputMessage and converts back OutputMessage
     *         to String
     * @see #registerConnectionListener(WSConnectionListener)
     * @see #removeConnectionListener(WSConnectionListener)
     */
    static WSConnectionImpl open(HttpSession httpSession, MessageConverter messageConverter) {
        if (httpSession == null) {
            throw new IllegalArgumentException("HTTP Session required. ");
        }
        if (messageConverter == null) {
            throw new IllegalArgumentException("MessageConverter required. ");
        }
        final WSConnectionImpl newConnection = new WSConnectionImpl(httpSession, messageConverter);
        connections.put(newConnection.getId(), newConnection);
        return newConnection;
    }

    /**
     * Get all WSConnection associated with specified HTTP session.
     *
     * @param httpSessionId
     *         HTTP session Id
     * @return all WSConnection associated with specified HTTP session
     */
    static Collection<WSConnectionImpl> getAll(String httpSessionId) {
        if (httpSessionId == null) {
            throw new IllegalArgumentException("HTTP session may not be null. ");
        }
        final List<WSConnectionImpl> result = new ArrayList<>();
        for (WSConnectionImpl connection : connections.values()) {
            if (httpSessionId.equals(connection.getHttpSession().getId())) {
                result.add(connection);
            }
        }
        return result;
    }

    /**
     * Close all connections associated with specified HTTP session Id.
     *
     * @param httpSessionId
     *         HTTP session Id
     */
    static void closeAll(String httpSessionId) {
        final Collection<WSConnectionImpl> toRemove = getAll(httpSessionId);
        for (WSConnectionImpl connection : toRemove) {
            try {
                connection.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        connections.values().removeAll(toRemove);
    }

    /**
     * Close all connections associated with specified HTTP session Id.
     *
     * @param httpSessionId
     *         HTTP session Id
     * @param status
     *         Status code
     * @param message
     *         Closing message
     *
     */
    static void closeAll(String httpSessionId, int status, String message) {
        final Collection<WSConnectionImpl> toRemove = getAll(httpSessionId);
        for (WSConnectionImpl connection : toRemove) {
            try {
                connection.close(status, message);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        connections.values().removeAll(toRemove);
    }

    private WSConnectionContext() {
    }
}
