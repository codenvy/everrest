/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
 * @author andrew00x
 */
@Provider
public class ExoAsynchronousJobPool extends AsynchronousJobPool implements ContextResolver<AsynchronousJobPool> {
    public ExoAsynchronousJobPool(EverrestConfiguration config) {
        super(config);
    }

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
