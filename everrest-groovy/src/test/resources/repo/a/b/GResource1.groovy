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

@javax.ws.rs.Path("a")
class GResource1 {
    @javax.ws.rs.GET
    @javax.ws.rs.Path("1")
    def m0() { "GResource1" }

    @javax.ws.rs.GET
    @javax.ws.rs.Path("2")
    def m1() { throw new GRuntimeException() }
}