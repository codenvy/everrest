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
package org.everrest.core.util;

import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.impl.ApplicationContextImpl;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Collector for trace messages. This class designed for internal usage only. Regular users of EverRest framework are
 * not expected to use this class directly.
 * <p/>
 * To turn on the tracing feature client must send query parameter <code>tracing=true</code>.
 * Trace messages added by method <code>trace</code>. All collected messages will be sent to client as headers. Each
 * trace message is represented as separate HTTP header. The name of header has next pattern
 * <code>EverRest-Trace-XXX</code>, where XXX is number of message.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class Tracer {
    /**
     * Check is tracing feature enabled.
     *
     * @return <code>true</code> if tracing enabled and <code>false</code> otherwise.
     */
    public static boolean isTracingEnabled() {
        ApplicationContext context = ApplicationContextImpl.getCurrent();
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
     * Add all collected trace messages to specified instance of <code>response</code> as HTTP headers.
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
        ApplicationContext context = ApplicationContextImpl.getCurrent();
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

    private static final class TraceHolder {
        private final List<String> traces = new ArrayList<String>();

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
