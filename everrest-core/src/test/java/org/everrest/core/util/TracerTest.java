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
package org.everrest.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedHashMap;
import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.util.Tracer.TraceHolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

public class TracerTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private ApplicationContext applicationContext;

  @Before
  public void setUp() throws Exception {
    applicationContext = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
    when(applicationContext.getAttributes().get("tracer")).thenReturn(new TraceHolder());

    ApplicationContext.setCurrent(applicationContext);
  }

  @Test
  public void tracingIsDisabledByDefault() throws Exception {
    when(applicationContext.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    assertFalse(Tracer.isTracingEnabled());
  }

  @Test
  public void enablesTracingByQueryParameter() throws Exception {
    enableTracing();
    assertTrue(Tracer.isTracingEnabled());
  }

  @Test
  public void throwsExceptionWhenThreadLocalApplicationContextIsNotSet() throws Exception {
    ApplicationContext.setCurrent(null);

    thrown.expect(IllegalStateException.class);
    Tracer.isTracingEnabled();
  }

  @Test
  public void addsTracingHeader() throws Exception {
    enableTracing();
    Tracer.trace("foo");
    Tracer.trace("bar");

    GenericContainerResponse containerResponse =
        mock(GenericContainerResponse.class, RETURNS_DEEP_STUBS);
    Tracer.addTraceHeaders(containerResponse);

    InOrder inOrder = inOrder(containerResponse.getHttpHeaders());
    inOrder.verify(containerResponse.getHttpHeaders()).add("EverRest-Trace-001", "foo");
    inOrder.verify(containerResponse.getHttpHeaders()).add("EverRest-Trace-002", "bar");
  }

  @Test
  public void formatsStringAndAddsTracingHeader() throws Exception {
    enableTracing();
    Tracer.trace("foo %d", 3);
    Tracer.trace("bar %d", 2);

    GenericContainerResponse containerResponse =
        mock(GenericContainerResponse.class, RETURNS_DEEP_STUBS);
    Tracer.addTraceHeaders(containerResponse);

    InOrder inOrder = inOrder(containerResponse.getHttpHeaders());
    inOrder.verify(containerResponse.getHttpHeaders()).add("EverRest-Trace-001", "foo 3");
    inOrder.verify(containerResponse.getHttpHeaders()).add("EverRest-Trace-002", "bar 2");
  }

  private void enableTracing() {
    when((Object) applicationContext.getQueryParameters().getFirst("tracing")).thenReturn("true");
  }
}
