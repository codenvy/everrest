/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import org.everrest.core.impl.EverrestProcessor;
import org.everrest.websockets.message.MessageSender;
import org.everrest.websockets.message.OutputMessage;
import org.everrest.websockets.message.RestInputMessage;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static javax.websocket.CloseReason.CloseCodes.getCloseCode;
import static org.everrest.websockets.ServerContainerInitializeListener.EVERREST_PROCESSOR_ATTRIBUTE;
import static org.everrest.websockets.ServerContainerInitializeListener.EXECUTOR_ATTRIBUTE;
import static org.everrest.websockets.ServerContainerInitializeListener.HTTP_SESSION_ATTRIBUTE;
import static org.everrest.websockets.ServerContainerInitializeListener.SECURITY_CONTEXT;

public class WSConnectionImpl extends Endpoint implements WSConnection {
    private static final AtomicLong counter = new AtomicLong(1);

    private final long id = counter.getAndIncrement();
    private final List<WSMessageReceiver> messageReceivers;
    private final Set<String>             channels;
    private final Map<String, Object>     attributes;

    private Session       wsSession;
    private HttpSession   httpSession;
    private CloseReason   closeReason;
    private MessageSender messageSender;

    public WSConnectionImpl() {
        this.messageReceivers = new CopyOnWriteArrayList<>();
        this.channels = new CopyOnWriteArraySet<>();
        this.attributes = new HashMap<>();
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        wsSession = session;
        messageSender = new MessageSender(session);
        final Map<String, Object> userProperties = config.getUserProperties();
        httpSession = (HttpSession)userProperties.get(HTTP_SESSION_ATTRIBUTE);
        final WS2RESTAdapter restAdapter =
                new WS2RESTAdapter(this,
                                   (SecurityContext)userProperties.get(SECURITY_CONTEXT),
                                   (EverrestProcessor)userProperties.get(EVERREST_PROCESSOR_ATTRIBUTE),
                                   (Executor)userProperties.get(EXECUTOR_ATTRIBUTE));
        messageReceivers.add(restAdapter);
        wsSession.addMessageHandler(RestInputMessage.class, new MessageHandler.Whole<RestInputMessage>() {
            @Override
            public void onMessage(RestInputMessage message) {
                for (WSMessageReceiver receiver : messageReceivers) {
                    receiver.onMessage(message);
                }
            }
        });

        for (WSConnectionListener connectionListener : WSConnectionContext.connectionListeners) {
            connectionListener.onOpen(this);
        }

        WSConnectionContext.connections.put(getId(), this);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        this.closeReason = closeReason;
        for (WSConnectionListener connectionListener : WSConnectionContext.connectionListeners) {
            connectionListener.onClose(this);
        }
        super.onClose(session, closeReason);
    }


    @Override
    public void onError(Session session, Throwable thr) {
        if (thr instanceof DecodeException) {
            for (WSMessageReceiver receiver : messageReceivers) {
                receiver.onError((DecodeException)thr);
            }
        }
        super.onError(session, thr);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public HttpSession getHttpSession() {
        return httpSession;
    }

    @Override
    public Session getWsSession() {
        return wsSession;
    }

    @Override
    public boolean subscribeToChannel(String channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel name may not be null. ");
        }
        return channels.add(channel);
    }

    @Override
    public boolean unsubscribeFromChannel(String channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel name may not be null. ");
        }
        return channels.remove(channel);
    }

    @Override
    public Collection<String> getChannels() {
        return channels;
    }

    @Override
    public boolean isConnected() {
        return wsSession != null && wsSession.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (isConnected()) {
            wsSession.close();
        }
    }

    @Override
    public void close(int status, String message) throws IOException {
        if (isConnected()) {
            wsSession.close(new CloseReason(getCloseCode(status), message));
        }
    }

    @Override
    public int getCloseStatus() {
        return closeReason == null ? 0 : closeReason.getCloseCode().getCode();
    }

    @Override
    public void sendMessage(OutputMessage output) throws EncodeException, IOException {
        checkIsConnected();
        messageSender.send(output);
    }

    private void checkIsConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("Unable send message because the WebSocket connection has been closed");
        }
    }

    @Override
    public void registerMessageReceiver(WSMessageReceiver messageReceiver) {
        messageReceivers.add(messageReceiver);
    }

    @Override
    public void removeMessageReceiver(WSMessageReceiver messageReceiver) {
        messageReceivers.remove(messageReceiver);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("WSConnectionImpl{")
                                              .append("id=").append(id)
                                              .append(", wsSession=").append(wsSession.getId());
        if (httpSession != null) {
            sb.append(", httpSession=").append(httpSession.getId());
        }
        sb.append(", channels=").append(channels).append('}');
        return sb.toString();
    }
}
