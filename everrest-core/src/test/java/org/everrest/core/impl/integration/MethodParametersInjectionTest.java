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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class MethodParametersInjectionTest extends BaseTest {

  @UseDataProvider("injectParametersTestData")
  @Test
  public void injectsParameters(
      Object resource,
      String path,
      Map<String, List<String>> requestHeaders,
      String requestEntity,
      Object responseEntity)
      throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(resource);
          }
        });
    ContainerResponse response =
        launcher.service(
            "POST",
            path,
            "",
            requestHeaders,
            requestEntity == null ? null : requestEntity.getBytes(),
            null);

    assertEquals(responseEntity, response.getEntity());
  }

  @DataProvider
  public static Object[][] injectParametersTestData() {
    return new Object[][] {
      {new PathParamResource(), "/a/test/1", null, null, "test"},
      {new PathParamResource(), "/a/te%20st/2", null, null, "te%20st"},
      {new PathParamResource(), "/a/te%20st/1", null, null, "te st"},
      {new PathParamResource(), "/a/test/3", null, null, newArrayList("test")},
      {new PathParamResource(), "/a/test/4", null, null, newHashSet("test")},
      {new PathParamResource(), "/a/test/5", null, null, newTreeSet(newArrayList("test"))},
      {new PathParamResource(), "/a/123/6", null, null, 123},
      {new PathParamResource(), "/a/foo/7/bar", null, null, "foobar"},
      {new PathParamResource(), "/a/123/8", null, null, 123},
      {new QueryParamResource(), "/b/1?x=test", null, null, "test"},
      {new QueryParamResource(), "/b/2?x=te%20st", null, null, "te%20st"},
      {new QueryParamResource(), "/b/1?x=te%20st", null, null, "te st"},
      {new QueryParamResource(), "/b/3", null, null, "default"},
      {new QueryParamResource(), "/b/4?x=foo&x=bar", null, null, newArrayList("foo", "bar")},
      {new QueryParamResource(), "/b/5?x=foo&x=bar", null, null, newHashSet("foo", "bar")},
      {
        new QueryParamResource(),
        "/b/6?x=foo&x=bar",
        null,
        null,
        newTreeSet(newArrayList("foo", "bar"))
      },
      {new QueryParamResource(), "/b/7?x=123", null, null, 123},
      {new QueryParamResource(), "/b/8?x=foo&y=bar", null, null, "foobar"},
      {new QueryParamResource(), "/b/9?x=123", null, null, 123},
      {new MatrixParamResource(), "/c/1;x=test", null, null, "test"},
      {new MatrixParamResource(), "/c/2;x=te%20st", null, null, "te%20st"},
      {new MatrixParamResource(), "/c/1;x=te%20st", null, null, "te st"},
      {new MatrixParamResource(), "/c/3", null, null, "default"},
      {new MatrixParamResource(), "/c/4;x=foo;x=bar", null, null, newArrayList("foo", "bar")},
      {new MatrixParamResource(), "/c/5;x=foo;x=bar", null, null, newHashSet("foo", "bar")},
      {
        new MatrixParamResource(),
        "/c/6;x=foo;x=bar",
        null,
        null,
        newTreeSet(newArrayList("foo", "bar"))
      },
      {new MatrixParamResource(), "/c/7;x=123", null, null, 123},
      {new MatrixParamResource(), "/c/8;x=foo;y=bar", null, null, "foobar"},
      {new MatrixParamResource(), "/c/9;x=123", null, null, 123},
      {
        new CookieParamResource(),
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        null,
        "test"
      },
      {new CookieParamResource(), "/d/2", null, null, "default"},
      {
        new CookieParamResource(),
        "/d/3",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        null,
        newArrayList("test")
      },
      {
        new CookieParamResource(),
        "/d/4",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        null,
        newHashSet("test")
      },
      {
        new CookieParamResource(),
        "/d/5",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        null,
        newTreeSet(newArrayList("test"))
      },
      {
        new CookieParamResource(),
        "/d/6",
        ImmutableMap.of("Cookie", newArrayList("x=123")),
        null,
        123
      },
      {
        new CookieParamResource(),
        "/d/7",
        ImmutableMap.of("Cookie", newArrayList("x=foo,y=bar")),
        null,
        "foobar"
      },
      {
        new CookieParamResource(),
        "/d/8",
        ImmutableMap.of("Cookie", newArrayList("x=123")),
        null,
        123
      },
      {
        new CookieParamResource(),
        "/d/9",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        null,
        new Cookie("x", "test")
      },
      {new HeaderParamResource(), "/e/1", ImmutableMap.of("x", newArrayList("test")), null, "test"},
      {new HeaderParamResource(), "/e/2", null, null, "default"},
      {
        new HeaderParamResource(),
        "/e/3",
        ImmutableMap.of("x", newArrayList("foo", "bar")),
        null,
        newArrayList("foo", "bar")
      },
      {
        new HeaderParamResource(),
        "/e/4",
        ImmutableMap.of("x", newArrayList("foo", "bar")),
        null,
        newHashSet("foo", "bar")
      },
      {
        new HeaderParamResource(),
        "/e/5",
        ImmutableMap.of("x", newArrayList("foo", "bar")),
        null,
        newTreeSet(newArrayList("foo", "bar"))
      },
      {new HeaderParamResource(), "/e/6", ImmutableMap.of("x", newArrayList("123")), null, 123},
      {
        new HeaderParamResource(),
        "/e/7",
        ImmutableMap.of("x", newArrayList("foo"), "y", newArrayList("bar")),
        null,
        "foobar"
      },
      {new HeaderParamResource(), "/e/8", ImmutableMap.of("x", newArrayList("123")), null, 123},
      {
        new FormParamResource(),
        "/f/1",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=test",
        "test"
      },
      {
        new FormParamResource(),
        "/f/2",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=te%20st",
        "te%20st"
      },
      {
        new FormParamResource(),
        "/f/1",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=te%20st",
        "te st"
      },
      {
        new FormParamResource(),
        "/f/3",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "",
        "default"
      },
      {
        new FormParamResource(),
        "/f/4",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=foo&x=bar",
        newArrayList("foo", "bar")
      },
      {
        new FormParamResource(),
        "/f/5",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=foo&x=bar",
        newHashSet("foo", "bar")
      },
      {
        new FormParamResource(),
        "/f/6",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=foo&x=bar",
        newTreeSet(newArrayList("foo", "bar"))
      },
      {
        new FormParamResource(),
        "/f/7",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=123",
        123
      },
      {
        new FormParamResource(),
        "/f/8",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=foo&y=bar",
        "foobar"
      },
      {
        new FormParamResource(),
        "/f/9",
        ImmutableMap.of("Content-Type", newArrayList("application/x-www-form-urlencoded")),
        "x=123",
        123
      },
    };
  }

  @Test
  public void injectsUriInfo() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new ContextParamResource());
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", UriInfo.class),
        response.getEntity() instanceof UriInfo);
  }

  @Test
  public void injectsRequest() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new ContextParamResource());
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/2", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", Request.class),
        response.getEntity() instanceof Request);
  }

  @Test
  public void injectsHttpHeaders() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new ContextParamResource());
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/3", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", HttpHeaders.class),
        response.getEntity() instanceof HttpHeaders);
  }

  @Test
  public void injectsSecurityContext() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new ContextParamResource());
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/4", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", SecurityContext.class),
        response.getEntity() instanceof SecurityContext);
  }

  @Test
  public void injectsProviders() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new ContextParamResource());
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/5", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", Providers.class),
        response.getEntity() instanceof Providers);
  }

  @Test
  public void injectsApplication() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Object> getSingletons() {
            return newHashSet(new ContextParamResource());
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/6", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", Application.class),
        response.getEntity() instanceof Application);
  }

  @Path("a/{x}")
  public static class PathParamResource {
    @Path("1")
    @POST
    public String m1(@PathParam("x") String x) {
      return x;
    }

    @Path("2")
    @POST
    public String m2(@Encoded @PathParam("x") String x) {
      return x;
    }

    @Path("3")
    @POST
    public GenericEntity<List<String>> m3(@PathParam("x") List<String> x) {
      return new GenericEntity<List<String>>(x) {};
    }

    @Path("4")
    @POST
    public GenericEntity<Set<String>> m4(@PathParam("x") Set<String> x) {
      return new GenericEntity<Set<String>>(x) {};
    }

    @Path("5")
    @POST
    public GenericEntity<SortedSet<String>> m5(@PathParam("x") SortedSet<String> x) {
      return new GenericEntity<SortedSet<String>>(x) {};
    }

    @Path("6")
    @POST
    public Integer m6(@PathParam("x") Integer x) {
      return x;
    }

    @Path("7/{y}")
    @POST
    public String m7(@PathParam("x") String x, @PathParam("y") String y) {
      return x + y;
    }

    @Path("8")
    @POST
    public int m8(@PathParam("x") int x) {
      return x;
    }
  }

  @Path("b")
  public static class QueryParamResource {
    @Path("1")
    @POST
    public String m1(@QueryParam("x") String x) {
      return x;
    }

    @Path("2")
    @POST
    public String m2(@Encoded @QueryParam("x") String x) {
      return x;
    }

    @Path("3")
    @POST
    public String m3(@DefaultValue("default") @QueryParam("x") String x) {
      return x;
    }

    @Path("4")
    @POST
    public GenericEntity<List<String>> m4(@QueryParam("x") List<String> x) {
      return new GenericEntity<List<String>>(x) {};
    }

    @Path("5")
    @POST
    public GenericEntity<Set<String>> m5(@QueryParam("x") Set<String> x) {
      return new GenericEntity<Set<String>>(x) {};
    }

    @Path("6")
    @POST
    public GenericEntity<SortedSet<String>> m6(@QueryParam("x") SortedSet<String> x) {
      return new GenericEntity<SortedSet<String>>(x) {};
    }

    @Path("7")
    @POST
    public Integer m7(@QueryParam("x") Integer x) {
      return x;
    }

    @Path("8")
    @POST
    public String m8(@QueryParam("x") String x, @QueryParam("y") String y) {
      return x + y;
    }

    @Path("9")
    @POST
    public int m9(@QueryParam("x") int x) {
      return x;
    }
  }

  @Path("c")
  public static class MatrixParamResource {
    @Path("1")
    @POST
    public String m1(@MatrixParam("x") String x) {
      return x;
    }

    @Path("2")
    @POST
    public String m2(@Encoded @MatrixParam("x") String x) {
      return x;
    }

    @Path("3")
    @POST
    public String m3(@DefaultValue("default") @MatrixParam("x") String x) {
      return x;
    }

    @Path("4")
    @POST
    public GenericEntity<List<String>> m4(@MatrixParam("x") List<String> x) {
      return new GenericEntity<List<String>>(x) {};
    }

    @Path("5")
    @POST
    public GenericEntity<Set<String>> m5(@MatrixParam("x") Set<String> x) {
      return new GenericEntity<Set<String>>(x) {};
    }

    @Path("6")
    @POST
    public GenericEntity<SortedSet<String>> m6(@MatrixParam("x") SortedSet<String> x) {
      return new GenericEntity<SortedSet<String>>(x) {};
    }

    @Path("7")
    @POST
    public Integer m7(@MatrixParam("x") Integer x) {
      return x;
    }

    @Path("8")
    @POST
    public String m8(@MatrixParam("x") String x, @MatrixParam("y") String y) {
      return x + y;
    }

    @Path("9")
    @POST
    public int m9(@MatrixParam("x") int x) {
      return x;
    }
  }

  @Path("d")
  public static class CookieParamResource {
    @Path("1")
    @POST
    public String m1(@CookieParam("x") String x) {
      return x;
    }

    @Path("2")
    @POST
    public String m2(@DefaultValue("default") @CookieParam("x") String x) {
      return x;
    }

    @Path("3")
    @POST
    public GenericEntity<List<String>> m3(@CookieParam("x") List<String> x) {
      return new GenericEntity<List<String>>(x) {};
    }

    @Path("4")
    @POST
    public GenericEntity<Set<String>> m4(@CookieParam("x") Set<String> x) {
      return new GenericEntity<Set<String>>(x) {};
    }

    @Path("5")
    @POST
    public GenericEntity<SortedSet<String>> m5(@CookieParam("x") SortedSet<String> x) {
      return new GenericEntity<SortedSet<String>>(x) {};
    }

    @Path("6")
    @POST
    public Integer m6(@CookieParam("x") Integer x) {
      return x;
    }

    @Path("7")
    @POST
    public String m7(@CookieParam("x") String x, @CookieParam("y") String y) {
      return x + y;
    }

    @Path("8")
    @POST
    public int m8(@CookieParam("x") int x) {
      return x;
    }

    @Path("9")
    @POST
    public Cookie m9(@CookieParam("x") Cookie x) {
      return x;
    }
  }

  @Path("e")
  public static class HeaderParamResource {
    @Path("1")
    @POST
    public String m1(@HeaderParam("x") String x) {
      return x;
    }

    @Path("2")
    @POST
    public String m2(@DefaultValue("default") @HeaderParam("x") String x) {
      return x;
    }

    @Path("3")
    @POST
    public GenericEntity<List<String>> m3(@HeaderParam("x") List<String> x) {
      return new GenericEntity<List<String>>(x) {};
    }

    @Path("4")
    @POST
    public GenericEntity<Set<String>> m4(@HeaderParam("x") Set<String> x) {
      return new GenericEntity<Set<String>>(x) {};
    }

    @Path("5")
    @POST
    public GenericEntity<SortedSet<String>> m5(@HeaderParam("x") SortedSet<String> x) {
      return new GenericEntity<SortedSet<String>>(x) {};
    }

    @Path("6")
    @POST
    public Integer m6(@HeaderParam("x") Integer x) {
      return x;
    }

    @Path("7")
    @POST
    public String m7(@HeaderParam("x") String x, @HeaderParam("y") String y) {
      return x + y;
    }

    @Path("8")
    @POST
    public int m8(@HeaderParam("x") int x) {
      return x;
    }
  }

  @Path("f")
  public static class FormParamResource {
    @Path("1")
    @POST
    public String m1(@FormParam("x") String x) {
      return x;
    }

    @Path("2")
    @POST
    public String m2(@Encoded @FormParam("x") String x) {
      return x;
    }

    @Path("3")
    @POST
    public String m3(@DefaultValue("default") @FormParam("x") String x) {
      return x;
    }

    @Path("4")
    @POST
    public GenericEntity<List<String>> m4(@FormParam("x") List<String> x) {
      return new GenericEntity<List<String>>(x) {};
    }

    @Path("5")
    @POST
    public GenericEntity<Set<String>> m5(@FormParam("x") Set<String> x) {
      return new GenericEntity<Set<String>>(x) {};
    }

    @Path("6")
    @POST
    public GenericEntity<SortedSet<String>> m6(@FormParam("x") SortedSet<String> x) {
      return new GenericEntity<SortedSet<String>>(x) {};
    }

    @Path("7")
    @POST
    public Integer m7(@FormParam("x") Integer x) {
      return x;
    }

    @Path("8")
    @POST
    public String m8(@FormParam("x") String x, @FormParam("y") String y) {
      return x + y;
    }

    @Path("9")
    @POST
    public int m9(@FormParam("x") int x) {
      return x;
    }
  }

  @Path("g")
  public static class ContextParamResource {
    @Path("1")
    @POST
    public UriInfo m1(@Context UriInfo uriInfo) {
      return uriInfo;
    }

    @Path("2")
    @POST
    public Request m2(@Context Request request) {
      return request;
    }

    @Path("3")
    @POST
    public HttpHeaders m3(@Context HttpHeaders httpHeaders) {
      return httpHeaders;
    }

    @Path("4")
    @POST
    public SecurityContext m4(@Context SecurityContext securityContext) {
      return securityContext;
    }

    @Path("5")
    @POST
    public Providers m5(@Context Providers providers) {
      return providers;
    }

    @Path("6")
    @POST
    public Application m6(@Context Application application) {
      return application;
    }
  }
}
