/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.guice.servlet;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
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
import org.everrest.guice.BindingPath;
import org.everrest.guice.EverrestConfigurationModule;
import org.everrest.guice.EverrestModule;
import org.everrest.guice.GuiceDependencySupplier;
import org.everrest.guice.GuiceObjectFactory;
import org.everrest.guice.GuiceRuntimeDelegateImpl;

/** @author andrew00x */
public abstract class EverrestGuiceContextListener extends GuiceServletContextListener {
  /**
   * Default EverrestGuiceContextListener implementation. It gets application's FQN from
   * context-param <i>jakarta.ws.rs.Application</i> and instantiate it. If such parameter is not
   * specified then scan (if scanning is enabled) web application's folders WEB-INF/classes and
   * WEB-INF/lib for classes which contains JAX-RS annotations. Interesting for three annotations
   * {@link Path}, {@link Provider} and {@link Filter}. Scanning of JAX-RS components is managed by
   * contex-param <i>org.everrest.scan.components</i>. This parameter must be <i>true</i> to enable
   * scanning.
   */
  public static class DefaultListener extends EverrestGuiceContextListener {
    @Override
    protected List<Module> getModules() {
      return Collections.emptyList();
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);
    ServletContext servletContext = sce.getServletContext();
    ResourceBinderImpl resources = new ResourceBinderImpl();
    ApplicationProviderBinder providers = new ApplicationProviderBinder();
    Injector injector = getInjector(servletContext);
    DependencySupplier dependencySupplier = new GuiceDependencySupplier(injector);
    EverrestConfiguration config = injector.getInstance(EverrestConfiguration.class);
    EverrestServletContextInitializer everrestInitializer =
        new EverrestServletContextInitializer(servletContext);
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

    processBindings(injector, everrest);
    RequestDispatcher requestDispatcher = new RequestDispatcher(resources);
    RequestHandlerImpl requestHandler = new RequestHandlerImpl(requestDispatcher, providers);
    EverrestProcessor processor =
        new EverrestProcessor(config, dependencySupplier, requestHandler, everrest);
    processor.start();

    servletContext.setAttribute(EverrestConfiguration.class.getName(), config);
    servletContext.setAttribute(Application.class.getName(), everrest);
    servletContext.setAttribute(DependencySupplier.class.getName(), dependencySupplier);
    servletContext.setAttribute(ResourceBinder.class.getName(), resources);
    servletContext.setAttribute(ApplicationProviderBinder.class.getName(), providers);
    servletContext.setAttribute(EverrestProcessor.class.getName(), processor);
    // use specific RuntimeDelegate instance which is able to work with guice rest service proxies.
    // (need for interceptors functionality)
    RuntimeDelegate.setInstance(new GuiceRuntimeDelegateImpl());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    makeFileCollectorDestroyer().stopFileCollector();
    ServletContext sctx = sce.getServletContext();
    EverrestProcessor processor =
        (EverrestProcessor) sctx.getAttribute(EverrestProcessor.class.getName());
    if (processor != null) {
      processor.stop();
    }
  }

  protected FileCollectorDestroyer makeFileCollectorDestroyer() {
    return new FileCollectorDestroyer();
  }

  @Override
  protected final Injector getInjector() {
    return Guice.createInjector(Stage.PRODUCTION, createModules());
  }

  private List<Module> createModules() {
    List<Module> all = new ArrayList<>();
    ServletModule servletModule = getServletModule();
    if (servletModule != null) {
      all.add(servletModule);
    }
    all.add(new EverrestModule());
    all.add(new EverrestConfigurationModule());
    List<Module> modules = getModules();
    if (modules != null && modules.size() > 0) {
      all.addAll(modules);
    }
    return all;
  }

  /**
   * Implementation can provide set of own {@link Module} for JAX-RS components.
   *
   * <p>
   *
   * <pre>
   * protected List&lt;Module&gt; getModules()
   * {
   *    List&lt;Module&gt; modules = new ArrayList&lt;Module&gt;(1);
   *    modules.add(new Module()
   *    {
   *       public void configure(Binder binder)
   *       {
   *          binder.bind(MyResource.class);
   *          binder.bind(MyProvider.class);
   *       }
   *    });
   *    return modules;
   * }
   * </pre>
   *
   * @return JAX-RS modules
   */
  protected abstract List<Module> getModules();

  /**
   * Create servlet module. By default return module with one component GuiceEverrestServlet.
   *
   * @return ServletModule
   */
  protected ServletModule getServletModule() {
    return new ServletModule() {
      @Override
      protected void configureServlets() {
        serve("/*").with(GuiceEverrestServlet.class);
      }
    };
  }

  protected Injector getInjector(ServletContext servletContext) {
    return (Injector) servletContext.getAttribute(Injector.class.getName());
  }

  @SuppressWarnings({"unchecked"})
  protected void processBindings(Injector injector, EverrestApplication everrest) {
    for (Binding<?> binding : injector.getBindings().values()) {
      Key<?> bindingKey = binding.getKey();
      Type type = bindingKey.getTypeLiteral().getType();
      if (type instanceof Class) {
        Class clazz = (Class) type;
        if (clazz.getAnnotation(Provider.class) != null) {
          ProviderDescriptor providerDescriptor = new ProviderDescriptorImpl(clazz);
          everrest.addFactory(new GuiceObjectFactory<>(providerDescriptor, binding.getProvider()));
        } else if (clazz.getAnnotation(Filter.class) != null) {
          FilterDescriptor filterDescriptor = new FilterDescriptorImpl(clazz);
          everrest.addFactory(new GuiceObjectFactory<>(filterDescriptor, binding.getProvider()));
        } else if (clazz.getAnnotation(Path.class) != null) {
          ResourceDescriptor resourceDescriptor;
          if (bindingKey.getAnnotation() != null
              && bindingKey.getAnnotationType().isAssignableFrom(BindingPath.class)) {
            String path = ((BindingPath) bindingKey.getAnnotation()).value();
            resourceDescriptor = new AbstractResourceDescriptor(path, clazz);
          } else {
            resourceDescriptor = new AbstractResourceDescriptor(clazz);
          }
          everrest.addFactory(new GuiceObjectFactory<>(resourceDescriptor, binding.getProvider()));
        }
      }
    }
  }
}
