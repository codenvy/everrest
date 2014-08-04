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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ProviderBinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ContextResolverTest extends BaseTest {

    @Provider
    @Produces("text/plain")
    public static class ContextResolver1 implements javax.ws.rs.ext.ContextResolver<String> {
        public String getContext(Class<?> type) {
            return "text";
        }
    }

    @Provider
    public static class ContextResolver2 implements javax.ws.rs.ext.ContextResolver<String> {
        public String getContext(Class<?> type) {
            return "*";
        }
    }

    @Provider
    @Produces("text/xml")
    public static class ContextResolver3 implements javax.ws.rs.ext.ContextResolver<String> {
        public String getContext(Class<?> type) {
            return "xml";
        }
    }

    @Provider
    @Produces("text/html")
    public static class ContextResolver4 implements javax.ws.rs.ext.ContextResolver<String> {
        public String getContext(Class<?> type) {
            return "html";
        }
    }

    @Provider
    @Produces("text/*")
    public static class ContextResolver5 implements javax.ws.rs.ext.ContextResolver<String> {
        public String getContext(Class<?> type) {
            return "anytext";
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new LinkedHashSet<>();
                classes.add(ContextResolver1.class);
                classes.add(ContextResolver2.class);
                classes.add(ContextResolver3.class);
                classes.add(ContextResolver4.class);
                classes.add(ContextResolver5.class);
                return classes;
            }
        });
    }

    @Test
    public void testContextResolverTextPlain() {
        ProviderBinder providers = processor.getProviders();
        Assert.assertEquals("text", providers.getContextResolver(String.class, new MediaType("text", "plain")).getContext(String.class));
    }

    @Test
    public void testContextResolverAnyMediaType() {
        ProviderBinder providers = processor.getProviders();
        Assert.assertEquals("*", providers.getContextResolver(String.class, new MediaType("xxx", "xxx")).getContext(String.class));
    }

    @Test
    public void testContextResolverTextXML() {
        ProviderBinder providers = processor.getProviders();
        Assert.assertEquals("xml", providers.getContextResolver(String.class, new MediaType("text", "xml")).getContext(String.class));
    }

    @Test
    public void testContextResolverHTML() {
        ProviderBinder providers = processor.getProviders();
        Assert.assertEquals("html", providers.getContextResolver(String.class, new MediaType("text", "html")).getContext(String.class));
    }

    @Test
    public void testContextResolverAnyText() {
        ProviderBinder providers = processor.getProviders();
        Assert.assertEquals("anytext", providers.getContextResolver(String.class, new MediaType("text", "xxx")).getContext(String.class));
    }
}
