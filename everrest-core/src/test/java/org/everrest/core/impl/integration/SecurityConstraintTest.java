/*
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
package org.everrest.core.impl.integration;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.tools.SimpleSecurityContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class SecurityConstraintTest extends BaseTest {

    private static SecurityContext userSecCtx;
    private static SecurityContext adminSecCtx;

    static {
        Principal user = mock(Principal.class);
        when(user.getName()).thenReturn("user");
        Set<String> userRoles = newHashSet("users");
        userSecCtx = new SimpleSecurityContext(user, userRoles, "BASIC", false);

        Principal admin = mock(Principal.class);
        when(user.getName()).thenReturn("admin");
        Set<String> adminRoles = newHashSet("administrators");
        adminSecCtx = new SimpleSecurityContext(admin, adminRoles, "BASIC", false);
    }

    @Path("a")
    public static class Resource1 {
        @DenyAll
        @GET
        @Path("no-body")
        public void noBody() {
        }

        @RolesAllowed({"users"})
        @GET
        @Path("users-only")
        public void usersOnly() {
        }

        @RolesAllowed({"administrators"})
        @GET
        @Path("admins-only")
        public void adminsOnly() {
        }

        @RolesAllowed({"users", "administrators"})
        @GET
        @Path("user-and-admins")
        public void userAndAdmins() {
        }

        @GET
        @Path("all")
        public void all() {
        }
    }

    @Path("b")
    @RolesAllowed({"administrators"})
    public static class Resource2 {
        @DenyAll
        @GET
        @Path("no-body")
        public void noBody() {
        }

        @RolesAllowed({"users"})
        @GET
        @Path("users-only")
        public void usersOnly() {
        }

        @RolesAllowed({"administrators"})
        @GET
        @Path("admins-only")
        public void adminsOnly() {
        }

        @RolesAllowed({"users", "administrators"})
        @GET
        @Path("user-and-admins")
        public void userAndAdmins() {
        }

        @PermitAll
        @GET
        @Path("all")
        public void all() {
        }
    }

    @DataProvider
    public static Object[][] data() {
        return new Object[][]{
                {Resource1.class, "/a/no-body", null, 403},
                {Resource1.class, "/a/no-body", userSecCtx, 403},
                {Resource1.class, "/a/no-body", adminSecCtx, 403},

                {Resource1.class, "/a/users-only", null, 403},
                {Resource1.class, "/a/users-only", userSecCtx, 204},
                {Resource1.class, "/a/users-only", adminSecCtx, 403},

                {Resource1.class, "/a/admins-only", null, 403},
                {Resource1.class, "/a/admins-only", userSecCtx, 403},
                {Resource1.class, "/a/admins-only", adminSecCtx, 204},

                {Resource1.class, "/a/user-and-admins", null, 403},
                {Resource1.class, "/a/user-and-admins", userSecCtx, 204},
                {Resource1.class, "/a/user-and-admins", adminSecCtx, 204},

                {Resource1.class, "/a/all", null, 204},
                {Resource1.class, "/a/all", userSecCtx, 204},
                {Resource1.class, "/a/all", adminSecCtx, 204},


                {Resource2.class, "/b/no-body", null, 403},
                {Resource2.class, "/b/no-body", userSecCtx, 403},
                {Resource2.class, "/b/no-body", adminSecCtx, 403},

                {Resource2.class, "/b/users-only", null, 403},
                {Resource2.class, "/b/users-only", userSecCtx, 204},
                {Resource2.class, "/b/users-only", adminSecCtx, 403},

                {Resource2.class, "/b/admins-only", null, 403},
                {Resource2.class, "/b/admins-only", userSecCtx, 403},
                {Resource2.class, "/b/admins-only", adminSecCtx, 204},

                {Resource2.class, "/b/user-and-admins", null, 403},
                {Resource2.class, "/b/user-and-admins", userSecCtx, 204},
                {Resource2.class, "/b/user-and-admins", adminSecCtx, 204},

                {Resource2.class, "/b/all", null, 204},
                {Resource2.class, "/b/all", userSecCtx, 204},
                {Resource2.class, "/b/all", adminSecCtx, 204}
        };
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Be sure security is on.
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(new SecurityConstraint());
            }
        });
    }

    @UseDataProvider("data")
    @Test
    public void testSecurityRestrictions(Class<?> resource, String path, SecurityContext secCtx, int expectedResponseStatus)
            throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(resource);
            }
        });
        EnvironmentContext env = new EnvironmentContext();
        env.put(SecurityContext.class, secCtx);

        ContainerResponse response = launcher.service("GET", path, "", null, null, env);

        assertEquals(expectedResponseStatus, response.getStatus());
    }
}
