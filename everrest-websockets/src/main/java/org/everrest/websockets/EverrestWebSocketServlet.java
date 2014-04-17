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

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.tools.SimplePrincipal;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.everrest.websockets.message.JsonMessageConverter;
import org.everrest.websockets.message.MessageConverter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author andrew00x
 */
public class EverrestWebSocketServlet extends WebSocketServlet {
    public static final String EVERREST_PROCESSOR_ATTRIBUTE = EverrestProcessor.class.getName();
    public static final String MESSAGE_CONVERTER_ATTRIBUTE  = MessageConverter.class.getName();

    private static final AtomicLong sequence = new AtomicLong(1);

    private EverrestProcessor           processor;
    private MessageConverter            messageConverter;
    private WebApplicationDeclaredRoles webApplicationRoles;
    private Executor executor;

    @Override
    public void init() throws ServletException {
        processor = getEverrestProcessor();
        messageConverter = getMessageConverter();
        if (messageConverter == null) {
            messageConverter = new JsonMessageConverter();
        }
        webApplicationRoles = new WebApplicationDeclaredRoles(getServletContext());
        executor = getExecutor();
    }

    @Override
    protected StreamInbound createWebSocketInbound(String s, HttpServletRequest req) {
        WSConnectionImpl connection = WSConnectionContext.open(req.getSession(), messageConverter);
        connection.registerMessageReceiver(new WS2RESTAdapter(connection, createSecurityContext(req), processor, executor));
        return connection;
    }

    protected EverrestProcessor getEverrestProcessor() {
        return (EverrestProcessor)getServletContext().getAttribute(EVERREST_PROCESSOR_ATTRIBUTE);
    }

    protected MessageConverter getMessageConverter() {
        return (MessageConverter)getServletContext().getAttribute(MESSAGE_CONVERTER_ATTRIBUTE);
    }

    protected Executor getExecutor() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r, "everrest.WebSocket" + sequence.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    protected SecurityContext createSecurityContext(HttpServletRequest req) {
        Principal principal = req.getUserPrincipal();
        if (principal == null) {
            return new SimpleSecurityContext(req.isSecure());
        }
        Set<String> userRoles = new LinkedHashSet<>();
        for (String declaredRole : webApplicationRoles.getDeclaredRoles()) {
            if (req.isUserInRole(declaredRole)) {
                userRoles.add(declaredRole);
            }
        }
        return new SimpleSecurityContext(new SimplePrincipal(principal.getName()), userRoles, req.getAuthType(), req.isSecure());
    }
}
