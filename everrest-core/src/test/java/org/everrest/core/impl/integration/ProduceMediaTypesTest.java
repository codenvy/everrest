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
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_XML;
import static org.junit.Assert.assertEquals;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Set;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

/** @author Dmytro Katayev */
@RunWith(DataProviderRunner.class)
public class ProduceMediaTypesTest extends BaseTest {

  @Path("/a")
  @Produces(TEXT_PLAIN)
  public static class Resource {
    @GET
    @Path("/1")
    public String m1() {
      return "text";
    }

    @GET
    @Path("/2")
    @Produces(TEXT_XML)
    public String m2() {
      return "<xml/>";
    }

    @GET
    @Path("/3")
    @Produces(APPLICATION_JSON)
    public String m3() {
      return "{\"json\":\"text\"}";
    }
  }

  @DataProvider
  public static Object[][] data() {
    return new Object[][] {
      {new Resource(), "/a/1", null, 200, "text", "text/plain"},
      {new Resource(), "/a/1", "text/plain", 200, "text", "text/plain"},
      {new Resource(), "/a/1", "text/*", 200, "text", "text/plain"},
      {new Resource(), "/a/1", "*/*", 200, "text", "text/plain"},
      {new Resource(), "/a/1", "text/xml", 406, null, null},
      {new Resource(), "/a/2", null, 200, "<xml/>", "text/xml"},
      {new Resource(), "/a/2", "text/xml", 200, "<xml/>", "text/xml"},
      {new Resource(), "/a/2", "text/*", 200, "<xml/>", "text/xml"},
      {new Resource(), "/a/2", "*/*", 200, "<xml/>", "text/xml"},
      {new Resource(), "/a/2", "text/plain", 406, null, null},
      {new Resource(), "/a/3", null, 200, "{\"json\":\"text\"}", "application/json"},
      {new Resource(), "/a/3", "application/json", 200, "{\"json\":\"text\"}", "application/json"},
      {new Resource(), "/a/3", "application/*", 200, "{\"json\":\"text\"}", "application/json"},
      {new Resource(), "/a/3", "*/*", 200, "{\"json\":\"text\"}", "application/json"},
      {new Resource(), "/a/3", "text/xml", 406, null, null}
    };
  }

  @UseDataProvider("data")
  @Test
  public void testProducesMediaTypes(
      Object resource,
      String path,
      String acceptMediaType,
      int expectedStatus,
      Object expectedEntity,
      String expectedContentType)
      throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(resource);
          }
        });
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    if (acceptMediaType != null) {
      headers.add(ACCEPT, acceptMediaType);
    }

    ContainerResponse response = launcher.service("GET", path, "", headers, null, null);

    assertEquals(expectedStatus, response.getStatus());
    if (expectedEntity != null) {
      assertEquals(expectedEntity, response.getEntity());
    }
    if (expectedContentType != null) {
      assertEquals(MediaType.valueOf(expectedContentType), response.getContentType());
    }
  }
}
