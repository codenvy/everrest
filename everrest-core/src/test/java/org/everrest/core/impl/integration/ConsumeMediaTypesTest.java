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
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_XML;
import static org.junit.Assert.assertEquals;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

/** @author Dmytro Katayev */
@RunWith(DataProviderRunner.class)
public class ConsumeMediaTypesTest extends BaseTest {

  @Path("/a")
  @Consumes(TEXT_PLAIN)
  public static class Resource {
    @POST
    @Path("/1")
    public void m1(@HeaderParam(CONTENT_TYPE) String type) {
      assertEquals(TEXT_PLAIN, type);
    }

    @POST
    @Path("/1")
    @Consumes(TEXT_XML)
    public void m11(@HeaderParam(CONTENT_TYPE) String type) {
      assertEquals(TEXT_XML, type);
    }

    @POST
    @Path("/2")
    @Consumes(TEXT_XML)
    public void m2(@HeaderParam(CONTENT_TYPE) String type) {
      assertEquals(TEXT_XML, type);
    }

    @POST
    @Path("/3")
    @Consumes(APPLICATION_JSON)
    public void m3(@HeaderParam(CONTENT_TYPE) String type) {
      assertEquals(APPLICATION_JSON, type);
    }
  }

  @DataProvider
  public static Object[][] data() {
    return new Object[][] {
      {new Resource(), "/a/1", "text/plain", "text", 204},
      {new Resource(), "/a/1", "text/xml", "<xml/>", 204},
      {new Resource(), "/a/2", "text/xml", "<xml/>", 204},
      {new Resource(), "/a/3", "application/json", "{\"json\":\"text\"}", 204},
      {new Resource(), "/a/2", "text/html", "<html></html>", 415}
    };
  }

  @UseDataProvider("data")
  @Test
  public void testConsumesMediaTypes(
      Object resource, String path, String contentType, String entity, int expectedStatus)
      throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(resource);
          }
        });
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    if (contentType != null) {
      headers.add(CONTENT_TYPE, contentType);
    }

    ContainerResponse response =
        launcher.service("POST", path, "", headers, entity.getBytes(), null);

    assertEquals(expectedStatus, response.getStatus());
  }
}
