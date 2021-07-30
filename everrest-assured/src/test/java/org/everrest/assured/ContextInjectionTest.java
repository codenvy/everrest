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
package org.everrest.assured;

import static com.jayway.restassured.RestAssured.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ContextInjectionTest {
  @Path("test")
  public class EchoService {
    @Context protected UriInfo uriInfo;

    @GET
    @Path("my_method")
    @Produces(MediaType.TEXT_PLAIN)
    public String echo(@DefaultValue("a") @QueryParam("text") String test) {
      return uriInfo.getBaseUri().toString();
    }
  }

  @Path("test2")
  public static class EchoService2 {
    @Context protected UriInfo uriInfo;

    @GET
    @Path("my_method")
    @Produces(MediaType.TEXT_PLAIN)
    public String echo(@DefaultValue("a") @QueryParam("text") String test) {
      return uriInfo.getBaseUri().toString();
    }
  }

  EchoService echoServiceSingleton = new EchoService();
  EchoService2 echoServicePerRequest;

  @Test
  public void shouldIjectContextInSingleton() throws Exception {
    final Response response = when().get("/test/my_method");

    assertEquals(response.getStatusCode(), 200);
  }

  @Test
  public void shouldIjectContextInPerRequest() throws Exception {
    final Response response = when().get("/test2/my_method");

    assertEquals(response.getStatusCode(), 200);
  }
}
