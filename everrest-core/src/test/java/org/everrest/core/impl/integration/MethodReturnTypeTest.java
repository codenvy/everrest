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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Before;
import org.junit.Test;

/** @author Dmytro Katayev */
public class MethodReturnTypeTest extends BaseTest {

  @Path("/a")
  public static class Resource {
    @POST
    @Path("/1")
    public void m1() {}

    @POST
    @Path("/2")
    public Response m2() {
      return Response.ok("m2").build();
    }

    @POST
    @Path("/3")
    public Response m3() {
      return null;
    }

    @POST
    @Path("/4")
    public GenericEntity<List<String>> m4() {
      return new GenericEntity<List<String>>(newArrayList("m4")) {};
    }

    @POST
    @Path("/5")
    public String m5() {
      return null;
    }

    @POST
    @Path("/6")
    public String m6() {
      return "m6";
    }
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new Resource());
          }
        });
  }

  @Test
  public void resourceMethodReturnsVoid() throws Exception {
    ContainerResponse response = launcher.service("POST", "/a/1", "", null, null, null);
    assertEquals(204, response.getStatus());
    assertNull(response.getEntity());
  }

  @Test
  public void resourceMethodReturnsResponse() throws Exception {
    ContainerResponse response = launcher.service("POST", "/a/2", "", null, null, null);
    assertEquals(200, response.getStatus());
    assertEquals("m2", response.getEntity());
  }

  @Test
  public void resourceMethodReturnsNullResponse() throws Exception {
    ContainerResponse response = launcher.service("POST", "/a/3", "", null, null, null);
    assertEquals(204, response.getStatus());
    assertNull(response.getEntity());
  }

  @Test
  public void resourceMethodReturnsGenericEntity() throws Exception {
    ContainerResponse response = launcher.service("POST", "/a/4", "", null, null, null);
    assertEquals(200, response.getStatus());
    assertEquals(newArrayList("m4"), response.getEntity());
  }

  @Test
  public void resourceMethodReturnsNull() throws Exception {
    ContainerResponse response = launcher.service("POST", "/a/5", "", null, null, null);
    assertEquals(204, response.getStatus());
    assertNull(response.getEntity());
  }

  @Test
  public void resourceMethodReturnsString() throws Exception {
    ContainerResponse response = launcher.service("POST", "/a/6", "", null, null, null);
    assertEquals(200, response.getStatus());
    assertEquals("m6", response.getEntity());
  }
}
