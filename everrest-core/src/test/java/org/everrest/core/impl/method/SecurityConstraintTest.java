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

package org.everrest.core.impl.method;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.method.filter.SecurityConstraint;
import org.everrest.core.tools.DummySecurityContext;
import org.everrest.test.mock.MockPrincipal;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.SecurityContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SecurityConstraintTest extends BaseTest
{

   private Principal userPrincipal = new MockPrincipal("user");

   private Principal adminPrincipal = new MockPrincipal("admin");

   private Set<String> userRoles = new HashSet<String>();

   private Set<String> adminRoles = new HashSet<String>();

   {
      userRoles.add("users");
      adminRoles.add("users");
      adminRoles.add("administrators");
   }

   private SecurityContext userSctx = new DummySecurityContext(userPrincipal, userRoles);

   private SecurityContext adminSctx = new DummySecurityContext(adminPrincipal, adminRoles);

   public void setUp() throws Exception
   {
      super.setUp();
      // Be sure security is on.
      providers.addMethodInvokerFilter(new SecurityConstraint());
   }

   @Path("a")
   public static class Resource1
   {
      @DenyAll
      @GET
      @Path("1")
      public void m0()
      {
      }

      @RolesAllowed({"users"})
      @GET
      @Path("2")
      public void m1()
      {
      }
   }

   public void testResource1() throws Exception
   {
      registry(Resource1.class);

      EnvironmentContext env = new EnvironmentContext();

      env.put(SecurityContext.class, adminSctx);
      // DenyAll annotation disable calling method for any users
      assertEquals(403, launcher.service("GET", "/a/1", "", null, null, env).getStatus());

      env.put(SecurityContext.class, userSctx);
      // "user" principal is in "users" role
      assertEquals(204, launcher.service("GET", "/a/2", "", null, null, env).getStatus());

      env.put(SecurityContext.class, adminSctx);
      // "admin" principal is in "users" role also
      assertEquals(204, launcher.service("GET", "/a/2", "", null, null, env).getStatus());

      unregistry(Resource1.class);
   }

   @Path("b")
   public static class Resource2
   {
      @RolesAllowed({"users"})
      @GET
      @Path("1")
      public void m0()
      {
      }

      @RolesAllowed({"administrators"})
      @GET
      @Path("2")
      public void m1()
      {
      }
   }

   public void testResource2() throws Exception
   {
      registry(Resource2.class);

      EnvironmentContext env = new EnvironmentContext();
      env.put(SecurityContext.class, userSctx);

      // "user" principal is in "users" role
      assertEquals(204, launcher.service("GET", "/b/1", "", null, null, env).getStatus());

      // "user" principal is not in "administrators" role
      assertEquals(403, launcher.service("GET", "/b/2", "", null, null, env).getStatus());

      env.put(SecurityContext.class, adminSctx);
      // "admin" principal is in "administrators" role also
      assertEquals(204, launcher.service("GET", "/b/2", "", null, null, env).getStatus());

      unregistry(Resource2.class);
   }

   @Path("c")
   @RolesAllowed({"administrators"})
   public static class Resource3
   {
      @DenyAll
      @GET
      @Path("1")
      public void m0()
      {
      }

      @RolesAllowed({"users"})
      @GET
      @Path("2")
      public void m1()
      {
      }

      @PermitAll
      @GET
      @Path("3")
      public void m2()
      {
      }

      @GET
      @Path("4")
      public void m3()
      {
      }
}

   public void testResource3() throws Exception
   {
      registry(Resource3.class);

      EnvironmentContext env = new EnvironmentContext();

      // deny for all
      env.put(SecurityContext.class, userSctx);
      assertEquals(403, launcher.service("GET", "/c/1", "", null, null, env).getStatus());
      env.put(SecurityContext.class, adminSctx);
      assertEquals(403, launcher.service("GET", "/c/1", "", null, null, env).getStatus());

      // allowed for "users" and "administrators"
      env.put(SecurityContext.class, userSctx);
      assertEquals(204, launcher.service("GET", "/c/2", "", null, null, env).getStatus());
      env.put(SecurityContext.class, adminSctx);
      assertEquals(204, launcher.service("GET", "/c/2", "", null, null, env).getStatus());
      // forbidden for anonymous
      env.put(SecurityContext.class, null);
      assertEquals(403, launcher.service("GET", "/c/2", "", null, null, env).getStatus());

      // allowed for anybody
      env.put(SecurityContext.class, userSctx);
      assertEquals(204, launcher.service("GET", "/c/3", "", null, null, env).getStatus());
      env.put(SecurityContext.class, adminSctx);
      assertEquals(204, launcher.service("GET", "/c/3", "", null, null, env).getStatus());
      env.put(SecurityContext.class, null);
      assertEquals(204, launcher.service("GET", "/c/3", "", null, null, env).getStatus());

      // allowed for "administrators" only. Annotation inherited from class.
      env.put(SecurityContext.class, userSctx);
      assertEquals(403, launcher.service("GET", "/c/4", "", null, null, env).getStatus());
      env.put(SecurityContext.class, adminSctx);
      assertEquals(204, launcher.service("GET", "/c/4", "", null, null, env).getStatus());
      env.put(SecurityContext.class, null);
      assertEquals(403, launcher.service("GET", "/c/4", "", null, null, env).getStatus());

      unregistry(Resource3.class);
   }
}
