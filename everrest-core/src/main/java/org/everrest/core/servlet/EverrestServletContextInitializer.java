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
package org.everrest.core.servlet;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.RequestHandler;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class EverrestServletContextInitializer {
  private static final Logger LOG =
      LoggerFactory.getLogger(EverrestServletContextInitializer.class);

  public static final String EVERREST_SCAN_COMPONENTS = "org.everrest.scan.components";
  public static final String EVERREST_SCAN_SKIP_PACKAGES = "org.everrest.scan.skip.packages";
  public static final String JAXRS_APPLICATION = "jakarta.ws.rs.Application";

  protected final ServletContext servletContext;

  public EverrestServletContextInitializer(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Try get application's FQN from context-param jakarta.ws.rs.Application and instantiate it. If
   * such parameter is not specified then scan web application's folders WEB-INF/classes and
   * WEB-INF/lib for classes which contains JAX-RS annotations. Interesting for three annotations
   * {@link Path}, {@link Provider} and {@link Filter} .
   *
   * @return instance of jakarta.ws.rs.core.Application
   */
  public Application getApplication() {
    Application application = null;
    String applicationFQN = getParameter(JAXRS_APPLICATION);
    boolean scan = getBoolean(EVERREST_SCAN_COMPONENTS, false);
    if (applicationFQN != null) {
      if (scan) {
        String msg =
            "Scan of JAX-RS components is disabled cause to specified 'jakarta.ws.rs.Application'.";
        LOG.warn(msg);
      }
      try {
        Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(applicationFQN);
        application = (Application) cl.newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else if (scan) {
      application =
          new Application() {
            @Override
            public Set<Class<?>> getClasses() {
              return new LinkedHashSet<>(ComponentFinder.findComponents());
            }
          };
    }
    return application;
  }

  public EverrestConfiguration createConfiguration() {
    EverrestConfiguration config = new EverrestConfiguration();
    for (String parameterName : getParameterNames()) {
      config.setProperty(parameterName, getParameter(parameterName));
    }
    return config;
  }

  public RequestHandler createRequestHandler() {
    return new RequestHandlerImpl(createRequestDispatcher(), createProviderBinder());
  }

  public RequestDispatcher createRequestDispatcher() {
    return new RequestDispatcher(createResourceBinder());
  }

  public ResourceBinder createResourceBinder() {
    return new ResourceBinderImpl();
  }

  public DependencySupplier getDependencySupplier() {
    DependencySupplier dependencySupplier =
        (DependencySupplier) servletContext.getAttribute(DependencySupplier.class.getName());
    if (dependencySupplier == null) {
      dependencySupplier = new ServletContextDependencySupplier(servletContext);
    }
    return dependencySupplier;
  }

  public ProviderBinder createProviderBinder() {
    return new ApplicationProviderBinder();
  }

  private EverrestApplication getEverrestApplication(EverrestConfiguration configuration) {
    EverrestApplication everrest = new EverrestApplication();
    if (configuration.isAsynchronousSupported()) {
      everrest.addResource(
          configuration.getAsynchronousServicePath(), AsynchronousJobService.class);
      everrest.addSingleton(new AsynchronousJobPool(configuration));
      everrest.addSingleton(new AsynchronousProcessListWriter());
    }
    if (configuration.isCheckSecurity()) {
      everrest.addSingleton(new SecurityConstraint());
    }
    everrest.addApplication(getApplication());
    return everrest;
  }

  public EverrestProcessor createEverrestProcessor() {
    EverrestConfiguration configuration = createConfiguration();
    return new EverrestProcessor(
        configuration,
        getDependencySupplier(),
        createRequestHandler(),
        getEverrestApplication(configuration));
  }

  protected List<String> getParameterNames() {
    return Collections.list(servletContext.getInitParameterNames());
  }

  /**
   * Get parameter with specified name from servlet context initial parameters.
   *
   * @param name parameter name
   * @return value of parameter with specified name
   */
  protected String getParameter(String name) {
    String str = servletContext.getInitParameter(name);
    if (str != null) {
      return str.trim();
    }
    return null;
  }

  protected String getParameter(String name, String def) {
    String value = getParameter(name);
    if (value == null) {
      return def;
    }
    return value;
  }

  protected boolean getBoolean(String name, boolean def) {
    String str = getParameter(name);
    if (str != null) {
      return "true".equalsIgnoreCase(str)
          || "yes".equalsIgnoreCase(str)
          || "on".equalsIgnoreCase(str)
          || "1".equals(str);
    }
    return def;
  }

  protected Double getNumber(String name, double def) {
    String str = getParameter(name);
    if (str != null) {
      try {
        return Double.parseDouble(str);
      } catch (NumberFormatException ignored) {
      }
    }
    return def;
  }
}
