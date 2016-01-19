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

import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.expect;
import static org.everrest.assured.EverrestJetty.JETTY_SERVER;

/**
 *
 */
@Listeners(value = {EverrestJetty.class})
public class GroovyServiceTest {

    @Test
    public void testName(ITestContext context) throws Exception {
        JettyHttpServer httpServer = (JettyHttpServer)context.getAttribute(JETTY_SERVER);
        httpServer.publishPerRequestGroovyScript("a/b/GroovyResource1.groovy", "GroovyResource1");

        expect()
                .statusCode(200)
                .body(Matchers.containsString("GroovyResource1"))

                .when()
                .get("/a/b");
    }
}
