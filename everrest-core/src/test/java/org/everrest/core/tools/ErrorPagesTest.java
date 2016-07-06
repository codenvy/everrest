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
package org.everrest.core.tools;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ErrorPagesTest {

    private String webXmlWithErrorPages = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<web-app id=\"WebApp_ID\" version=\"3.0\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                          "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_3_0.xsd\">\n" +
                                          "    <error-page>\n" +
                                          "        <error-code>400</error-code>\n" +
                                          "        <location>/Error400.html</location>\n" +
                                          "    </error-page>\n" +
                                          "    <error-page>\n" +
                                          "        <exception-type>org.test.SomeException</exception-type>\n" +
                                          "        <location>/SomeError.jsp</location>\n" +
                                          "    </error-page>\n" +
                                          "</web-app>\n";
    private ErrorPages errorPages;

    @Before
    public void setUp() throws Exception {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream("/WEB-INF/web.xml")).thenReturn(new ByteArrayInputStream(webXmlWithErrorPages.getBytes()));
        errorPages = new ErrorPages(servletContext);
    }

    @Test
    public void containsErrorPageForErrorCode() {
        assertTrue("Expected to have configured error page for status 400", errorPages.hasErrorPage(400));
    }

    @Test
    public void containsErrorPageForSomeException() {
        assertTrue("Expected to have configured error page for org.test.SomeException", errorPages.hasErrorPage("org.test.SomeException"));
    }

    @Test(expected = RuntimeException.class)
    public void throwsRuntimeExceptionIfErrorOccursWhileParsingWebXml() {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream("/WEB-INF/web.xml")).thenReturn(new ByteArrayInputStream("invalid web.xml".getBytes()));
        new ErrorPages(servletContext);
    }
}