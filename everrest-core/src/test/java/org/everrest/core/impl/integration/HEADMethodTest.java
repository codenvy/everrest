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

import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Test;

/** @author Dmytro Katayev */
public class HEADMethodTest extends BaseTest {

  @Path("/a")
  public static class Resource_with_HEAD_method {
    @GET
    public String get() {
      return "GET resource";
    }

    @HEAD
    public String head() {
      return "HEAD resource";
    }
  }

  @Path("/b")
  public static class Resource_without_HEAD_method {
    @GET
    public String get() {
      return "GET resource";
    }
  }

  @Test
  public void whenHEADMethodIsAvailableUseIt() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new Resource_with_HEAD_method());
          }
        });

    ContainerResponse response = launcher.service("HEAD", "/a", "", null, null, null);

    assertEquals(200, response.getStatus());
    assertEquals("HEAD resource", response.getResponse().getEntity());
  }

  @Test
  public void whenHEADMethodIsNotThenUseGETMethod() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new Resource_without_HEAD_method());
          }
        });

    ContainerResponse response = launcher.service("HEAD", "/b", "", null, null, null);

    assertEquals(200, response.getStatus());
    assertEquals("GET resource", response.getResponse().getEntity());
  }
}
