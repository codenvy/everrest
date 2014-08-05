/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.test.mock;

/** @author Mestrallet Benjamin */
public class MockPrincipal implements java.security.Principal {

    private final String username;

    public MockPrincipal(String username) {
        this.username = username;
    }


    @Override
    public String getName() {
        return username;
    }
}
