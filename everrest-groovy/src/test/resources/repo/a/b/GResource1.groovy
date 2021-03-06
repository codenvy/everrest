/**
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

@javax.ws.rs.Path("a")
class GResource1 {
    @javax.ws.rs.GET
    @javax.ws.rs.Path("1")
    def m0() { "GResource1" }

    @javax.ws.rs.GET
    @javax.ws.rs.Path("2")
    def m1() { throw new GRuntimeException() }
}