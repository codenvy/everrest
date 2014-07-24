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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
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
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SimplePrincipal other = (SimplePrincipal)obj;
        return identity == null ? other.identity == null : identity.equals(other.identity);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + (identity == null ? 0 : identity.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return "SimplePrincipal{identity='" + identity + '\'' + '}';
    }
}
