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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;

/** @author andrew00x */
public class XHTTPMethodOverrideTest extends BaseTest {

  @Path("/a")
  public static class Resource1 {
    @POST
    public String m0() {
      return "m0";
    }
  }

  public void setUp() throws Exception {
    super.setUp();
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(Resource1.class);
          }
        });
  }

  @Test
  public void doesNotOverrideHttpMethod() throws Exception {
    assertEquals(405, launcher.service("GET", "/a", "", null, null, null).getStatus());
  }

  @Test
  public void overridesHttpMethod() throws Exception {
    MultivaluedMapImpl headers = new MultivaluedMapImpl();
    headers.putSingle(ExtHttpHeaders.X_HTTP_METHOD_OVERRIDE, "POST");
    ContainerResponse response = launcher.service("GET", "/a", "", headers, null, null);
    assertEquals(200, response.getStatus());
    assertEquals("m0", response.getEntity());
  }
}
