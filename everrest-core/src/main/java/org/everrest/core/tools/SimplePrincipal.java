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
package org.everrest.core.tools;

import java.security.Principal;
import java.util.Objects;

public class SimplePrincipal implements Principal {
    private final String identity;

    public SimplePrincipal(String identity) {
        this.identity = identity;
    }

    @Override
    public String getName() {
        return identity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimplePrincipal)) {
            return false;
        }

        return Objects.equals(identity, ((SimplePrincipal)obj).identity);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(identity);
        return hash;
    }

    @Override
    public String toString() {
        return "SimplePrincipal{identity='" + identity + '\'' + '}';
    }
}
