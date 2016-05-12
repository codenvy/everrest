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
package org.everrest.core.util;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerResponse;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Collector for trace messages. This class designed for internal usage only. Regular users of EverRest framework are
 * not expected to use this class directly.
 * <p/>
 * To turn on the tracing feature client must send query parameter {@code tracing=true}.
 * Trace messages added by method {@code trace}. All collected messages will be sent to client as headers. Each
 * trace message is represented as separate HTTP header. The name of header has next pattern
 * {@code EverRest-Trace-XXX}, where XXX is number of message.
 *
 * @author andrew00x
 */
public final class Tracer {
    /**
     * Check is tracing feature enabled.
     *
     * @return {@code true} if tracing enabled and {@code false} otherwise.
     */
    public static boolean isTracingEnabled() {
        ApplicationContext context = ApplicationContext.getCurrent();
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not initialized yet. ");
        }
        return Boolean.parseBoolean(context.getQueryParameters().getFirst("tracing"));
    }

    /**
     * Add trace message.
     *
     * @param message
     *         the trace message
     */
    public static void trace(String message) {
        if (isTracingEnabled()) {
            getTraceHolder().addTrace(message);
        }
    }

    /**
     * Add trace message.
     *
     * @param format
     *         the trace message's format
     * @param args
     *         the arguments for string format
     */
    public static void trace(String format, Object... args) {
        trace(String.format(format, args));
    }

    /**
     * Add all collected trace messages to specified instance of {@code response} as HTTP headers.
     * This method must be invoked at the end of request lifecycle.
     *
     * @param response
     *         the response for adding headers
     */
    public static void addTraceHeaders(GenericContainerResponse response) {
        if (isTracingEnabled()) {
            getTraceHolder().addTraceHeaders(response);
        }
    }

    private static TraceHolder getTraceHolder() {
        ApplicationContext context = ApplicationContext.getCurrent();
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not initialized yet. ");
        }
        TraceHolder t = (TraceHolder)context.getAttributes().get("tracer");
        if (t == null) {
            t = new TraceHolder();
            context.getAttributes().put("tracer", t);
        }
        return t;
    }

    static class TraceHolder {
        private final List<String> traces = new ArrayList<>();

        void addTrace(String message) {
            traces.add(message);
        }

        void addTraceHeaders(GenericContainerResponse response) {
            int i = 1;
            for (String message : getTraceHolder().traces) {
                response.getHttpHeaders().add(format("EverRest-Trace-%03d", i++), message);
            }
        }
    }
}
