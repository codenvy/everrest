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

import org.hamcrest.Matchers;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;

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
