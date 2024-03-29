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
package org.everrest.core.impl.integration;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static org.everrest.core.ExtHttpHeaders.ACCEPT_RANGES;
import static org.everrest.core.ExtHttpHeaders.CONTENT_RANGE;
import static org.junit.Assert.assertEquals;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.Ranges;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;

/** @author andrew00x */
public class PartialContentTest extends BaseTest {

  @Path("a")
  public static class Resource1 {
    @GET
    public Response m(@HeaderParam("Range") Ranges ranges) {
      int start = (int) ranges.getRanges().get(0).getStart();
      int end = (int) ranges.getRanges().get(0).getEnd();
      byte[] bytes = new byte[(end - start + 1)];
      System.arraycopy(contentBytes, 2, bytes, 0, bytes.length);
      return Response.status(206)
          .header(CONTENT_LENGTH, Long.toString(bytes.length))
          .header(ACCEPT_RANGES, "bytes")
          .header(CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, contentBytes.length))
          .entity(bytes)
          .build();
    }
  }

  private static final String contentString = "to be or not to be";

  private static final byte[] contentBytes = contentString.getBytes();

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return Collections.<Class<?>>singleton(Resource1.class);
          }
        });
  }

  @Test
  public void testPartialContent() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("range", "bytes=2-5");
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    ContainerResponse response = launcher.service("GET", "/a", "", headers, null, writer, null);

    assertEquals(206, response.getStatus());
    MultivaluedMap<String, Object> responseHeaders = response.getHttpHeaders();
    assertEquals("4", responseHeaders.getFirst("content-length"));
    assertEquals("bytes 2-5/18", responseHeaders.getFirst("content-range"));
    assertEquals("bytes", responseHeaders.getFirst("accept-ranges"));
    assertEquals(" be ", new String(writer.getBody()));
  }
}
