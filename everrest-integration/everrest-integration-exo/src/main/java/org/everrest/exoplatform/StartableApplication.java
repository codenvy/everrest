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
package org.everrest.exoplatform;

import org.everrest.core.Filter;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.method.MethodInvokerFilter;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Startable;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Purpose of this component is deliver all JAX-RS components in ExoContainer (instances of classes annotated
 * with {@link Path}, {@link Provider} and {@link Filter}). All components considered as singleton Resources and
 * Providers, see {@link #getSingletons()}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class StartableApplication extends Application implements Startable {
    private final ExoContainer container;

    private final Set<Class<?>> cls = Collections.emptySet();

    private final Set<Object> singletons = new HashSet<Object>();

    public StartableApplication(ExoContainerContext containerContext) {
        container = containerContext.getContainer();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<?>> getClasses() {
        return cls;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void start() {
        Collection adapters = container.getComponentAdapters();
        if (adapters != null && !adapters.isEmpty()) {
            // Assume all components loaded from ExoContainer are singleton (it is common behavior for ExoContainer).
            // If need more per-request component then use javax.ws.rs.core.Application for deploy.
            for (Object o : adapters) {
                ComponentAdapter componentAdapter = (ComponentAdapter)o;
                Class clazz = componentAdapter.getComponentImplementation();
                if (clazz.getAnnotation(Provider.class) != null) {
                    if (ContextResolver.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                    if (ExceptionMapper.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                    if (MessageBodyReader.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                    if (MessageBodyWriter.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                } else if (clazz.getAnnotation(Filter.class) != null) {
                    if (MethodInvokerFilter.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                    if (RequestFilter.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                    if (ResponseFilter.class.isAssignableFrom(clazz)) {
                        singletons.add(componentAdapter.getComponentInstance(container));
                    }
                } else if (clazz.getAnnotation(Path.class) != null) {
                    singletons.add(componentAdapter.getComponentInstance(container));
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
    }
}
