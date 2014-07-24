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
package org.everrest.exoplatform.servlet;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResponseFilter;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.servlet.EverrestApplication;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.core.util.Logger;
import org.everrest.exoplatform.ExoDependencySupplier;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;
import org.picocontainer.ComponentAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.net.MalformedURLException;
import java.util.Collection;

/**
 * EverrestExoContextListener primarily intended for using in standalone web applications that uses ExoContainer.
 * ExoContainer used for injecting requested dependencies in Resources and Providers. If method
 * {@link #getContainer(ServletContext)} returns instance of ExoContainer (not null) then it used for lookup JAX-RS
 * components (instances of classes annotated with {@link Path}, {@link Provider} and {@link Filter}).
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class EverrestExoContextListener implements ServletContextListener {
    /**
     * Default EverrestExoContextListener implementation. It gets application's FQN from context-param
     * <i>javax.ws.rs.Application</i> and instantiate it. If such parameter is not specified then scan (if scanning is
     * enabled) web application's folders WEB-INF/classes and WEB-INF/lib for classes which contains JAX-RS annotations.
     * Interesting for three annotations {@link Path}, {@link Provider} and {@link Filter}. Scanning of JAX-RS
     * components
     * is managed by context-param <i>org.everrest.scan.components</i>. This parameter must be <i>true</i> to enable
     * scanning.
     */
    public static class DefaultListener extends EverrestExoContextListener {
        /** @see org.everrest.exoplatform.servlet.EverrestExoContextListener#getContainer(javax.servlet.ServletContext) */
        @Override
        protected ExoContainer getContainer(ServletContext servletContext) {
            return null;
        }
    }

    /**
     * Implementation of EverrestExoContextListener which get path to StandaloneContainer configuration from
     * context-param <i>everrest.exoplatform.standalone.config</i>. If path to configuration found then this path added
     * in to StandaloneContainer configuration. All other configuration located in war and jar files will be processed
     * as
     * described <a
     * href="http://platform30.demo.exoplatform.org/docs/refguide/html/ch-service-configuration-for-beginners.html"
     * >here</a>. Additionally this implementation do the same work as {@link DefaultListener}.
     */
    public static class StandaloneContainerStarter extends EverrestExoContextListener {
        public static final String CONFIGURATION_PATH = "everrest.exoplatform.standalone.config";

        public static final String PREFIX_WAR = "war:";

        private static final Logger LOG = Logger.getLogger(StandaloneContainerStarter.class);

        private StandaloneContainer container;

        /** @see org.everrest.exoplatform.servlet.EverrestExoContextListener#getContainer(javax.servlet.ServletContext) */
        @Override
        protected ExoContainer getContainer(ServletContext servletContext) {
            String configurationURL = servletContext.getInitParameter(CONFIGURATION_PATH);

            if (configurationURL != null) {
                if (configurationURL.startsWith(PREFIX_WAR)) {
                    try {
                        configurationURL =
                                servletContext.getResource(configurationURL.substring(PREFIX_WAR.length())).toExternalForm();
                    } catch (MalformedURLException e) {
                        LOG.error("Error of configurationURL read. ", e);
                    }
                }
            } else {
                configurationURL = System.getProperty(CONFIGURATION_PATH);
            }

            try {
                StandaloneContainer.addConfigurationURL(configurationURL);
            } catch (MalformedURLException e1) {
                try {
                    StandaloneContainer.addConfigurationPath(configurationURL);
                } catch (MalformedURLException e2) {
                    LOG.error("Error of addConfiguration. ", e2);
                }
            }

            try {
                container = StandaloneContainer.getInstance(Thread.currentThread().getContextClassLoader());
            } catch (Exception e) {
                LOG.error("Error of StandaloneContainer initialization. ", e);
            }

            return container;
        }

        /** {@inheritDoc} */
        public void contextDestroyed(ServletContextEvent sce) {
            if (container != null) {
                container.stop();
            }
            super.contextDestroyed(sce);
        }
    }

   /* ================================================================================ */

    protected ResourceBinderImpl resources;

    protected ApplicationProviderBinder providers;

    /** @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent) */
    @Override
    public final void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        EverrestServletContextInitializer everrestInitializer = new EverrestServletContextInitializer(servletContext);
        this.resources = new ResourceBinderImpl();
        this.providers = new ApplicationProviderBinder();
        DependencySupplier dependencySupplier = new ExoDependencySupplier();
        EverrestConfiguration config = everrestInitializer.getConfiguration();
        Application application = everrestInitializer.getApplication();
        EverrestApplication everrest = new EverrestApplication(config);
        everrest.addApplication(application);
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencySupplier, config, everrest);
        processor.start();

        servletContext.setAttribute(EverrestConfiguration.class.getName(), config);
        servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
        servletContext.setAttribute(ResourceBinder.class.getName(), resources);
        servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
        servletContext.setAttribute(EverrestProcessor.class.getName(), processor);

        processComponents(servletContext);
    }

    /**
     * @param servletContext
     *         ServletContext.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void processComponents(ServletContext servletContext) {
        ExoContainer container = getContainer(servletContext);
        if (container != null) {
            Collection adapters = container.getComponentAdapters();
            if (adapters != null && !adapters.isEmpty()) {
                ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
                // Assume all components loaded from ExoContainer are singleton (it is common behavior for ExoContainer).
                // If need more per-request component then use javax.ws.rs.core.Application for deploy.
                ComponentLifecycleScope lifeCycle = ComponentLifecycleScope.SINGLETON;

                for (Object o : adapters) {
                    ComponentAdapter componentAdapter = (ComponentAdapter)o;

                    Class clazz = componentAdapter.getComponentImplementation();
                    if (clazz.isAnnotationPresent(Provider.class)) {
                        ProviderDescriptor pDescriptor = new ProviderDescriptorImpl(clazz, lifeCycle);
                        pDescriptor.accept(rdv);

                        if (ContextResolver.class.isAssignableFrom(clazz)) {
                            providers.addContextResolver(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                                                                                                        componentAdapter
                                                                                                                .getComponentInstance(
                                                                                                                        container)));
                        }
                        if (ExceptionMapper.class.isAssignableFrom(clazz)) {
                            providers.addExceptionMapper(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor, componentAdapter
                                    .getComponentInstance(container)));
                        }
                        if (MessageBodyReader.class.isAssignableFrom(clazz)) {
                            providers.addMessageBodyReader(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                                                                                                          componentAdapter
                                                                                                                  .getComponentInstance(
                                                                                                                          container)));
                        }
                        if (MessageBodyWriter.class.isAssignableFrom(clazz)) {
                            providers.addMessageBodyWriter(new SingletonObjectFactory<ProviderDescriptor>(pDescriptor,
                                                                                                          componentAdapter
                                                                                                                  .getComponentInstance(
                                                                                                                          container)));
                        }
                    } else if (clazz.isAnnotationPresent(Filter.class)) {
                        FilterDescriptorImpl fDescriptor = new FilterDescriptorImpl(clazz, lifeCycle);
                        fDescriptor.accept(rdv);

                        if (MethodInvokerFilter.class.isAssignableFrom(clazz)) {
                            providers.addMethodInvokerFilter(new SingletonObjectFactory<FilterDescriptor>(fDescriptor,
                                                                                                          componentAdapter
                                                                                                                  .getComponentInstance(
                                                                                                                          container)));
                        }
                        if (RequestFilter.class.isAssignableFrom(clazz)) {
                            providers.addRequestFilter(new SingletonObjectFactory<FilterDescriptor>(fDescriptor, componentAdapter
                                    .getComponentInstance(container)));
                        }
                        if (ResponseFilter.class.isAssignableFrom(clazz)) {
                            providers.addResponseFilter(new SingletonObjectFactory<FilterDescriptor>(fDescriptor, componentAdapter
                                    .getComponentInstance(container)));
                        }
                    } else if (clazz.isAnnotationPresent(Path.class)) {
                        AbstractResourceDescriptor rDescriptor = new AbstractResourceDescriptorImpl(clazz, lifeCycle);
                        rDescriptor.accept(rdv);

                        resources.addResource(new SingletonObjectFactory<AbstractResourceDescriptor>(rDescriptor, componentAdapter
                                .getComponentInstance(container)));
                    }
                }
            }
        }
    }

    /**
     * Get ExoContainer instance. Typically instance of container is used for look up classes which annotated with
     * &#064;javax.ws.rs.Path, &#064;javax.ws.rs.ext.Provider, &#064;org.everrest.core.Filter annotations or subclasses
     * of javax.ws.rs.core.Application from it. If not need to load any components from ExoContainer this method must
     * return <code>null</code>.
     *
     * @param servletContext
     *         servlet context
     * @return ExoContainer instance or <code>null</code>
     */
    protected abstract ExoContainer getContainer(ServletContext servletContext);

    /** @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent) */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        makeFileCollectorDestroyer().stopFileCollector();
        ServletContext servletContext = sce.getServletContext();
        EverrestProcessor processor = (EverrestProcessor)servletContext.getAttribute(EverrestProcessor.class.getName());
        if (processor != null) {
            processor.stop();
        }
    }

    protected FileCollectorDestroyer makeFileCollectorDestroyer() {
        return new FileCollectorDestroyer();
    }
}
