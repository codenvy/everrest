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

import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.core.util.Logger;
import org.everrest.exoplatform.ExoDependencySupplier;
import org.everrest.exoplatform.StartableApplication;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.net.MalformedURLException;

/**
 * EverrestExoContextListener primarily intended for using in standalone web applications that uses ExoContainer.
 * ExoContainer used for injecting requested dependencies in Resources and Providers. If method
 * {@link #getContainer(ServletContext)} returns instance of ExoContainer (not null) then it used for lookup JAX-RS
 * components (instances of classes annotated with {@link Path}, {@link Provider} and {@link Filter}).
 *
 * @author andrew00x
 */
public abstract class EverrestExoContextListener implements ServletContextListener {
    /**
     * Default EverrestExoContextListener implementation. It gets application's FQN from context-param
     * <i>javax.ws.rs.Application</i> and instantiate it. If such parameter is not specified then scan (if scanning is
     * enabled) web application's folders WEB-INF/classes and WEB-INF/lib for classes which contains JAX-RS annotations.
     * Interesting for three annotations {@link Path}, {@link Provider} and {@link Filter}. Scanning of JAX-RS
     * components is managed by context-param <i>org.everrest.scan.components</i>. This parameter must be <i>true</i> to enable scanning.
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
     * as described <a href="http://platform30.demo.exoplatform.org/docs/refguide/html/ch-service-configuration-for-beginners.html">here</a>.
     * Additionally this implementation do the same work as {@link DefaultListener}.
     */
    public static class StandaloneContainerStarter extends EverrestExoContextListener {
        public static final String CONFIGURATION_PATH = "everrest.exoplatform.standalone.config";

        public static final String PREFIX_WAR = "war:";

        private static final Logger LOG = Logger.getLogger(StandaloneContainerStarter.class);

        private StandaloneContainer container;

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

        public void contextDestroyed(ServletContextEvent sce) {
            if (container != null) {
                container.stop();
            }
            super.contextDestroyed(sce);
        }
    }

   /* ================================================================================ */

    @Override
    public final void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        EverrestServletContextInitializer everrestInitializer = new EverrestServletContextInitializer(servletContext);
        ResourceBinderImpl resources = new ResourceBinderImpl();
        ApplicationProviderBinder providers = new ApplicationProviderBinder();
        DependencySupplier dependencySupplier = new ExoDependencySupplier();
        EverrestConfiguration config = everrestInitializer.getConfiguration();
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
        ExoContainer container = getContainer(servletContext);
        if (container != null) {
            StartableApplication startable = (StartableApplication)container.getComponentInstanceOfType(StartableApplication.class);
            if (startable == null) {
                container.registerComponentImplementation(StartableApplication.class);
                startable = (StartableApplication)container.getComponentInstanceOfType(StartableApplication.class);
            }
            everrest.addApplication(startable);
        }
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencySupplier, config, everrest);
        processor.start();

        servletContext.setAttribute(EverrestConfiguration.class.getName(), config);
        servletContext.setAttribute(Application.class.getName(), everrest);
        servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
        servletContext.setAttribute(ResourceBinder.class.getName(), resources);
        servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
        servletContext.setAttribute(EverrestProcessor.class.getName(), processor);
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
