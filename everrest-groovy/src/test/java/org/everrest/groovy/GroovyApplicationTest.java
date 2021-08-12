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
package org.everrest.groovy;

import static org.everrest.groovy.servlet.GroovyEverrestServletContextInitializer.EVERREST_GROOVY_ROOT_RESOURCES;
import static org.everrest.groovy.servlet.GroovyEverrestServletContextInitializer.EVERREST_GROOVY_SCAN_COMPONENTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import groovy.lang.GroovyClassLoader;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.core.Application;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.groovy.servlet.GroovyEverrestServletContextInitializer;
import org.junit.Before;
import org.junit.Test;

/** @author andrew00x */
public class GroovyApplicationTest extends BaseTest {

  protected ApplicationPublisher applicationPublisher;
  protected GroovyClassLoader groovyClassLoader;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    groovyClassLoader = new GroovyClassLoader();
    URL root = Thread.currentThread().getContextClassLoader().getResource("repo");
    DefaultGroovyResourceLoader groovyResourceLoader = new DefaultGroovyResourceLoader(root);
    groovyClassLoader.setResourceLoader(groovyResourceLoader);
    applicationPublisher = new ApplicationPublisher(resources, providers);
  }

  @Test
  public void testApplication() throws Exception {
    String application =
        "class Application0 extends jakarta.ws.rs.core.Application\n"
            + "{\n" //
            + "Set<Class<?>> getClasses(){new HashSet<Class<?>>([a.b.GResource1.class, a.b.GExceptionMapper.class])}\n"
            + "}\n";
    Class<?> class1 = groovyClassLoader.parseClass(application);
    jakarta.ws.rs.core.Application groovyApplication =
        (jakarta.ws.rs.core.Application) class1.newInstance();
    applicationPublisher.publish(groovyApplication);
    assertEquals("GResource1", launcher.service("GET", "/a/1", "", null, null, null).getEntity());
    // ExceptionMapper written in Groovy should process a.b.GRuntimeException.
    assertEquals(
        "GExceptionMapper", launcher.service("GET", "/a/2", "", null, null, null).getEntity());
  }

  @Test
  public void testScanComponents() {
    StringBuilder classPath = new StringBuilder();
    classPath.append(
        Thread.currentThread().getContextClassLoader().getResource("scan/").toString());

    ServletContext servletContext = mock(ServletContext.class);
    when(servletContext.getInitParameter(EVERREST_GROOVY_ROOT_RESOURCES))
        .thenReturn(classPath.toString());
    when(servletContext.getInitParameter(EVERREST_GROOVY_SCAN_COMPONENTS)).thenReturn("true");

    GroovyEverrestServletContextInitializer initializer =
        new GroovyEverrestServletContextInitializer(servletContext);
    Application application = initializer.getApplication();
    Set<Class<?>> classes = application.getClasses();
    assertNotNull(classes);
    assertEquals(2, classes.size());
    java.util.List<String> l = new ArrayList<>(2);
    l.addAll(classes.stream().map(Class::getName).collect(Collectors.toList()));
    assertTrue(l.contains("org.everrest.A"));
    assertTrue(l.contains("org.everrest.B"));
  }
}
