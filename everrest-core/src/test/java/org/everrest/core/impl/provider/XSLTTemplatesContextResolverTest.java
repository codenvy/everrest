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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XSLTTemplatesContextResolverTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TemplatesParser templatesParser;
    private XSLTTemplatesContextResolver xsltTemplatesContextResolver;

    @Before
    public void setUp() throws Exception {
        templatesParser = mock(TemplatesParser.class);
        xsltTemplatesContextResolver = new XSLTTemplatesContextResolver(templatesParser);
    }

    @Test
    public void getsXSLTTemplatesContextResolverAsContextResolver() throws Exception {
        XSLTTemplatesContextResolver contextResolver = xsltTemplatesContextResolver.getContext(null);
        assertSame(xsltTemplatesContextResolver, contextResolver);
    }

    @Test
    public void returnsNullIfTemplatesIsNotAvailable() throws Exception {
        assertNull(xsltTemplatesContextResolver.getTemplates("unavailable"));
    }

    @Test
    public void savesTemplatesAndReturnsItByName() {
        Templates templates = mock(Templates.class);
        xsltTemplatesContextResolver.addTemplates("templates 1", templates);

        assertSame(templates, xsltTemplatesContextResolver.getTemplates("templates 1"));
    }

    @Test
    public void throwsIllegalArgumentExceptionIfTemplatesWithTheSameNameAlreadyRegistered() {
        xsltTemplatesContextResolver.addTemplates("templates 1", mock(Templates.class));

        thrown.expect(IllegalArgumentException.class);
        xsltTemplatesContextResolver.addTemplates("templates 1", mock(Templates.class));
    }

    @Test
    public void removesRegisteredTemplatesByName() {
        xsltTemplatesContextResolver.addTemplates("templates 1", mock(Templates.class));
        assertNotNull(xsltTemplatesContextResolver.getTemplates("templates 1"));

        xsltTemplatesContextResolver.removeTemplates("templates 1");
        assertNull(xsltTemplatesContextResolver.getTemplates("templates 1"));
    }

    @Test
    public void parsesGivenSourceAndRegistersParsedTemplates() throws Exception {
        Source source = mock(Source.class);
        Templates templates = mock(Templates.class);
        when(templatesParser.parseTemplates(source)).thenReturn(templates);

        xsltTemplatesContextResolver.addAsTemplate("templates 1", source);
        assertSame(templates, xsltTemplatesContextResolver.getTemplates("templates 1"));
    }
}