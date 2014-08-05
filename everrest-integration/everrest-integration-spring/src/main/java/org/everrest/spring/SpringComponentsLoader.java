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
package org.everrest.spring;

import org.everrest.core.ApplicationContext;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.InitialProperties;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.FileCollectorDestroyer;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This loader registers any bean annotated with &#64;Path, &#64;Provider or &#64;Filter in the EverRest framework.
 *
 * @author andrew00x
 */
public class SpringComponentsLoader implements BeanFactoryPostProcessor, HandlerMapping {
    private static abstract class _Lifecycle implements org.springframework.context.Lifecycle {
        private final AtomicBoolean flag = new AtomicBoolean(false);

        @Override
        public final void start() {
            doStart();
            flag.set(true);
        }

        @Override
        public final void stop() {
            doStop();
            flag.set(false);
        }

        @Override
        public final boolean isRunning() {
            return flag.get();
        }

        void doStart() {
        }

        void doStop() {
        }
    }

    private static final class SpringEverrestProcessorLifecycle extends _Lifecycle {
        private final EverrestProcessor processor;

        private SpringEverrestProcessorLifecycle(EverrestProcessor processor) {
            this.processor = processor;
        }

        @Override
        void doStart() {
            processor.start();
        }

        @Override
        void doStop() {
            processor.stop();
        }
    }

    private static final class SpringFileCollectorDestroyer extends _Lifecycle {
        private final FileCollectorDestroyer fileCollectorDestroyer;

        public SpringFileCollectorDestroyer(FileCollectorDestroyer fileCollectorDestroyer) {
            this.fileCollectorDestroyer = fileCollectorDestroyer;
        }

        @Override
        void doStop() {
            fileCollectorDestroyer.stopFileCollector();
        }
    }

    protected ResourceBinder            resources;
    protected ApplicationProviderBinder providers;
    protected EverrestProcessor         processor;
    protected EverrestConfiguration     configuration;

    public SpringComponentsLoader(ResourceBinder resources, ApplicationProviderBinder providers,
                                  DependencySupplier dependencies) {
        this(resources, providers, new EverrestConfiguration(), dependencies);
    }

    public SpringComponentsLoader(ResourceBinder resources, ApplicationProviderBinder providers,
                                  EverrestConfiguration configuration, DependencySupplier dependencies) {
        this.resources = resources;
        this.providers = providers;
        this.configuration = configuration;
        this.processor = new EverrestProcessor(resources, providers, dependencies, configuration, null);
    }

    protected SpringComponentsLoader() {
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        return new HandlerExecutionChain(processor);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerSingleton("org.everrest.lifecycle.SpringEverrestProcessorLifecycle",
                                      new SpringEverrestProcessorLifecycle(processor));
        beanFactory.registerSingleton("org.everrest.lifecycle.SpringFileCollectorDestroyer",
                                      new SpringFileCollectorDestroyer(makeFileCollectorDestroyer()));
        EverrestApplication everrest = makeEverrestApplication();

        ResourceDescriptorValidator rdv = ResourceDescriptorValidator.getInstance();
        addAutowiredDependencies(beanFactory);
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            Class<?> beanClass = beanFactory.getType(beanName);
            if (beanClass.getAnnotation(Provider.class) != null) {
                ProviderDescriptor pDescriptor = new ProviderDescriptorImpl(beanClass);
                pDescriptor.accept(rdv);
                everrest.addFactory(new SpringObjectFactory<>(pDescriptor, beanName, beanFactory));
            } else if (beanClass.getAnnotation(Filter.class) != null) {
                FilterDescriptor fDescriptor = new FilterDescriptorImpl(beanClass);
                fDescriptor.accept(rdv);
                everrest.addFactory(new SpringObjectFactory<>(fDescriptor, beanName, beanFactory));
            } else if (beanClass.getAnnotation(Path.class) != null) {
                AbstractResourceDescriptor rDescriptor = new AbstractResourceDescriptorImpl(beanClass);
                rDescriptor.accept(rdv);
                everrest.addFactory(new SpringObjectFactory<>(rDescriptor, beanName, beanFactory));
            }
        }

        processor.addApplication(everrest);
    }

    protected FileCollectorDestroyer makeFileCollectorDestroyer() {
        return new FileCollectorDestroyer();
    }

    protected EverrestApplication makeEverrestApplication() {
        final EverrestApplication everrest = new EverrestApplication();
        if (configuration.isAsynchronousSupported()) {
            everrest.addResource(configuration.getAsynchronousServicePath(), AsynchronousJobService.class);
            everrest.addSingleton(new AsynchronousJobPool(configuration));
            everrest.addSingleton(new AsynchronousProcessListWriter());
        }
        if (configuration.isCheckSecurity()) {
            everrest.addSingleton(new SecurityConstraint());
        }
        return everrest;
    }

    /**
     * Add binding for HttpHeaders, InitialProperties, Request, SecurityContext, UriInfo. All this types will be
     * supported for injection in constructor or fields of component of Spring IoC container.
     *
     * @param beanFactory
     *         bean factory
     * @see org.springframework.beans.factory.annotation.Autowired
     */
    protected void addAutowiredDependencies(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.registerResolvableDependency(HttpHeaders.class, new ObjectFactory<HttpHeaders>() {
            @Override
            public HttpHeaders getObject() {
                ApplicationContext context = ApplicationContextImpl.getCurrent();
                if (context == null) {
                    throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
                }
                return context.getHttpHeaders();
            }
        });
        beanFactory.registerResolvableDependency(InitialProperties.class, new ObjectFactory<InitialProperties>() {
            @Override
            public InitialProperties getObject() {
                ApplicationContext context = ApplicationContextImpl.getCurrent();
                if (context == null) {
                    throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
                }
                return context.getInitialProperties();
            }
        });
        beanFactory.registerResolvableDependency(Request.class, new ObjectFactory<Request>() {
            @Override
            public Request getObject() {
                ApplicationContext context = ApplicationContextImpl.getCurrent();
                if (context == null) {
                    throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
                }
                return context.getRequest();
            }
        });
        beanFactory.registerResolvableDependency(SecurityContext.class, new ObjectFactory<SecurityContext>() {
            @Override
            public SecurityContext getObject() {
                ApplicationContext context = ApplicationContextImpl.getCurrent();
                if (context == null) {
                    throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
                }
                return context.getSecurityContext();
            }
        });
        beanFactory.registerResolvableDependency(UriInfo.class, new ObjectFactory<UriInfo>() {
            @Override
            public UriInfo getObject() {
                ApplicationContext context = ApplicationContextImpl.getCurrent();
                if (context == null) {
                    throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
                }
                return context.getUriInfo();
            }
        });
        beanFactory.registerResolvableDependency(javax.ws.rs.core.Application.class, new ObjectFactory<javax.ws.rs.core.Application>() {
            @Override
            public javax.ws.rs.core.Application getObject() {
                ApplicationContext context = ApplicationContextImpl.getCurrent();
                if (context == null) {
                    throw new IllegalStateException("EverRest ApplicationContext is not initialized.");
                }
                return context.getApplication();
            }
        });
    }

    protected ResourceBinder getResources() {
        return resources;
    }

    protected ApplicationProviderBinder getProviders() {
        return providers;
    }

    protected EverrestProcessor getProcessor() {
        return processor;
    }
}