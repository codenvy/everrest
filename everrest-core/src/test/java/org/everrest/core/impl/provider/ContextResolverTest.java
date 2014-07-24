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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
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

    public void setUp() throws Exception {
        super.setUp();
        providers.addContextResolver(ContextResolver1.class);
        providers.addContextResolver(ContextResolver2.class);
        providers.addContextResolver(ContextResolver3.class);
        providers.addContextResolver(ContextResolver4.class);
        providers.addContextResolver(ContextResolver5.class);
    }

    public void tearDown() throws Exception {
        super.setUp();
    }

    public void testContextResolver() {
        assertEquals("text", providers.getContextResolver(String.class, new MediaType("text", "plain")).getContext(
                String.class));
        assertEquals("*", providers.getContextResolver(String.class, new MediaType("xxx", "xxx"))
                                   .getContext(String.class));
        assertEquals("xml", providers.getContextResolver(String.class, new MediaType("text", "xml")).getContext(
                String.class));
        assertEquals("html", providers.getContextResolver(String.class, new MediaType("text", "html")).getContext(
                String.class));
        assertEquals("anytext", providers.getContextResolver(String.class, new MediaType("text", "xxx")).getContext(
                String.class));
    }

}
