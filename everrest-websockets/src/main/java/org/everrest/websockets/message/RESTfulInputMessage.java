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
package org.everrest.websockets.message;

/**
 * RESTful input message.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class RESTfulInputMessage extends InputMessage {
    public static RESTfulInputMessage newPingMessage(String uuid, String message) {
        final RESTfulInputMessage instance = new RESTfulInputMessage();
        instance.setUuid(uuid);
        instance.setMethod("POST");
        instance.setHeaders(new Pair[]{new Pair("x-everrest-websocket-message-type", "ping")});
        instance.setBody(message);
        return instance;
    }

    public static RESTfulInputMessage newSubscribeChannelMessage(String uuid, String channel) {
        final RESTfulInputMessage instance = new RESTfulInputMessage();
        instance.setUuid(uuid);
        instance.setMethod("POST");
        instance.setHeaders(new Pair[]{new Pair("x-everrest-websocket-message-type", "subscribe-channel")});
        instance.setBody(String.format("{\"channel\":\"%s\"}", channel));
        return instance;
    }

    public static RESTfulInputMessage newUnsubscribeChannelMessage(String uuid, String channel) {
        final RESTfulInputMessage instance = new RESTfulInputMessage();
        instance.setUuid(uuid);
        instance.setMethod("POST");
        instance.setHeaders(new Pair[]{new Pair("x-everrest-websocket-message-type", "unsubscribe-channel")});
        instance.setBody(String.format("{\"channel\":\"%s\"}", channel));
        return instance;
    }

    private String method;
    private String path;
    private Pair[] headers;

    public RESTfulInputMessage() {
    }

    /**
     * Get name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
     *
     * @return name of HTTP method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Set name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
     *
     * @param method
     *         name of HTTP method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Get resource path.
     *
     * @return resource path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set resource path.
     *
     * @param path
     *         resource path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get HTTP headers.
     *
     * @return HTTP headers
     */
    public Pair[] getHeaders() {
        return headers;
    }

    /**
     * Set HTTP headers.
     *
     * @param headers
     *         HTTP headers
     */
    public void setHeaders(Pair[] headers) {
        this.headers = headers;
    }
}
