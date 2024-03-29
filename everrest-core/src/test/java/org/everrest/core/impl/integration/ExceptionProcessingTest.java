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

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Before;
import org.junit.Test;

/** @author Dmytro Katayev */
public class ExceptionProcessingTest extends BaseTest {

  public static class SomeException extends Exception {
    public SomeException() {}

    public SomeException(String msg) {
      super(msg);
    }
  }

  @Path("/a")
  public static class Resource1 {
    @Path("/1")
    @GET
    public void m1() throws Exception {
      throw new SomeException("Some Exception");
    }

    @Path("/2")
    @GET
    public void m2() throws Exception {
      throw new SomeException();
    }

    @Path("/3")
    @GET
    public void m3() {
      throw new WebApplicationException(
          Response.status(500)
              .entity("this exception must not be hidden by any ExceptionMapper")
              .build());
    }

    @GET
    @Path("/4")
    public void m4() {
      throw new WebApplicationException(Response.status(400).build());
    }
  }

  @Provider
  public static class WebApplicationExceptionMapper
      implements ExceptionMapper<WebApplicationException> {
    int invocationCounter;

    @Override
    public Response toResponse(WebApplicationException exception) {
      invocationCounter++;
      return Response.status(200).entity("<WebApplicationException>").build();
    }
  }

  @Provider
  public static class SomeExceptionMapper implements ExceptionMapper<SomeException> {
    int invocationCounter;

    @Override
    public Response toResponse(SomeException exception) {
      invocationCounter++;
      return Response.status(200).entity("<<SomeException>>").build();
    }
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new Resource1());
          }
        });
  }

  @Test
  public void whenThrownExceptionHasMessageThenSendItInResponseEntity() throws Exception {
    ContainerResponse response = launcher.service("GET", "/a/1", "", null, null, null);

    assertEquals(500, response.getStatus());
    assertEquals("Some Exception", response.getEntity());
  }

  @Test
  public void whenThrownExceptionDoesNotHaveMessageThenSendExceptionClassNameInResponseEntity()
      throws Exception {
    ContainerResponse response = launcher.service("GET", "/a/2", "", null, null, null);

    assertEquals(500, response.getStatus());
    assertEquals(SomeException.class.getName(), response.getEntity());
  }

  @Test
  public void whenThrownExceptionTypeHasExceptionMapperItIsUsedToTransformExceptionToResponse()
      throws Exception {
    SomeExceptionMapper someExceptionMapper = new SomeExceptionMapper();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(someExceptionMapper);
          }
        });

    ContainerResponse response = launcher.service("GET", "/a/2", "", null, null, null);

    assertEquals(200, response.getStatus());
    assertEquals("<<SomeException>>", response.getEntity());
  }

  @Test
  public void whenWebApplicationExceptionHasEntityThenItIsNotProcessedByExceptionMapper()
      throws Exception {
    WebApplicationExceptionMapper webApplicationExceptionMapper =
        new WebApplicationExceptionMapper();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(webApplicationExceptionMapper);
          }
        });

    ContainerResponse response = launcher.service("GET", "/a/3", "", null, null, null);

    assertEquals(500, response.getStatus());
    assertEquals("this exception must not be hidden by any ExceptionMapper", response.getEntity());
    assertEquals(0, webApplicationExceptionMapper.invocationCounter);
  }

  @Test
  public void whenWebApplicationExceptionDoesNotHaveEntityThenItIsProcessedByExceptionMapper()
      throws Exception {
    WebApplicationExceptionMapper webApplicationExceptionMapper =
        new WebApplicationExceptionMapper();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(webApplicationExceptionMapper);
          }
        });

    ContainerResponse response = launcher.service("GET", "/a/4", "", null, null, null);

    assertEquals(200, response.getStatus());
    assertEquals("<WebApplicationException>", response.getEntity());
    assertEquals(1, webApplicationExceptionMapper.invocationCounter);
  }
}
