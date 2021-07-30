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
package org.everrest.core.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.everrest.core.ApplicationContext.anApplicationContext;
import static org.everrest.core.ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE;
import static org.everrest.core.impl.EverrestConfiguration.METHOD_INVOKER_DECORATOR_FACTORY;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.Application;
import org.everrest.core.ApplicationContext;
import org.everrest.core.DependencySupplier;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.Lifecycle;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.impl.uri.UriComponent;
import org.everrest.core.util.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class EverrestProcessor implements Lifecycle {

  private static final Logger LOG = LoggerFactory.getLogger(EverrestProcessor.class);

  private final DependencySupplier dependencySupplier;
  private final RequestHandler requestHandler;
  private final EverrestApplication everrestApplication;
  private final EverrestConfiguration configuration;

  private final MethodInvokerDecoratorFactory methodInvokerDecoratorFactory;
  /**
   * Application properties. Properties from this map will be copied to ApplicationContext and may
   * be accessible via method {@link ApplicationContext#getProperties()}.
   */
  private final Map<String, String> properties;

  public EverrestProcessor(DependencySupplier dependencySupplier, RequestHandler requestHandler) {
    this(null, dependencySupplier, requestHandler, null);
  }

  public EverrestProcessor(
      EverrestConfiguration configuration,
      DependencySupplier dependencySupplier,
      RequestHandler requestHandler,
      Application application) {
    this.configuration = configuration == null ? new EverrestConfiguration() : configuration;
    this.dependencySupplier = dependencySupplier;
    this.requestHandler = requestHandler;

    properties = new ConcurrentHashMap<>();
    everrestApplication = new EverrestApplication();
    if (application != null) {
      addApplication(application);
    }
    methodInvokerDecoratorFactory = createMethodInvokerDecoratorFactory(this.configuration);
  }

  private MethodInvokerDecoratorFactory createMethodInvokerDecoratorFactory(
      EverrestConfiguration configuration) {
    String decoratorFactoryClassName = configuration.getProperty(METHOD_INVOKER_DECORATOR_FACTORY);
    if (decoratorFactoryClassName != null) {
      try {
        Class<?> decoratorFactoryClass =
            Thread.currentThread().getContextClassLoader().loadClass(decoratorFactoryClassName);
        return MethodInvokerDecoratorFactory.class.cast(decoratorFactoryClass.newInstance());
      } catch (Exception e) {
        throw new IllegalStateException(
            String.format("Cannot instantiate '%s', : %s", decoratorFactoryClassName, e), e);
      }
    }
    return null;
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

  public void process(
      GenericContainerRequest request,
      GenericContainerResponse response,
      EnvironmentContext environmentContext)
      throws IOException {

    EnvironmentContext.setCurrent(environmentContext);

    ApplicationContext context =
        anApplicationContext()
            .withRequest(request)
            .withResponse(response)
            .withProviders(requestHandler.getProviders())
            .withProperties(properties)
            .withApplication(everrestApplication)
            .withConfiguration(new EverrestConfiguration(configuration))
            .withDependencySupplier(dependencySupplier)
            .withMethodInvokerDecoratorFactory(methodInvokerDecoratorFactory)
            .build();
    try {
      context.start();
      ApplicationContext.setCurrent(context);
      if (configuration.isNormalizeUri()) {
        normalizeRequestUri(request);
      }
      if (configuration.isHttpMethodOverride()) {
        overrideHttpMethod(request);
      }
      requestHandler.handleRequest(request, response);
    } finally {
      try {
        context.stop();
      } finally {
        ApplicationContext.setCurrent(null);
      }
      EnvironmentContext.setCurrent(null);
    }
  }

  private void normalizeRequestUri(GenericContainerRequest request) {
    request.setUris(UriComponent.normalize(request.getRequestUri()), request.getBaseUri());
  }

  private void overrideHttpMethod(GenericContainerRequest request) {
    String method = request.getRequestHeaders().getFirst(X_HTTP_METHOD_OVERRIDE);
    if (method != null) {
      if (Tracer.isTracingEnabled()) {
        Tracer.trace(
            "Override HTTP method from \"X-HTTP-Method-Override\" header %s => %s",
            request.getMethod(), method);
      }
      request.setMethod(method);
    }
  }

  public void addApplication(Application application) {
    checkNotNull(application);
    everrestApplication.addApplication(application);
    ApplicationPublisher applicationPublisher =
        new ApplicationPublisher(requestHandler.getResources(), requestHandler.getProviders());
    applicationPublisher.publish(application);
  }

  @Override
  public void start() {}

  @Override
  public void stop() {
    for (Object singleton : everrestApplication.getSingletons()) {
      try {
        new LifecycleComponent(singleton).destroy();
      } catch (InternalException e) {
        LOG.error("Unable to destroy component", e);
      }
    }
  }

  public ProviderBinder getProviders() {
    return requestHandler.getProviders();
  }

  public ResourceBinder getResources() {
    return requestHandler.getResources();
  }

  public EverrestApplication getApplication() {
    return everrestApplication;
  }

  public DependencySupplier getDependencySupplier() {
    return dependencySupplier;
  }
}
