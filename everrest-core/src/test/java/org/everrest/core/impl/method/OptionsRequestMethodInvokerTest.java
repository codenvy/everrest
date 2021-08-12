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
package org.everrest.core.impl.method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.everrest.core.ApplicationContext;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.wadl.WadlProcessor;
import org.junit.Before;
import org.junit.Test;

public class OptionsRequestMethodInvokerTest {
  private WadlProcessor wadlProcessor;
  private ApplicationContext applicationContext;
  private ResourceDescriptor resourceDescriptor;
  private GenericResourceMethod resourceMethod;

  private OptionsRequestMethodInvoker optionsRequestMethodInvoker;

  @Before
  public void setUp() throws Exception {
    wadlProcessor = mock(WadlProcessor.class);
    resourceDescriptor = mock(ResourceDescriptor.class);
    resourceMethod = mock(GenericResourceMethod.class);
    applicationContext = mock(ApplicationContext.class);

    when(resourceMethod.getParentResource()).thenReturn(resourceDescriptor);
    when(applicationContext.getBaseUri()).thenReturn(URI.create("/a"));

    optionsRequestMethodInvoker = new OptionsRequestMethodInvoker(wadlProcessor);
  }

  @Test
  public void providesWadlResponse() throws Exception {
    org.everrest.core.wadl.research.Application wadlDoc =
        new org.everrest.core.wadl.research.Application();
    when(wadlProcessor.process(resourceDescriptor, URI.create("/a"))).thenReturn(wadlDoc);

    Object object = new Object();
    Response response =
        (Response)
            optionsRequestMethodInvoker.invokeMethod(object, resourceMethod, applicationContext);

    assertEquals(200, response.getStatus());
    assertEquals(new MediaType("application", "vnd.sun.wadl+xml"), response.getMediaType());
    assertSame(wadlDoc, response.getEntity());
  }
}
