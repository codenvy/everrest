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
package org.everrest.groovy;

import groovy.lang.GroovyClassLoader;

import org.everrest.core.impl.ApplicationPublisher;
import org.everrest.groovy.servlet.GroovyEverrestServletContextInitializer;
import org.everrest.test.mock.MockServletContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author andrew00x
 */
public class GroovyApplicationTest extends BaseTest {

    protected ApplicationPublisher applicationPublisher;
    protected GroovyClassLoader    groovyClassLoader;

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
                "class Application0 extends javax.ws.rs.core.Application\n"
                + "{\n" //
                + "Set<Class<?>> getClasses(){new HashSet<Class<?>>([a.b.GResource1.class, a.b.GExceptionMapper.class])}\n"
                + "}\n";
        Class<?> class1 = groovyClassLoader.parseClass(application);
        javax.ws.rs.core.Application groovyApplication = (javax.ws.rs.core.Application)class1.newInstance();
        applicationPublisher.publish(groovyApplication);
        Assert.assertEquals("GResource1", launcher.service("GET", "/a/1", "", null, null, null).getEntity());
        // ExceptionMapper written in Groovy should process a.b.GRuntimeException.
        Assert.assertEquals("GExceptionMapper", launcher.service("GET", "/a/2", "", null, null, null).getEntity());
    }

    @Test
    public void testScanComponents() {
        MockServletContext mockContext = new MockServletContext("test");
        StringBuilder classPath = new StringBuilder();
        classPath.append(Thread.currentThread().getContextClassLoader().getResource("scan/").toString());
        mockContext.setInitParameter(GroovyEverrestServletContextInitializer.EVERREST_GROOVY_ROOT_RESOURCES, classPath
                .toString());
        mockContext.setInitParameter(GroovyEverrestServletContextInitializer.EVERREST_GROOVY_SCAN_COMPONENTS, "true");
        GroovyEverrestServletContextInitializer initializer = new GroovyEverrestServletContextInitializer(mockContext);
        Application application = initializer.getApplication();
        Set<Class<?>> classes = application.getClasses();
        Assert.assertNotNull(classes);
        Assert.assertEquals(2, classes.size());
        java.util.List<String> l = new ArrayList<>(2);
        for (Class<?> c : classes) {
            l.add(c.getName());
        }
        Assert.assertTrue(l.contains("org.everrest.A"));
        Assert.assertTrue(l.contains("org.everrest.B"));
    }

}
