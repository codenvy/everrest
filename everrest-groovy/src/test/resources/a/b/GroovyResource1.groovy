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
package a.b

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Context

@Path("a")
class GroovyResource1 {
    GroovyResource1(@Context HttpServletRequest req1) {
        this.req1 = req1;
    }

    @Context
    private HttpServletRequest req
    private HttpServletRequest req1

    @GET
    @Path("b")
    def m0() { req.getMethod() + "\n" + req.getRequestURI().toString() }
}