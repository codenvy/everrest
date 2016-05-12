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
import org.junit.Test;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertNotNull;

public class TemplatesParserTest {
    private TemplatesParser templatesParser;

    @Before
    public void setUp() throws Exception {
        templatesParser = new TemplatesParser();
    }

    @Test
    public void parsesGiveSourceToTemplates() throws Exception {
        Source source = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("xslt/book.xsl"));
        Templates templates = templatesParser.parseTemplates(source);
        assertNotNull(templates);
    }
}