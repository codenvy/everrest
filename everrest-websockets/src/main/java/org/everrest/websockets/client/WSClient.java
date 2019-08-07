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
package org.everrest.websockets.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.websockets.message.BaseTextEncoder;
import org.everrest.websockets.message.InputMessage;
import org.everrest.websockets.message.JsonMessageConverter;
import org.everrest.websockets.message.MessageSender;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfPossible;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.websocket.ContainerProvider.getWebSocketContainer;
import static javax.websocket.RemoteEndpoint.Basic;
import static org.everrest.core.impl.uri.UriComponent.parseQueryString;
import static org.everrest.websockets.message.RestInputMessage.newSubscribeChannelMessage;
import static org.everrest.websockets.message.RestInputMessage.newUnsubscribeChannelMessage;

/**
 * @author andrew00x
 */
@ClientEndpoint(encoders = {WSClient.InputMessageEncoder.class})
public class WSClient {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WSClient.class);

    private static ExecutorService executor = newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("everrest.WSClient-%d").setDaemon(true).build());

    private final URI                         serverUri;
    private final List<ClientMessageListener> listeners;
    private final List<String>                channels;

    private Session       session;
    private MessageSender messageSender;

    public WSClient(URI serverUri, ClientMessageListener... listeners) {
        this(Builder.create(serverUri).listeners(listeners));
    }

    public WSClient(Builder builder) {
        this.serverUri = builder.serverUri;
        this.listeners = builder.listeners;
        this.channels = builder.channels;
    }

    /**
     * Connect to server endpoint.
     *
     * @param timeout
     *         connection timeout value in seconds
     * @throws IOException
     *         if connection failed
     * @throws SocketTimeoutException
     *         if timeout occurs while try to connect to server endpoint
     * @throws IllegalArgumentException
     *         if {@code timeout} zero or negative
     */
    public void connect(int timeout) throws IOException, DeploymentException {
        if (timeout < 1) {
            throw new IllegalArgumentException(String.format("Invalid timeout: %d", timeout));
        }
        final WebSocketContainer container = getWebSocketContainer();
        container.setAsyncSendTimeout(1);
        try {
            executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final Session session = container.connectToServer(WSClient.this, serverUri);
                    final Basic remoteEndpoint = session.getBasicRemote();
                    for (String channel : channels) {
                        remoteEndpoint.sendObject(newSubscribeChannelMessage(uuid(), channel));
                    }
                    return null;
                }
            }).get(timeout, SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            throw new SocketTimeoutException("Connection timeout");
        } catch (ExecutionException e) {
            propagateIfPossible(e.getCause(), IOException.class, DeploymentException.class);
            propagate(e);
        }
    }

    public void disconnect() throws IOException {
        if (isConnected()) {
            session.close();
        }
    }

    public URI getServerUri() {
        return serverUri;
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    private void checkIsConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("Unable send message because the WebSocket connection has been closed");
        }
    }

    public void send(String message) throws IOException {
        checkIsConnected();
        messageSender.send(message);
    }

    public void send(byte[] message) throws IOException {
        checkIsConnected();
        messageSender.send(message);
    }

    public void send(InputMessage message) throws IOException, EncodeException {
        checkIsConnected();
        messageSender.send(message);
    }

    public void subscribeToChannel(String channel) throws IOException {
        try {
            send(newSubscribeChannelMessage(uuid(), channel));
        } catch (EncodeException e) {
            propagate(e);
        }
        LOG.debug("Subscribed to channel {}", channel);
    }

    public void unsubscribeFromChannel(String channel) throws IOException {
        try {
            send(newUnsubscribeChannelMessage(uuid(), channel));
        } catch (EncodeException e) {
            propagate(e);
        }
        LOG.debug("Unsubscribed from channel {}", channel);
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }

    @OnOpen
    public void onOpen(Session session) {
        LOG.debug("WS session {} started", session.getId());
        this.session = session;
        messageSender = new MessageSender(session);
        for (ClientMessageListener listener : listeners) {
            listener.onOpen(this);
        }
    }

    @OnMessage
    public void processTextMessage(String message) {
        for (ClientMessageListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    @OnMessage
    public void processBinaryMessage(byte[] message) {
        for (ClientMessageListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.debug("WS session {} about to be closed, {}", session.getId(), closeReason);
        for (ClientMessageListener listener : listeners) {
            listener.onClose(closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase());
        }
    }

    @OnError
    public void onError(Throwable error) {
        LOG.warn(error.getMessage(), error);
    }


    public static class InputMessageEncoder extends BaseTextEncoder<InputMessage> {
        private final JsonMessageConverter jsonMessageConverter = new JsonMessageConverter();

        @Override
        public String encode(InputMessage output) throws EncodeException {
            try {
                return jsonMessageConverter.toString(output);
            } catch (JsonException e) {
                throw new EncodeException(output, e.getMessage(), e);
            }
        }
    }


    public static class Builder {
        private final URI                         serverUri;
        private final List<ClientMessageListener> listeners;
        private final List<String>                channels;

        public static Builder create(URI serverUri) {
            return new Builder(serverUri);
        }

        public Builder(URI serverUri) {
            if (serverUri == null) {
                throw new IllegalArgumentException("Connection URI may not be null");
            }
            this.serverUri = serverUri;
            listeners = new LinkedList<>();
            channels = new LinkedList<>();
            final List<String> channelsFromUri = parseQueryString(serverUri.getRawQuery(), true).get("channel");
            if (channelsFromUri != null) {
                channels.addAll(channelsFromUri);
            }
        }

        public Builder listeners(ClientMessageListener... listeners) {
            Collections.addAll(this.listeners, listeners);
            return this;
        }

        public Builder listeners(Collection<ClientMessageListener> listeners) {
            this.listeners.addAll(listeners);
            return this;
        }

        public Builder channels(String... channels) {
            Collections.addAll(this.channels, channels);
            return this;
        }

        public Builder channels(Collection<String> channels) {
            this.channels.addAll(channels);
            return this;
        }

        public WSClient build() {
            return new WSClient(this);
        }
    }
}
