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

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Receives text messages over websocket connection and notify all registered WSMessageReceivers. This implementation
 * does not support binary messages. If binary message received then connection will be closed with error status and
 * then UnsupportedOperationException will be thrown.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class WSConnectionImpl extends MessageInbound implements WSConnection {
    private static final AtomicLong counter = new AtomicLong(1);
    private static final Charset    UTF8_CS = Charset.forName("UTF-8");

    private final long id = counter.getAndIncrement();
    private final HttpSession             httpSession;
    private final MessageConverter        messageConverter;
    private final List<WSMessageReceiver> messageReceivers;
    private final Set<String>             channels;
    private final Set<String>             readOnlyChannels;
    private final AtomicBoolean connected   = new AtomicBoolean(false);
    private       int           closeStatus = 0;

    WSConnectionImpl(HttpSession httpSession, MessageConverter messageConverter) {
        this.httpSession = httpSession;
        this.messageConverter = messageConverter;
        this.messageReceivers = new CopyOnWriteArrayList<WSMessageReceiver>();
        this.channels = new CopyOnWriteArraySet<String>();
        this.readOnlyChannels = Collections.unmodifiableSet(channels);
    }

    //

    @Override
    protected void onBinaryMessage(ByteBuffer message) throws IOException {
        if (connected.compareAndSet(true, false)) {
            getWsOutbound().close(Constants.STATUS_UNEXPECTED_DATA_TYPE, UTF8_CS.encode("Binary messages is not supported. "));
        }
        throw new UnsupportedOperationException("Binary messages is not supported. ");
    }

    @Override
    protected void onTextMessage(CharBuffer message) throws IOException {
        RESTfulInputMessage input = null;
        MessageConversionException error = null;
        try {
            input = messageConverter.fromString(message.toString(), RESTfulInputMessage.class);
        } catch (MessageConversionException e) {
            error = e;
        }

        if (error != null) {
            for (WSMessageReceiver receiver : messageReceivers) {
                receiver.onError(error);
            }
        } else {
            for (WSMessageReceiver receiver : messageReceivers) {
                receiver.onMessage(input);
            }
        }
    }

    @Override
    protected void onOpen(WsOutbound outbound) {
        // Notify connection listeners about this connection is ready to use.
        for (WSConnectionListener connectionListener : WSConnectionContext.connectionListeners) {
            connectionListener.onOpen(this);
        }
        connected.compareAndSet(false, true);
    }

    @Override
    protected void onClose(int status) {
        connected.compareAndSet(true, false);
        closeStatus = status;
        // Notify connection listeners about this connection is closed.
        for (WSConnectionListener connectionListener : WSConnectionContext.connectionListeners) {
            connectionListener.onClose(this);
        }
    }

    //

    public Long getId() {
        return id;
    }

    @Override
    public HttpSession getHttpSession() {
        return httpSession;
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
        return readOnlyChannels;
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public int getCloseStatus() {
        return closeStatus;
    }

    @Override
    public void close() throws IOException {
        if (connected.compareAndSet(true, false)) {
            getWsOutbound().close(Constants.STATUS_CLOSE_NORMAL, null);
        }
    }

    @Override
    public void sendMessage(OutputMessage output) throws MessageConversionException, IOException {
        CharBuffer message = CharBuffer.wrap(messageConverter.toString(output));
        WsOutbound out = getWsOutbound();
        out.writeTextMessage(message);
        out.flush();
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
    public String toString() {
        return "WSConnectionImpl{" +
               "id=" + id +
               ", httpSession='" + httpSession.getId() + '\'' +
               ", channels=" + channels +
               '}';
    }
}
