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

import static io.restassured.RestAssured.expect;
import static org.everrest.assured.EverrestJetty.JETTY_SERVER;

import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {EverrestJetty.class})
public class GroovyServiceTest {

  @Test
  public void testName(ITestContext context) throws Exception {
    JettyHttpServer httpServer = (JettyHttpServer) context.getAttribute(JETTY_SERVER);
    httpServer.publishPerRequestGroovyScript("a/b/GroovyResource1.groovy", "GroovyResource1");

    expect().statusCode(200).body(Matchers.containsString("GroovyResource1")).when().get("/a/b");
  }
}
