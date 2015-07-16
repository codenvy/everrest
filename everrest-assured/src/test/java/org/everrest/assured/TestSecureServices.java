/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.assured;

import org.hamcrest.Matchers;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;

/**
 *
 */
@Listeners(value = {EverrestJetty.class})
public class TestSecureServices {
    @Path("/secure-test")
    public class SecureService {
        @GET
        @RolesAllowed("cloud-admin")
        @Path("/sstring")
        public String getSecure() {
            return "sstring";
        }

        @GET
        @Path("/usstring")
        public String getUSecure() {
            return "usstring";
        }
    }

    private final SecureService secureService = new SecureService();

    @Test
    public void shouldAllowToCallUnsecureMethodWithUnsecureRequest() {
        expect()
                .body(Matchers.equalTo("usstring"))
                .when().get("/secure-test/usstring");
    }

    @Test
    public void shouldNotAllowToCallUnsecureMethodWithSecureRequest() {
        expect()
                .statusCode(401)
                .when().get(JettyHttpServer.SECURE_PATH + "/secure-test/usstring");
    }

    @Test
    public void shouldNotAllowToCallSecureMethodWithSecureRequestWithoutAutorization() {
        expect()
                .statusCode(401)
                .when().get(JettyHttpServer.SECURE_PATH + "/secure-test/sstring");
    }

    @Test
    public void shouldNotAllowToCallSecureMethodWithUnsecureRequest() {
        expect()
                .statusCode(403)
                .when().get("/secure-test/sstring");
    }

    @Test
    public void shouldAllowToCallUnsecureMethodWithSecureRequest() {
        //given
        given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
                //when-then
                .expect()
                .body(Matchers.equalTo("usstring"))
                .when().get("/secure-test/usstring");
    }

    @Test
    public void shouldAllowToCallUnsecureMethodWithSecureRequestURL() {
        //given
        given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
                //when-then
                .expect()
                .body(Matchers.equalTo("usstring"))
                .when().get(JettyHttpServer.SECURE_PATH + "/secure-test/usstring");
    }

    @Test
    public void shouldAllowToCallSecureMethodWithSecureRequestURL() {
        //given
        given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
                //when-then
                .expect()
                .body(Matchers.equalTo("sstring"))
                .when().get(JettyHttpServer.SECURE_PATH + "/secure-test/sstring");
    }

    @Test
    public void shouldNotAllowToCallSecureMethodWithUnsecureULR() {
        //given
        given()
                .auth().basic(JettyHttpServer.ADMIN_USER_NAME, JettyHttpServer.ADMIN_USER_PASSWORD)
                //when-then
                .expect()
                .statusCode(403)
                .when().get("/secure-test/sstring");
    }
}
