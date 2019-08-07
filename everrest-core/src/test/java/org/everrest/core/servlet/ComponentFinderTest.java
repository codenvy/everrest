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
package org.everrest.core.servlet;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentFinderTest {
    private ServletContext  servletContext;
    private ComponentFinder componentFinder;

    @Before
    public void setUp() throws Exception {
        componentFinder = new ComponentFinder();
        componentFinder.reset();

        servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameter("org.everrest.scan.skip.packages"))
                .thenReturn("org.everrest.core.servlet.Skip1, org.everrest.core.servlet.Skip2");
    }

    @Test
    public void skipsClassesThatConfiguredAsSkippedInServletContext() throws Exception {
        HashSet<Class<?>> classes = new HashSet<>(Arrays.asList(Skip1.class, Deploy.class, Skip2.class));
        componentFinder.onStartup(classes, servletContext);
        assertEquals(new HashSet<Class<?>>(Arrays.asList(Deploy.class)), ComponentFinder.findComponents());
    }
}

class Deploy {
}

class Skip1 {
}

class Skip2 {
}
