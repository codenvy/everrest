/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.tools;

import org.junit.Test;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebApplicationDeclaredRolesTest {

    private String webXmlWithRoles = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                     "<web-app id=\"WebApp_ID\" version=\"3.0\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                     "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_3_0.xsd\">\n" +
                                     "    <security-role>\n" +
                                     "        <role-name>admin</role-name>\n" +
                                     "    </security-role>\n" +
                                     "    <security-role>\n" +
                                     "        <role-name>user</role-name>\n" +
                                     "    </security-role>\n" +
                                     "</web-app>\n";

    @Test
    public void parsesRolesFromWebXml() {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream("/WEB-INF/web.xml")).thenReturn(new ByteArrayInputStream(webXmlWithRoles.getBytes()));
        WebApplicationDeclaredRoles webApplicationDeclaredRoles = new WebApplicationDeclaredRoles(servletContext);
        assertEquals(new HashSet<>(Arrays.asList("user", "admin")), webApplicationDeclaredRoles.getDeclaredRoles());
    }

    @Test
    public void returnsEmptyRolesSetIfWebXmlNotFound() {
        ServletContext servletContext = mock(ServletContext.class);
        WebApplicationDeclaredRoles webApplicationDeclaredRoles = new WebApplicationDeclaredRoles(servletContext);
        assertTrue("Roles set must be empty", webApplicationDeclaredRoles.getDeclaredRoles().isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void throwsRuntimeExceptionIfErrorOccursWhileParsingWebXml() {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream("/WEB-INF/web.xml")).thenReturn(new ByteArrayInputStream("invalid web.xml".getBytes()));
        new WebApplicationDeclaredRoles(servletContext);
    }
}