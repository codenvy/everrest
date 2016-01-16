/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

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