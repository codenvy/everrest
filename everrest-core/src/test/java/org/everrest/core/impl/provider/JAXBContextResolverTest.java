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

import org.everrest.core.generated.Book;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class JAXBContextResolverTest {

    private JAXBContextResolver jaxbContextResolver;

    @Before
    public void setUp() throws Exception {
        jaxbContextResolver = new JAXBContextResolver();
    }

    @Test
    public void getsJAXBContextResolverAsContextResolver() throws Exception {
        JAXBContextResolver contextResolver = jaxbContextResolver.getContext(null);
        assertSame(jaxbContextResolver, contextResolver);
    }

    @Test
    public void returnsJAXBContextForClass() throws Exception {
        assertNotNull(jaxbContextResolver.getJAXBContext(Book.class));
    }

    @Test
    public void savesGivenJAXBContext() throws Exception {
        JAXBContext jaxbContext = mock(JAXBContext.class);
        jaxbContextResolver.addJAXBContext(jaxbContext, Book.class);
        assertSame(jaxbContext, jaxbContextResolver.getJAXBContext(Book.class));
    }

    @Test
    public void removesJAXBContext() throws Exception {
        JAXBContext jaxbContext = mock(JAXBContext.class);
        jaxbContextResolver.addJAXBContext(jaxbContext, Book.class);

        jaxbContextResolver.removeJAXBContext(Book.class);
        JAXBContext newJaxbContext = jaxbContextResolver.getJAXBContext(Book.class);
        assertNotNull(newJaxbContext);
        assertNotSame(jaxbContext, jaxbContextResolver.getJAXBContext(Book.class));
    }
}