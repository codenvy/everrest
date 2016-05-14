/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.assured;

import com.jayway.restassured.response.Response;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import static com.jayway.restassured.RestAssured.when;
import static org.testng.Assert.assertEquals;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ContextInjectionTest {
    @Path("test")
    public class EchoService {
        @Context
        protected UriInfo uriInfo;

        @GET
        @Path("my_method")
        @Produces(MediaType.TEXT_PLAIN)
        public String echo(@DefaultValue("a") @QueryParam("text") String test) {
            return uriInfo.getBaseUri().toString();
        }
    }

    @Path("test2")
    public static class EchoService2 {
        @Context
        protected UriInfo uriInfo;

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