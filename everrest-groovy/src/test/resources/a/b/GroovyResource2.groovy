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
class GroovyResource2 {
    private org.everrest.groovy.GroovyIoCInjectTest.Component1 component

    GroovyResource2(org.everrest.groovy.GroovyIoCInjectTest.Component1 component) {
        this.component = component
    }

    @javax.ws.rs.GET
    @javax.ws.rs.Path("b")
    def m0() { component.getName() }
}