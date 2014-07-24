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
package org.everrest.exoplatform.container;

import org.everrest.exoplatform.StandaloneBaseTest;
import org.picocontainer.ComponentAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RestfulContainerTest extends StandaloneBaseTest {
    private RestfulContainer restfulContainer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        restfulContainer = new RestfulContainer(container);
    }

    /** @see org.everrest.exoplatform.BaseTest#tearDown() */
    @Override
    protected void tearDown() throws Exception {
        restfulContainer.stop();
        super.tearDown();
    }

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
