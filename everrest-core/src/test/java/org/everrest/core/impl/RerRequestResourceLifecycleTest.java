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
package org.everrest.core.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author andrew00x
 */
public class RerRequestResourceLifecycleTest extends BaseTest {
    @Path("a")
    public static class Resource1 {
        static AtomicInteger destroyVisit = new AtomicInteger();
        static AtomicInteger initVisit    = new AtomicInteger();

        @SuppressWarnings("unused")
        @PreDestroy
        private void _destroy() // @PreDestroy must be processed even for private methods.
        {
            destroyVisit.incrementAndGet();
        }

        @SuppressWarnings("unused")
        @PostConstruct
        private void _init() // @PostConstruct must be processed even for private methods.
        {
            initVisit.incrementAndGet();
        }

        @POST
        public String m(String entity) {
            return entity;
        }
    }


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return java.util.Collections.<Class<?>>singleton(Resource1.class);
            }
        });
    }

    @Test
    public void testResources() throws Exception {
        Resource1.destroyVisit.set(0);
        Resource1.initVisit.set(0);
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", "text/plain");
        ContainerResponse response = launcher.service("POST", "a", "", h, "text".getBytes(), null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(1, Resource1.initVisit.get());
        Assert.assertEquals(1, Resource1.destroyVisit.get());
    }
}
