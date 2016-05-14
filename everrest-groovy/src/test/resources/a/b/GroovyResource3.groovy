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

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("a")
class GroovyResource3 {
    GroovyResource3() {}

    @GET
    @Path("b")
    def m0() { System.getProperty("java.home") }
}