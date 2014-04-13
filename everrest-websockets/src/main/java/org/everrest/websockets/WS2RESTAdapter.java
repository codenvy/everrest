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
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.util.Logger;
import org.everrest.websockets.message.InputMessage;
import org.everrest.websockets.message.MessageConversionException;
import org.everrest.websockets.message.OutputMessage;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RESTfulInputMessage;
import org.everrest.websockets.message.RESTfulOutputMessage;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author andrew00x
 */
class WS2RESTAdapter implements WSMessageReceiver {
    private static final Logger LOG = Logger.getLogger(WS2RESTAdapter.class);

    private static final URI BASE_URI = URI.create("");

    private final WSConnection      connection;
    private final SecurityContext   securityContext;
    private final EverrestProcessor everrestProcessor;
    private final Executor          executor;
    private final Set<String>       inProgress;

    WS2RESTAdapter(WSConnection connection, SecurityContext securityContext, EverrestProcessor everrestProcessor, Executor executor) {
        this.connection = connection;
        this.securityContext = securityContext;
        this.everrestProcessor = everrestProcessor;
        this.executor = executor;
        this.inProgress = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    @Override
    public void onMessage(final InputMessage input) {
        // See method onTextMessage(CharBuffer) in class WSConnectionImpl.
        if (!(input instanceof RESTfulInputMessage)) {
            throw new IllegalArgumentException("Invalid input message. ");
        }
        final RESTfulInputMessage request = (RESTfulInputMessage)input;
        final MultivaluedMap<String, String> headers = Pair.toMap(request.getHeaders());
        final String messageType = headers.getFirst("x-everrest-websocket-message-type");
        if ("ping".equalsIgnoreCase(messageType)) {
            // At the moment is no JavaScript API to send ping message from client side.
            final RESTfulOutputMessage pong = newOutputMessage(request);
            // Copy body from ping request.
            pong.setBody(request.getBody());
            pong.setResponseCode(200);
            pong.setHeaders(new Pair[]{Pair.of("x-everrest-websocket-message-type", "pong")});
            doSendMessage(pong);
            return;
        }
        if ("subscribe-channel".equalsIgnoreCase(messageType) || "unsubscribe-channel".equalsIgnoreCase(messageType)) {
            final String channel = parseSubscriptionMessage(input);
            final RESTfulOutputMessage response = newOutputMessage(request);
            // Send the same body as in request.
            response.setBody(request.getBody());
            response.setHeaders(new Pair[]{Pair.of("x-everrest-websocket-message-type", messageType)});
            if (channel != null) {
                if ("subscribe-channel".equalsIgnoreCase(messageType)) {
                    connection.subscribeToChannel(channel);
                } else {
                    connection.unsubscribeFromChannel(channel);
                }
                response.setResponseCode(200);
            } else {
                LOG.error("Invalid message: {} ", input.getBody());
                // If cannot get channel name from input message consider it is client error.
                response.setResponseCode(400);
            }
            doSendMessage(response);
            return;
        }
        final String uuid = request.getUuid();
        if (uuid == null) {
            throw new IllegalArgumentException("Invalid input message. Message UUID is required. ");
        }
        if (inProgress.contains(uuid)) {
            // Re-send accept response if client tries send message with the same id
            final RESTfulOutputMessage response = newOutputMessage(request);
            response.setResponseCode(202);
            doSendMessage(response);
        }
        executor.execute(new Runnable() {
            public void run() {
                try {
                    ByteArrayInputStream data = null;
                    final String body = input.getBody();
                    if (body != null) {
                        try {
                            data = new ByteArrayInputStream(body.getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            // Should never happen since UTF-8 is supported.
                            throw new IllegalStateException(e.getMessage(), e);
                        }
                    }
                    final String requestPath = request.getPath();
                    final URI requestUri = requestPath == null || requestPath.isEmpty()
                                           ? URI.create("/")
                                           : URI.create(requestPath.charAt(0) == '/' ? requestPath : ('/' + requestPath));
                    if (data != null) {
                        // Always know content length since we use ByteArrayInputStream.
                        headers.putSingle("content-length", Integer.toString(data.available()));
                    }
                    final RESTfulOutputMessage response = newOutputMessage(request);
                    final ContainerRequest internalRequest = new ContainerRequest(request.getMethod(),
                                                                                  requestUri,
                                                                                  BASE_URI,
                                                                                  data,
                                                                                  new InputHeadersMap(headers),
                                                                                  securityContext);
                    final ContainerResponse internalResponse = new ContainerResponse(new EverrestResponseWriter(response));
                    final EnvironmentContext env = new EnvironmentContext();
                    env.put(WSConnection.class, connection);
                    everrestProcessor.process(internalRequest, internalResponse, env);
                    doSendMessage(response);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    inProgress.remove(uuid);
                }
            }
        });
        // send accept response
        final RESTfulOutputMessage restOutputMessage = newOutputMessage(request);
        restOutputMessage.setResponseCode(202);
        inProgress.add(uuid);
        doSendMessage(restOutputMessage);
    }

    @Override
    public void onError(Exception error) {
        LOG.error(error.getMessage(), error);
        if (error instanceof MessageConversionException) {
            try {
                connection.close(Constants.STATUS_POLICY_VIOLATION, error.getMessage());
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private RESTfulOutputMessage newOutputMessage(RESTfulInputMessage input) {
        final RESTfulOutputMessage output = new RESTfulOutputMessage();
        output.setUuid(input.getUuid());
        output.setMethod(input.getMethod());
        output.setPath(input.getPath());
        return output;
    }

    private void doSendMessage(OutputMessage output) {
        if (connection.isConnected()) {
            try {
                connection.sendMessage(output);
            } catch (MessageConversionException | IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            LOG.warn("Connection is already closed. ");
        }
    }

    /**
     * Get name of channel from input message. Expected format of message: {"channel":"my_channel"}. Method return
     * <code>null</code> if message is invalid.
     */
    private String parseSubscriptionMessage(InputMessage input) {
        final JsonParser p = new JsonParser();
        try {
            p.parse(new StringReader(input.getBody()));
        } catch (JsonException e) {
            return null;
        }
        final JsonValue jv = p.getJsonObject().getElement("channel");
        return jv != null ? jv.getStringValue() : null;
    }
}
