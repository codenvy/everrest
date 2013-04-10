/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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

    /** {@inheritDoc} */
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    /** {@inheritDoc} */
    public Principal getUserPrincipal() {
        return principal;
    }

    /** {@inheritDoc} */
    public boolean isSecure() {
        return secure;
    }

    /** {@inheritDoc} */
    public boolean isUserInRole(String role) {
        return principal != null && userRoles.contains(role);
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }
}
