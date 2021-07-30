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
package org.everrest.core.impl.provider;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;
import org.junit.Before;
import org.junit.Test;

public class TemplatesParserTest {
  private TemplatesParser templatesParser;

  @Before
  public void setUp() throws Exception {
    templatesParser = new TemplatesParser();
  }

  @Test
  public void parsesGivenSourceToTemplates() throws Exception {
    try (InputStream testXslResource =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("xslt/book.xsl")) {
      Source source = new StreamSource(testXslResource);
      Templates templates = templatesParser.parseTemplates(source);
      assertNotNull(templates);
    }
  }
}
