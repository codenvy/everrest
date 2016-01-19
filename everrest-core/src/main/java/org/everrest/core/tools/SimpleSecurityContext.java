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
package org.everrest.core.tools;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SimpleSecurityContext implements SecurityContext {
    private final String      authenticationScheme;
    private final Principal   principal;
    private final Set<String> userRoles;
    private final boolean     secure;

    public SimpleSecurityContext(Principal principal, Set<String> userRoles, String authenticationScheme, boolean secure) {
        this.principal = principal;
        this.authenticationScheme = authenticationScheme;
        this.secure = secure;
        this.userRoles = userRoles == null
                         ? Collections.<String>emptySet() : Collections.unmodifiableSet(new HashSet<String>(userRoles));
    }

    public SimpleSecurityContext(boolean secure) {
        this(null, null, null, secure);
    }


    @Override
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }


    @Override
    public Principal getUserPrincipal() {
        return principal;
    }


    @Override
    public boolean isSecure() {
        return secure;
    }


    @Override
    public boolean isUserInRole(String role) {
        return principal != null && userRoles.contains(role);
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }
}
