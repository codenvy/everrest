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
package org.everrest.core.impl;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.Lifecycle;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.UnhandledException;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.impl.uri.UriComponent;
import org.everrest.core.util.Logger;
import org.everrest.core.util.Tracer;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author andrew00x
 */
public final class EverrestProcessor implements Lifecycle {
    private static final Logger LOG = Logger.getLogger(EverrestProcessor.class.getName());

    private final ResourceBinder     resources;
    private final ProviderBinder     providers;
    private final DependencySupplier dependencySupplier;
    private final RequestHandler     requestHandler;
    private final Deployer           deployer;

    private final boolean                       normalizeUriFeature;
    private final boolean                       httpMethodOverrideFeature;
    private final int                           maxBufferSize;
    private final MethodInvokerDecoratorFactory methodInvokerDecoratorFactory;

    /**
     * Application properties. Properties from this map will be copied to ApplicationContext and may be accessible via method {@link
     * ApplicationContextImpl#getProperties()}.
     */
    private final Map<String, String> properties;

    public EverrestProcessor(ResourceBinder resources, ProviderBinder providers, DependencySupplier dependencySupplier,
                             EverrestConfiguration config, Application application) {
        this.resources = resources;
        this.providers = providers;
        this.dependencySupplier = dependencySupplier;
        this.requestHandler = new RequestHandlerImpl(new RequestDispatcher(resources));
        properties = new ConcurrentHashMap<>();

        if (config == null) {
            config = new EverrestConfiguration();
        }

        httpMethodOverrideFeature = config.isHttpMethodOverride();
        normalizeUriFeature = config.isNormalizeUri();
        maxBufferSize = config.getMaxBufferSize();
        String decoratorFactoryClassName = (String)config.getProperty(EverrestConfiguration.METHOD_INVOKER_DECORATOR_FACTORY);
        if (decoratorFactoryClassName != null) {
            try {
                methodInvokerDecoratorFactory = MethodInvokerDecoratorFactory.class.cast(
                        Thread.currentThread().getContextClassLoader().loadClass(decoratorFactoryClassName).newInstance());
            } catch (Exception e) {
                throw new IllegalStateException("Cannot instantiate '" + decoratorFactoryClassName + "', : " + e, e);
            }
        } else {
            methodInvokerDecoratorFactory = null;
        }

        deployer = new Deployer(resources, providers);

        if (application != null) {
            deployer.addApplication(application);
        }
    }

    public EverrestProcessor(ResourceBinder resources, ProviderBinder providers, DependencySupplier dependencySupplier) {
        this(resources, providers, dependencySupplier, null, null);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, String value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    public void process(GenericContainerRequest request, GenericContainerResponse response, EnvironmentContext envCtx)
            throws UnhandledException, IOException {

        EnvironmentContext.setCurrent(envCtx);

        ApplicationContextImpl context = null;
        try {
            context = new ApplicationContextImpl(request, response, providers, methodInvokerDecoratorFactory);
            context.getProperties().putAll(properties);
            context.setDependencySupplier(dependencySupplier);
            context.getAttributes().put(EverrestConfiguration.EVERREST_MAX_BUFFER_SIZE, maxBufferSize);
            context.setApplication(deployer);
            context.start();
            ApplicationContextImpl.setCurrent(context);

            if (normalizeUriFeature) {
                request.setUris(UriComponent.normalize(request.getRequestUri()), request.getBaseUri());
            }

            if (httpMethodOverrideFeature) {
                String method = request.getRequestHeaders().getFirst(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE);
                if (method != null) {
                    if (Tracer.isTracingEnabled()) {
                        Tracer.trace("Override HTTP method from \"X-HTTP-Method-Override\" header "
                                     + request.getMethod() + " => " + method);
                    }

                    request.setMethod(method);
                }
            }

            requestHandler.handleRequest(request, response);

        } finally {
            try {
                if (context != null) {
                    context.stop();
                }
            } finally {
                ApplicationContextImpl.setCurrent(null);
            }
            EnvironmentContext.setCurrent(null);
        }
    }

    public void addApplication(Application application) {
        if (application == null) {
            throw new NullPointerException("application");
        }
        deployer.addApplication(application);
    }

    /** @see org.everrest.core.Lifecycle#start() */
    @Override
    public void start() {
    }

    /** @see org.everrest.core.Lifecycle#stop() */
    @Override
    public void stop() {
        deployer.stop();
    }

    private static class Deployer extends EverrestApplication {
        final ApplicationPublisher        publisher;
        final List<WeakReference<Object>> singletonsReferences;

        Deployer(ResourceBinder resources, ProviderBinder providers) {
            publisher = new ApplicationPublisher(resources, providers);
            singletonsReferences = new ArrayList<>();
        }

        @Override
        public void addApplication(Application application) {
            super.addApplication(application);
            publisher.publish(application);
            Set<Object> singletons = application.getSingletons();
            if (singletons != null && singletons.size() > 0) {
                for (Object o : singletons) {
                    singletonsReferences.add(new WeakReference<>(o));
                }
            }
        }

        void stop() {
            for (WeakReference<Object> ref : singletonsReferences) {
                Object o = ref.get();
                if (o != null) {
                    try {
                        new LifecycleComponent(o).destroy();
                    } catch (InternalException e) {
                        LOG.error("Unable to destroy component. ", e);
                    }
                }
            }
            singletonsReferences.clear();
        }
    }

    public ProviderBinder getProviders() {
        return providers;
    }

    public ResourceBinder getResources() {
        return resources;
    }
}
