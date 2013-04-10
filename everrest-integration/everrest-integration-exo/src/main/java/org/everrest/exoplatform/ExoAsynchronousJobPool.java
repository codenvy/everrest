/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform;

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Provider
public class ExoAsynchronousJobPool extends AsynchronousJobPool implements ContextResolver<AsynchronousJobPool> {
    public ExoAsynchronousJobPool(EverrestConfiguration config) {
        super(config);
    }

    /**
     * @see org.everrest.core.impl.async.AsynchronousJobPool#newCallable(java.lang.Object, java.lang.reflect.Method,
     *      java.lang.Object[])
     */
    @Override
    protected Callable<Object> newCallable(Object resource, Method method, Object[] params) {
        return new CallableWrapper(super.newCallable(resource, method, params));
    }

    private static class CallableWrapper implements Callable<Object> {
        private final ConversationState state;
        private final Callable<Object>  callable;

        public CallableWrapper(Callable<Object> callable) {
            this.callable = callable;
            state = ConversationState.getCurrent();
        }

        @Override
        public Object call() throws Exception {
            ConversationState.setCurrent(state == null
                                         ? new ConversationState(new Identity(IdentityConstants.ANONIM)) : state);
            try {
                return callable.call();
            } finally {
                ConversationState.setCurrent(null);
            }
        }
    }
}
