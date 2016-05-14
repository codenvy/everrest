/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.exoplatform.container;

import org.everrest.exoplatform.StandaloneBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
public class RestfulContainerTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        restfulContainer.stop();
        super.tearDown();
    }

    @Test
    public void testFindComponentAdaptersByAnnotation() throws Exception {
        restfulContainer.registerComponentImplementation(A.class);
        restfulContainer.registerComponentImplementation(B.class);
        restfulContainer.registerComponentImplementation(C.class);
        restfulContainer.registerComponentImplementation(D.class);
        List<ComponentAdapter> adapters = restfulContainer.getComponentAdapters(MyAnnotation.class);
        assertEquals(3, adapters.size());
        List<Class<?>> l = new ArrayList<Class<?>>(3);
        for (ComponentAdapter a : adapters) {
            l.add(a.getComponentImplementation());
        }
        assertTrue(l.contains(A.class));
        assertTrue(l.contains(B.class));
        assertTrue(l.contains(D.class));
    }

    @Test
    public void testFindComponentAdaptersByTypeAndAnnotation() throws Exception {
        restfulContainer.registerComponentImplementation(A.class);
        restfulContainer.registerComponentImplementation(B.class);
        restfulContainer.registerComponentImplementation(C.class);
        restfulContainer.registerComponentImplementation(D.class);
        List<ComponentAdapter> adapters = restfulContainer.getComponentAdaptersOfType(I.class, MyAnnotation.class);
        assertEquals(2, adapters.size());
        List<Class<?>> l = new ArrayList<Class<?>>(2);
        for (ComponentAdapter a : adapters) {
            l.add(a.getComponentImplementation());
        }
        assertTrue(l.contains(A.class));
        assertTrue(l.contains(B.class));
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface MyAnnotation {
    }

    public static interface I {
    }

    @MyAnnotation
    public static class A implements I {
    }

    @MyAnnotation
    public static class B extends A {
    }

    public static class C implements I {
    }

    @MyAnnotation
    public static class D {
    }
}
