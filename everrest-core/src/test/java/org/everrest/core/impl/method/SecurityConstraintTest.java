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
package org.everrest.core.impl.method;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.test.mock.MockPrincipal;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
public class SecurityConstraintTest extends BaseTest {

    private Principal   userPrincipal  = new MockPrincipal("user");
    private Principal   adminPrincipal = new MockPrincipal("admin");
    private Set<String> userRoles      = new HashSet<>();
    private Set<String> adminRoles     = new HashSet<>();

    {
        userRoles.add("users");
        adminRoles.add("users");
        adminRoles.add("administrators");
    }

    private SecurityContext userSctx = new SimpleSecurityContext(userPrincipal, userRoles, "BASIC", false);

    private SecurityContext adminSctx = new SimpleSecurityContext(adminPrincipal, adminRoles, "BASIC", false);

    public void setUp() throws Exception {
        super.setUp();
        // Be sure security is on.
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new SecurityConstraint());
            }
        });
    }

    @Path("a")
    public static class Resource1 {
        @DenyAll
        @GET
        @Path("1")
        public void m0() {
        }

        @RolesAllowed({"users"})
        @GET
        @Path("2")
        public void m1() {
        }
    }

    @Test
    public void testResource1() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource1.class);
            }
        });

        EnvironmentContext env = new EnvironmentContext();

        env.put(SecurityContext.class, adminSctx);
        // DenyAll annotation disable calling method for any users
        Assert.assertEquals(403, launcher.service("GET", "/a/1", "", null, null, env).getStatus());

        env.put(SecurityContext.class, userSctx);
        // "user" principal is in "users" role
        Assert.assertEquals(204, launcher.service("GET", "/a/2", "", null, null, env).getStatus());

        env.put(SecurityContext.class, adminSctx);
        // "admin" principal is in "users" role also
        Assert.assertEquals(204, launcher.service("GET", "/a/2", "", null, null, env).getStatus());
    }

    @Path("b")
    public static class Resource2 {
        @RolesAllowed({"users"})
        @GET
        @Path("1")
        public void m0() {
        }

        @RolesAllowed({"administrators"})
        @GET
        @Path("2")
        public void m1() {
        }
    }

    @Test
    public void testResource2() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource2.class);
            }
        });

        EnvironmentContext env = new EnvironmentContext();
        env.put(SecurityContext.class, userSctx);

        // "user" principal is in "users" role
        Assert.assertEquals(204, launcher.service("GET", "/b/1", "", null, null, env).getStatus());

        // "user" principal is not in "administrators" role
        Assert.assertEquals(403, launcher.service("GET", "/b/2", "", null, null, env).getStatus());

        env.put(SecurityContext.class, adminSctx);
        // "admin" principal is in "administrators" role also
        Assert.assertEquals(204, launcher.service("GET", "/b/2", "", null, null, env).getStatus());
    }

    @Path("c")
    @RolesAllowed({"administrators"})
    public static class Resource3 {
        @DenyAll
        @GET
        @Path("1")
        public void m0() {
        }

        @RolesAllowed({"users"})
        @GET
        @Path("2")
        public void m1() {
        }

        @PermitAll
        @GET
        @Path("3")
        public void m2() {
        }

        @GET
        @Path("4")
        public void m3() {
        }
    }

    @Test
    public void testResource3() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource3.class);
            }
        });

        EnvironmentContext env = new EnvironmentContext();

        // deny for all
        env.put(SecurityContext.class, userSctx);
        Assert.assertEquals(403, launcher.service("GET", "/c/1", "", null, null, env).getStatus());
        env.put(SecurityContext.class, adminSctx);
        Assert.assertEquals(403, launcher.service("GET", "/c/1", "", null, null, env).getStatus());

        // allowed for "users" and "administrators"
        env.put(SecurityContext.class, userSctx);
        Assert.assertEquals(204, launcher.service("GET", "/c/2", "", null, null, env).getStatus());
        env.put(SecurityContext.class, adminSctx);
        Assert.assertEquals(204, launcher.service("GET", "/c/2", "", null, null, env).getStatus());
        // forbidden for anonymous
        env.put(SecurityContext.class, null);
        Assert.assertEquals(403, launcher.service("GET", "/c/2", "", null, null, env).getStatus());

        // allowed for anybody
        env.put(SecurityContext.class, userSctx);
        Assert.assertEquals(204, launcher.service("GET", "/c/3", "", null, null, env).getStatus());
        env.put(SecurityContext.class, adminSctx);
        Assert.assertEquals(204, launcher.service("GET", "/c/3", "", null, null, env).getStatus());
        env.put(SecurityContext.class, null);
        Assert.assertEquals(204, launcher.service("GET", "/c/3", "", null, null, env).getStatus());

        // allowed for "administrators" only. Annotation inherited from class.
        env.put(SecurityContext.class, userSctx);
        Assert.assertEquals(403, launcher.service("GET", "/c/4", "", null, null, env).getStatus());
        env.put(SecurityContext.class, adminSctx);
        Assert.assertEquals(204, launcher.service("GET", "/c/4", "", null, null, env).getStatus());
        env.put(SecurityContext.class, null);
        Assert.assertEquals(403, launcher.service("GET", "/c/4", "", null, null, env).getStatus());
    }
}
