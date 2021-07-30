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

import static java.util.Collections.enumeration;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.EverrestApplication;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.core.impl.async.AsynchronousProcessListWriter;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.tools.DependencySupplierImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class EverrestServletContextInitializerTest {
  private ServletContext servletContext;
  private HashSet<Class<?>> scannedClasses;

  @Before
  public void setUp() throws Exception {
    servletContext = mock(ServletContext.class);

    scannedClasses = new HashSet<>(Arrays.asList(SomeResource.class, SomeProvider.class));
    ComponentFinder componentFinder = new ComponentFinder();
    componentFinder.reset();
    componentFinder.onStartup(scannedClasses, servletContext);
  }

  @Test
  public void createsApplicationIsConfiguredInServletContext() {
    when(servletContext.getInitParameter("javax.ws.rs.Application"))
        .thenReturn(SomeApplication.class.getName());

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);

    Application application = everrestServletContextInitializer.getApplication();
    assertEquals(SomeApplication.class, application.getClass());
  }

  @Test
  public void scansForJaxRsComponentsAndCreatesApplication() throws Exception {
    when(servletContext.getInitParameter("org.everrest.scan.components")).thenReturn("true");

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);

    Application application = everrestServletContextInitializer.getApplication();
    assertEquals(scannedClasses, application.getClasses());
  }

  @Test
  public void ignoresScanParameterIfApplicationIsConfiguredInServletContext() throws Exception {
    when(servletContext.getInitParameter("javax.ws.rs.Application"))
        .thenReturn(SomeApplication.class.getName());
    when(servletContext.getInitParameter("org.everrest.scan.components")).thenReturn("true");

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);

    Application application = everrestServletContextInitializer.getApplication();
    assertEquals(SomeApplication.class, application.getClass());
    assertTrue(
        "Must ignore scan 'org.everrest.scan.components' parameter if FQN of Application class configured in ServletContext",
        application.getClasses() == null || application.getClasses().isEmpty());
  }

  @Test
  public void createsEverrestConfigurationBasedOnInitParamsFromServletContext() {
    Map<String, String> initParams = new HashMap<>();
    initParams.put("org.everrest.http.method.override", "true");
    initParams.put("org.everrest.security", "true");
    initParams.put("org.everrest.asynchronous", "true");
    initParams.put("org.everrest.asynchronous.service.path", "/xxx");
    initParams.put("org.everrest.asynchronous.pool.size", "100");
    initParams.put("org.everrest.asynchronous.queue.size", "100");
    initParams.put("org.everrest.asynchronous.job.timeout", "60");
    initParams.put("org.everrest.core.impl.method.MethodInvokerDecoratorFactory", "SomeDecorator");
    initParams.put("org.everrest.normalize.uri", "true");
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestConfiguration configuration = everrestServletContextInitializer.createConfiguration();

    assertEquals(initParams, configuration.getAllProperties());
  }

  @Test
  public void createsResourceBinder() {
    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    ResourceBinder resources = everrestServletContextInitializer.createResourceBinder();
    assertNotNull(resources);
  }

  @Test
  public void createsDependencySupplier() {
    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    DependencySupplier dependencySupplier =
        everrestServletContextInitializer.getDependencySupplier();
    assertTrue(
        String.format(
            "ServletContextDependencySupplier is expected to be created but %s found",
            dependencySupplier),
        dependencySupplier instanceof ServletContextDependencySupplier);
  }

  @Test
  public void usesDependencySupplierConfiguredInServletContext() {
    DependencySupplier configuredDependencySupplier = new DependencySupplierImpl();
    when(servletContext.getAttribute(DependencySupplier.class.getName()))
        .thenReturn(configuredDependencySupplier);
    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    DependencySupplier dependencySupplier =
        everrestServletContextInitializer.getDependencySupplier();
    assertSame(
        String.format(
            "ServletContextDependencySupplier is expected to be created but %s found",
            dependencySupplier),
        dependencySupplier,
        configuredDependencySupplier);
  }

  @Test
  public void createsProviderBinder() {
    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    ProviderBinder providers = everrestServletContextInitializer.createProviderBinder();
    assertNotNull(providers);
  }

  @Test
  public void turnsOnAsynchronousSupportWithDefaultParameters() {
    Map<String, String> initParams = new HashMap<>();
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestProcessor everrestProcessor =
        everrestServletContextInitializer.createEverrestProcessor();
    EverrestApplication everrestApplication = everrestProcessor.getApplication();

    assertTrue(
        "AsynchronousJobService is expected to be mapped to path /async",
        everrestApplication.getResourceClasses().get("/async") == AsynchronousJobService.class);

    Object[] objectsMatchedToAsynchronousJobPool =
        everrestApplication
            .getSingletons()
            .stream()
            .filter(e -> e instanceof AsynchronousJobPool)
            .toArray();
    assertTrue(
        "AsynchronousJobPool is expected to be deployed as provider",
        objectsMatchedToAsynchronousJobPool.length == 1);
    AsynchronousJobPool asynchronousJobPool =
        (AsynchronousJobPool) objectsMatchedToAsynchronousJobPool[0];
    assertEquals("/async", asynchronousJobPool.getAsynchronousServicePath());
    assertEquals(10, asynchronousJobPool.getThreadPoolSize());
    assertEquals(100, asynchronousJobPool.getMaxQueueSize());
    assertEquals(512, asynchronousJobPool.getMaxCacheSize());
    assertEquals(60, asynchronousJobPool.getJobTimeout());

    assertTrue(
        "AsynchronousProcessListWriter is expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof AsynchronousProcessListWriter)
                .count()
            == 1);
  }

  @Test
  public void turnsOnAsynchronousSupportWithCustomParameters() {
    Map<String, String> initParams = new HashMap<>();
    initParams.put("org.everrest.asynchronous", "true");
    initParams.put("org.everrest.asynchronous.service.path", "/zzz");
    initParams.put("org.everrest.asynchronous.pool.size", "20");
    initParams.put("org.everrest.asynchronous.queue.size", "200");
    initParams.put("org.everrest.asynchronous.cache.size", "1024");
    initParams.put("org.everrest.asynchronous.job.timeout", "120");
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestProcessor everrestProcessor =
        everrestServletContextInitializer.createEverrestProcessor();
    EverrestApplication everrestApplication = everrestProcessor.getApplication();

    assertTrue(
        "AsynchronousJobService is expected to be mapped to path /zzz",
        everrestApplication.getResourceClasses().get("/zzz") == AsynchronousJobService.class);

    Object[] objectsMatchedToAsynchronousJobPool =
        everrestApplication
            .getSingletons()
            .stream()
            .filter(e -> e instanceof AsynchronousJobPool)
            .toArray();
    assertTrue(
        "AsynchronousJobPool is expected to be deployed as provider",
        objectsMatchedToAsynchronousJobPool.length == 1);
    AsynchronousJobPool asynchronousJobPool =
        (AsynchronousJobPool) objectsMatchedToAsynchronousJobPool[0];
    assertEquals("/zzz", asynchronousJobPool.getAsynchronousServicePath());
    assertEquals(20, asynchronousJobPool.getThreadPoolSize());
    assertEquals(200, asynchronousJobPool.getMaxQueueSize());
    assertEquals(1024, asynchronousJobPool.getMaxCacheSize());
    assertEquals(120, asynchronousJobPool.getJobTimeout());

    assertTrue(
        "AsynchronousProcessListWriter is expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof AsynchronousProcessListWriter)
                .count()
            == 1);
  }

  @Test
  public void turnsOffAsynchronousSupportInConfiguration() {
    Map<String, String> initParams = new HashMap<>();
    initParams.put("org.everrest.asynchronous", "false");
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestProcessor everrestProcessor =
        everrestServletContextInitializer.createEverrestProcessor();
    EverrestApplication everrestApplication = everrestProcessor.getApplication();

    assertTrue(
        "AsynchronousJobService is expected to be deployed as resource",
        everrestApplication
                .getResourceClasses()
                .values()
                .stream()
                .filter(e -> e == AsynchronousJobService.class)
                .count()
            == 0);

    assertTrue(
        "AsynchronousJobPool is not expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof AsynchronousJobPool)
                .count()
            == 0);

    assertTrue(
        "AsynchronousProcessListWriter is not expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof AsynchronousProcessListWriter)
                .count()
            == 0);
  }

  @Test
  public void turnsOnSecurityConstrainSupportInConfiguration() {
    Map<String, String> initParams = new HashMap<>();
    initParams.put("org.everrest.security", "true");
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestProcessor everrestProcessor =
        everrestServletContextInitializer.createEverrestProcessor();
    EverrestApplication everrestApplication = everrestProcessor.getApplication();

    assertTrue(
        "SecurityConstraint is expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof SecurityConstraint)
                .count()
            == 1);
  }

  @Test
  public void turnsOnSecurityConstrainSupportByDefault() {
    Map<String, String> initParams = new HashMap<>();
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestProcessor everrestProcessor =
        everrestServletContextInitializer.createEverrestProcessor();
    EverrestApplication everrestApplication = everrestProcessor.getApplication();

    assertTrue(
        "SecurityConstraint is expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof SecurityConstraint)
                .count()
            == 1);
  }

  @Test
  public void turnsOffSecurityConstrainSupportInConfiguration() {
    Map<String, String> initParams = new HashMap<>();
    initParams.put("org.everrest.security", "false");
    configureInitParamsInServletContext(initParams);

    EverrestServletContextInitializer everrestServletContextInitializer =
        new EverrestServletContextInitializer(servletContext);
    EverrestProcessor everrestProcessor =
        everrestServletContextInitializer.createEverrestProcessor();
    EverrestApplication everrestApplication = everrestProcessor.getApplication();

    assertTrue(
        "SecurityConstraint is not expected to be deployed as provider",
        everrestApplication
                .getSingletons()
                .stream()
                .filter(e -> e instanceof SecurityConstraint)
                .count()
            == 0);
  }

  private void configureInitParamsInServletContext(Map<String, String> initParams) {
    when(servletContext.getInitParameterNames()).thenReturn(enumeration(initParams.keySet()));
    when(servletContext.getInitParameter(any(String.class)))
        .thenAnswer(
            new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) throws Throwable {
                String paramName = (String) invocation.getArguments()[0];
                return initParams.get(paramName);
              }
            });
  }

  public static class SomeApplication extends Application {}
}

@Path("/a/b/c")
class SomeResource {}

@Provider
class SomeProvider {}
