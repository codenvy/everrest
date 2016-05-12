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
package org.everrest.pico;

import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.web.WebappComposer;
import org.picocontainer.web.script.ScriptedWebappComposer;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.Collection;

/**
 * Register components of containers with different webapp scopes (application, session, request) in EverRest framework if they are
 * annotated with &#64;Path, &#64;Provider or &#64;Filter annotation.
 *
 * @author andrew00x
 * @see WebappComposer
 */
public class EverrestComposer implements WebappComposer {
    private static final class PicoEverrestProcessorWrapper implements Startable {
        private final EverrestProcessor processor;

        private PicoEverrestProcessorWrapper(EverrestProcessor processor) {
            this.processor = processor;
        }

        @Override
        public void start() {
            processor.start();
        }

        @Override
        public void stop() {
            processor.stop();
        }
    }

    private static final class PicoFileCollectorDestroyer implements Disposable {
        private final FileCollectorDestroyer fileCollectorDestroyer;

        public PicoFileCollectorDestroyer(FileCollectorDestroyer fileCollectorDestroyer) {
            this.fileCollectorDestroyer = fileCollectorDestroyer;
        }

        @Override
        public void dispose() {
            fileCollectorDestroyer.stopFileCollector();
        }
    }

    private ScriptedWebappComposer scriptedComposer;
    private String                 sessionScript;
    private String                 requestScript;

    protected EverrestProcessor processor;

    @Override
    public final void composeApplication(MutablePicoContainer container, ServletContext servletContext) {
        String applicationScript = servletContext.getInitParameter("application-script");
        if (applicationScript == null) {
            applicationScript = ScriptedWebappComposer.DEFAULT_APPLICATION_SCRIPT;
        }

        sessionScript = servletContext.getInitParameter("session-script");
        if (sessionScript == null) {
            sessionScript = ScriptedWebappComposer.DEFAULT_SESSION_SCRIPT;
        }

        requestScript = servletContext.getInitParameter("request-script");
        if (requestScript == null) {
            requestScript = ScriptedWebappComposer.DEFAULT_REQUEST_SCRIPT;
        }

        String containerBuilder = servletContext.getInitParameter("scripted-container-builder");
        if (containerBuilder == null) {
            containerBuilder = ScriptedWebappComposer.DEFAULT_CONTAINER_BUILDER;
        }

        scriptedComposer = new ScriptedWebappComposer(containerBuilder, applicationScript, sessionScript, requestScript);

        if (isResourceAvailable(applicationScript)) {
            scriptedComposer.composeApplication(container, servletContext);
        }

        EverrestServletContextInitializer everrestInitializer = new EverrestServletContextInitializer(servletContext);
        RequestHandler requestHandler = container.getComponent(RequestHandler.class);
        EverrestConfiguration config = container.getComponent(EverrestConfiguration.class);
        DependencySupplier dependencySupplier = container.getComponent(DependencySupplier.class);
        ResourceBinder resources;
        ApplicationProviderBinder providers;

        if (requestHandler == null) {
            resources = container.getComponent(ResourceBinder.class);
            providers = container.getComponent(ApplicationProviderBinder.class);

            if (resources == null) {
                resources = new ResourceBinderImpl();
                container.addComponent(ResourceBinder.class, resources);
            }

            if (providers == null) {
                providers = new ApplicationProviderBinder();
                container.addComponent(ApplicationProviderBinder.class, providers);
            }
            requestHandler = new RequestHandlerImpl(new RequestDispatcher(resources), providers);
            container.addComponent(RequestHandler.class, requestHandler);
        } else {
            resources = requestHandler.getResources();
            providers = (ApplicationProviderBinder)requestHandler.getProviders();
        }


        if (config == null) {
            config = everrestInitializer.createConfiguration();
            container.addComponent(EverrestConfiguration.class, config);
        }

        if (dependencySupplier == null) {
            dependencySupplier = new PicoDependencySupplier();
            container.addComponent(DependencySupplier.class, dependencySupplier);
        }

        Application application = everrestInitializer.getApplication();
        EverrestApplication everrest = new EverrestApplication();
        if (config.isAsynchronousSupported()) {
            everrest.addResource(config.getAsynchronousServicePath(), AsynchronousJobService.class);
            everrest.addSingleton(new AsynchronousJobPool(config));
            everrest.addSingleton(new AsynchronousProcessListWriter());
        }
        if (config.isCheckSecurity()) {
            everrest.addSingleton(new SecurityConstraint());
        }
        everrest.addApplication(application);

        doComposeApplication(container, servletContext);
        processComponents(container, everrest);

        processor = new EverrestProcessor(config, dependencySupplier, requestHandler, everrest);
        container.addComponent(new PicoEverrestProcessorWrapper(processor));
        container.addComponent(new PicoFileCollectorDestroyer(makeFileCollectorDestroyer()));

        servletContext.setAttribute(EverrestConfiguration.class.getName(), config);
        servletContext.setAttribute(Application.class.getName(), everrest);
        servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
        servletContext.setAttribute(ResourceBinder.class.getName(), resources);
        servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
        servletContext.setAttribute(EverrestProcessor.class.getName(), processor);
    }

    protected FileCollectorDestroyer makeFileCollectorDestroyer() {
        return new FileCollectorDestroyer();
    }

    @Override
    public final void composeSession(MutablePicoContainer container) {
        if (isResourceAvailable(sessionScript)) {
            scriptedComposer.composeSession(container);
        }

        doComposeSession(container);
        EverrestApplication sesEverrest = new EverrestApplication();
        processComponents(container, sesEverrest);
        processor.addApplication(sesEverrest);
    }

    @Override
    public final void composeRequest(MutablePicoContainer container) {
        if (isResourceAvailable(requestScript)) {
            scriptedComposer.composeRequest(container);
        }

        doComposeRequest(container);
        EverrestApplication reqEverrest = new EverrestApplication();
        processComponents(container, reqEverrest);
        processor.addApplication(reqEverrest);
    }

    private boolean isResourceAvailable(String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(resource) != null;
    }

    /**
     * Compose components with application scope.
     * <p/>
     * <pre>
     * // Do this if need to keep default everrest framework behaviour.
     * processor.addApplication(everrestInitializer.getApplication());
     * // Register components in picocontainer.
     * container.addComponent(MyApplicationScopeResource.class);
     * container.addComponent(MyApplicationScopeProvider.class);
     * </pre>
     *
     * @param container
     *         picocontainer
     * @param servletContext
     *         servlet context
     */
    protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext) {
    }

    /**
     * Compose components with request scope.
     * <p/>
     * <pre>
     * container.addComponent(MyRequestScopeResource.class);
     * container.addComponent(MyRequestScopeProvider.class);
     * </pre>
     *
     * @param container
     *         picocontainer
     */
    protected void doComposeRequest(MutablePicoContainer container) {
    }

    /**
     * Compose components with session scope.
     * <p/>
     * <pre>
     * container.addComponent(MySessionScopeResource.class);
     * container.addComponent(MySessionScopeProvider.class);
     * </pre>
     *
     * @param container
     *         picocontainer
     */
    protected void doComposeSession(MutablePicoContainer container) {
    }

    protected void processComponents(MutablePicoContainer container, EverrestApplication everrest) {
        Collection<ComponentAdapter<?>> adapters = container.getComponentAdapters();
        for (ComponentAdapter<?> adapter : adapters) {
            Class<?> clazz = adapter.getComponentImplementation();
            if (clazz.isAnnotationPresent(Provider.class)) {
                ProviderDescriptor providerDescriptor = new ProviderDescriptorImpl(clazz);
                everrest.addFactory(new PicoObjectFactory<>(providerDescriptor));
            } else if (clazz.isAnnotationPresent(Filter.class)) {
                FilterDescriptor filterDescriptor = new FilterDescriptorImpl(clazz);
                everrest.addFactory(new PicoObjectFactory<>(filterDescriptor));
            } else if (clazz.isAnnotationPresent(Path.class)) {
                ResourceDescriptor resourceDescriptor = new AbstractResourceDescriptor(clazz);
                everrest.addFactory(new PicoObjectFactory<>(resourceDescriptor));
            }
        }
    }
}
