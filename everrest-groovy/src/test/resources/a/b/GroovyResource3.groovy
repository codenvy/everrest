/**
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