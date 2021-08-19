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
public class ConstructorParametersInjectionTest extends BaseTest {

  @UseDataProvider("injectParametersTestData")
  @Test
  public void injectsParameters(
      Class<?> resource,
      String path,
      Map<String, List<String>> requestHeaders,
      Object responseEntity)
      throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(resource);
          }
        });
    ContainerResponse response = launcher.service("POST", path, "", requestHeaders, null, null);

    assertEquals(responseEntity, response.getEntity());
  }

  @DataProvider
  public static Object[][] injectParametersTestData() {
    return new Object[][] {
      {StringPathParamResource.class, "/a/test/1", null, "test"},
      {EncodedStringPathParamResource.class, "/a/te%20st/1", null, "te%20st"},
      {StringPathParamResource.class, "/a/te%20st/1", null, "te st"},
      {ListOfStringsPathParamResource.class, "/a/test/1", null, newArrayList("test")},
      {SetOfStringsPathParamResource.class, "/a/test/1", null, newHashSet("test")},
      {
        SortedSetOfStringsPathParamResource.class,
        "/a/test/1",
        null,
        newTreeSet(newArrayList("test"))
      },
      {StringValueOfPathParamResource.class, "/a/123/1", null, 123},
      {MultiplePathParamResource.class, "/a/foo/1/bar", null, "foobar"},
      {PrimitivePathParamResource.class, "/a/123/1", null, 123},
      {StringQueryParamResource.class, "/b/1?x=test", null, "test"},
      {EncodedStringQueryParamResource.class, "/b/1?x=te%20st", null, "te%20st"},
      {StringQueryParamResource.class, "/b/1?x=te%20st", null, "te st"},
      {DefaultValueQueryParamResource.class, "/b/1", null, "default"},
      {ListOfStringsQueryParamResource.class, "/b/1?x=foo&x=bar", null, newArrayList("foo", "bar")},
      {SetOfStringsQueryParamResource.class, "/b/1?x=foo&x=bar", null, newHashSet("foo", "bar")},
      {
        SortedSetOfStringsQueryParamResource.class,
        "/b/1?x=foo&x=bar",
        null,
        newTreeSet(newArrayList("foo", "bar"))
      },
      {StringValueOfQueryParamResource.class, "/b/1?x=123", null, 123},
      {MultipleQueryParamResource.class, "/b/1?x=foo&y=bar", null, "foobar"},
      {PrimitiveQueryParamResource.class, "/b/1?x=123", null, 123},
      {StringMatrixParamResource.class, "/c/1;x=test", null, "test"},
      {EncodedStringMatrixParamResource.class, "/c/1;x=te%20st", null, "te%20st"},
      {StringMatrixParamResource.class, "/c/1;x=te%20st", null, "te st"},
      {DefaultValueMatrixParamResource.class, "/c/1", null, "default"},
      {
        ListOfStringsMatrixParamResource.class, "/c/1;x=foo;x=bar", null, newArrayList("foo", "bar")
      },
      {SetOfStringsMatrixParamResource.class, "/c/1;x=foo;x=bar", null, newHashSet("foo", "bar")},
      {
        SortedSetOfStringsMatrixParamResource.class,
        "/c/1;x=foo;x=bar",
        null,
        newTreeSet(newArrayList("foo", "bar"))
      },
      {StringValueOfMatrixParamResource.class, "/c/1;x=123", null, 123},
      {MultipleMatrixParamResource.class, "/c/1;x=foo;y=bar", null, "foobar"},
      {PrimitiveMatrixParamResource.class, "/c/1;x=123", null, 123},
      {
        CookieCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        new Cookie("x", "test")
      },
      {
        StringCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        "test"
      },
      {DefaultValueCookieParamResource.class, "/d/1", null, "default"},
      {
        ListOfStringsCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        newArrayList("test")
      },
      {
        SetOfStringsCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        newHashSet("test")
      },
      {
        SortedSetOfStringsCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=test")),
        newTreeSet(newArrayList("test"))
      },
      {
        StringValueOfCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=123")),
        123
      },
      {
        MultipleCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=foo,y=bar")),
        "foobar"
      },
      {
        PrimitiveCookieParamResource.class,
        "/d/1",
        ImmutableMap.of("Cookie", newArrayList("x=123")),
        123
      },
      {StringHeaderParamResource.class, "/e/1", ImmutableMap.of("x", newArrayList("test")), "test"},
      {DefaultValueHeaderParamResource.class, "/e/1", null, "default"},
      {
        ListOfStringsHeaderParamResource.class,
        "/e/1",
        ImmutableMap.of("x", newArrayList("foo", "bar")),
        newArrayList("foo", "bar")
      },
      {
        SetOfStringsHeaderParamResource.class,
        "/e/1",
        ImmutableMap.of("x", newArrayList("foo", "bar")),
        newHashSet("foo", "bar")
      },
      {
        SortedSetOfStringsHeaderParamResource.class,
        "/e/1",
        ImmutableMap.of("x", newArrayList("foo", "bar")),
        newTreeSet(newArrayList("foo", "bar"))
      },
      {
        StringValueOfHeaderParamResource.class,
        "/e/1",
        ImmutableMap.of("x", newArrayList("123")),
        123
      },
      {
        MultipleHeaderParamResource.class,
        "/e/1",
        ImmutableMap.of("x", newArrayList("foo"), "y", newArrayList("bar")),
        "foobar"
      },
      {PrimitiveHeaderParamResource.class, "/e/1", ImmutableMap.of("x", newArrayList("123")), 123},
    };
  }

  @Test
  public void injectsUriInfo() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(UriInfoResource.class);
          }
        });
    ContainerResponse response = launcher.service("POST", "/f/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", UriInfo.class),
        response.getEntity() instanceof UriInfo);
  }

  @Test
  public void injectsRequest() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(RequestResource.class);
          }
        });
    ContainerResponse response = launcher.service("POST", "/g/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", Request.class),
        response.getEntity() instanceof Request);
  }

  @Test
  public void injectsHttpHeaders() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(HttpHeadersResource.class);
          }
        });
    ContainerResponse response = launcher.service("POST", "/h/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", HttpHeaders.class),
        response.getEntity() instanceof HttpHeaders);
  }

  @Test
  public void injectsSecurityContext() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(SecurityContextResource.class);
          }
        });
    ContainerResponse response = launcher.service("POST", "/i/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", SecurityContext.class),
        response.getEntity() instanceof SecurityContext);
  }

  @Test
  public void injectsProviders() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(ProvidersResource.class);
          }
        });
    ContainerResponse response = launcher.service("POST", "/j/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", Providers.class),
        response.getEntity() instanceof Providers);
  }

  @Test
  public void injectsApplication() throws Exception {
    processor.addApplication(
        new Application() {
          @Override
          public Set<Class<?>> getClasses() {
            return newHashSet(ApplicationResource.class);
          }
        });
    ContainerResponse response = launcher.service("POST", "/k/1", "", null, null, null);

    assertTrue(
        String.format("Expected %s injected", Application.class),
        response.getEntity() instanceof Application);
  }

  @Path("a/{x}")
  public static class StringPathParamResource {
    private final String x;

    public StringPathParamResource(@PathParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("a/{x}")
  public static class EncodedStringPathParamResource {
    private final String x;

    public EncodedStringPathParamResource(@Encoded @PathParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("a/{x}")
  public static class ListOfStringsPathParamResource {
    private final List<String> x;

    public ListOfStringsPathParamResource(@PathParam("x") List<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<List<String>> m1() {
      return new GenericEntity<List<String>>(x) {};
    }
  }

  @Path("a/{x}")
  public static class SetOfStringsPathParamResource {
    private final Set<String> x;

    public SetOfStringsPathParamResource(@PathParam("x") Set<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<Set<String>> m1() {
      return new GenericEntity<Set<String>>(x) {};
    }
  }

  @Path("a/{x}")
  public static class SortedSetOfStringsPathParamResource {
    private final SortedSet<String> x;

    public SortedSetOfStringsPathParamResource(@PathParam("x") SortedSet<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<SortedSet<String>> m1() {
      return new GenericEntity<SortedSet<String>>(x) {};
    }
  }

  @Path("a/{x}")
  public static class StringValueOfPathParamResource {
    private final Integer x;

    public StringValueOfPathParamResource(@PathParam("x") Integer x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public Integer m1() {
      return x;
    }
  }

  @Path("a/{x}/1/{y}")
  public static class MultiplePathParamResource {
    private final String x;
    private final String y;

    public MultiplePathParamResource(@PathParam("x") String x, @PathParam("y") String y) {
      this.x = x;
      this.y = y;
    }

    @POST
    public String m1() {
      return x + y;
    }
  }

  @Path("a/{x}")
  public static class PrimitivePathParamResource {
    private final int x;

    public PrimitivePathParamResource(@PathParam("x") int x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public int m1() {
      return x;
    }
  }

  @Path("b")
  public static class StringQueryParamResource {
    private final String x;

    public StringQueryParamResource(@QueryParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("b")
  public static class EncodedStringQueryParamResource {
    private final String x;

    public EncodedStringQueryParamResource(@Encoded @QueryParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("b")
  public static class DefaultValueQueryParamResource {
    private final String x;

    public DefaultValueQueryParamResource(@DefaultValue("default") @QueryParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("b")
  public static class ListOfStringsQueryParamResource {
    private final List<String> x;

    public ListOfStringsQueryParamResource(@QueryParam("x") List<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<List<String>> m1() {
      return new GenericEntity<List<String>>(x) {};
    }
  }

  @Path("b")
  public static class SetOfStringsQueryParamResource {
    private final Set<String> x;

    public SetOfStringsQueryParamResource(@QueryParam("x") Set<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<Set<String>> m1() {
      return new GenericEntity<Set<String>>(x) {};
    }
  }

  @Path("b")
  public static class SortedSetOfStringsQueryParamResource {
    private final SortedSet<String> x;

    public SortedSetOfStringsQueryParamResource(@QueryParam("x") SortedSet<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<SortedSet<String>> m1() {
      return new GenericEntity<SortedSet<String>>(x) {};
    }
  }

  @Path("b")
  public static class StringValueOfQueryParamResource {
    private final Integer x;

    public StringValueOfQueryParamResource(@QueryParam("x") Integer x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public Integer m1() {
      return x;
    }
  }

  @Path("b")
  public static class MultipleQueryParamResource {
    private final String x;
    private final String y;

    public MultipleQueryParamResource(@QueryParam("x") String x, @QueryParam("y") String y) {
      this.x = x;
      this.y = y;
    }

    @Path("1")
    @POST
    public String m1() {
      return x + y;
    }
  }

  @Path("b")
  public static class PrimitiveQueryParamResource {
    private final int x;

    public PrimitiveQueryParamResource(@QueryParam("x") int x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public int m1() {
      return x;
    }
  }

  @Path("c")
  public static class StringMatrixParamResource {
    private final String x;

    public StringMatrixParamResource(@MatrixParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("c")
  public static class EncodedStringMatrixParamResource {
    private final String x;

    public EncodedStringMatrixParamResource(@Encoded @MatrixParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("c")
  public static class DefaultValueMatrixParamResource {
    private final String x;

    public DefaultValueMatrixParamResource(@DefaultValue("default") @MatrixParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("c")
  public static class ListOfStringsMatrixParamResource {
    private final List<String> x;

    public ListOfStringsMatrixParamResource(@MatrixParam("x") List<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<List<String>> m1() {
      return new GenericEntity<List<String>>(x) {};
    }
  }

  @Path("c")
  public static class SetOfStringsMatrixParamResource {
    private final Set<String> x;

    public SetOfStringsMatrixParamResource(@MatrixParam("x") Set<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<Set<String>> m1() {
      return new GenericEntity<Set<String>>(x) {};
    }
  }

  @Path("c")
  public static class SortedSetOfStringsMatrixParamResource {
    private final SortedSet<String> x;

    public SortedSetOfStringsMatrixParamResource(@MatrixParam("x") SortedSet<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<SortedSet<String>> m1() {
      return new GenericEntity<SortedSet<String>>(x) {};
    }
  }

  @Path("c")
  public static class StringValueOfMatrixParamResource {
    private final Integer x;

    public StringValueOfMatrixParamResource(@MatrixParam("x") Integer x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public Integer m1() {
      return x;
    }
  }

  @Path("c")
  public static class MultipleMatrixParamResource {
    private final String x;
    private final String y;

    public MultipleMatrixParamResource(@MatrixParam("x") String x, @MatrixParam("y") String y) {
      this.x = x;
      this.y = y;
    }

    @Path("1")
    @POST
    public String m1() {
      return x + y;
    }
  }

  @Path("c")
  public static class PrimitiveMatrixParamResource {
    private final int x;

    public PrimitiveMatrixParamResource(@MatrixParam("x") int x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public int m1() {
      return x;
    }
  }

  @Path("d")
  public static class CookieCookieParamResource {
    private final Cookie x;

    public CookieCookieParamResource(@CookieParam("x") Cookie x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public Cookie m1() {
      return x;
    }
  }

  @Path("d")
  public static class StringCookieParamResource {
    private final String x;

    public StringCookieParamResource(@CookieParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("d")
  public static class DefaultValueCookieParamResource {
    private final String x;

    public DefaultValueCookieParamResource(@DefaultValue("default") @CookieParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("d")
  public static class ListOfStringsCookieParamResource {
    private final List<String> x;

    public ListOfStringsCookieParamResource(@CookieParam("x") List<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<List<String>> m1() {
      return new GenericEntity<List<String>>(x) {};
    }
  }

  @Path("d")
  public static class SetOfStringsCookieParamResource {
    private final Set<String> x;

    public SetOfStringsCookieParamResource(@CookieParam("x") Set<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<Set<String>> m1() {
      return new GenericEntity<Set<String>>(x) {};
    }
  }

  @Path("d")
  public static class SortedSetOfStringsCookieParamResource {
    private final SortedSet<String> x;

    public SortedSetOfStringsCookieParamResource(@CookieParam("x") SortedSet<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<SortedSet<String>> m1() {
      return new GenericEntity<SortedSet<String>>(x) {};
    }
  }

  @Path("d")
  public static class StringValueOfCookieParamResource {
    private final Integer x;

    public StringValueOfCookieParamResource(@CookieParam("x") Integer x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public Integer m1() {
      return x;
    }
  }

  @Path("d")
  public static class MultipleCookieParamResource {
    private final String x;
    private final String y;

    public MultipleCookieParamResource(@CookieParam("x") String x, @CookieParam("y") String y) {
      this.x = x;
      this.y = y;
    }

    @Path("1")
    @POST
    public String m1() {
      return x + y;
    }
  }

  @Path("d")
  public static class PrimitiveCookieParamResource {
    private final int x;

    public PrimitiveCookieParamResource(@CookieParam("x") int x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public int m1() {
      return x;
    }
  }

  @Path("e")
  public static class StringHeaderParamResource {
    private final String x;

    public StringHeaderParamResource(@HeaderParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("e")
  public static class DefaultValueHeaderParamResource {
    private final String x;

    public DefaultValueHeaderParamResource(@DefaultValue("default") @HeaderParam("x") String x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public String m1() {
      return x;
    }
  }

  @Path("e")
  public static class ListOfStringsHeaderParamResource {
    private final List<String> x;

    public ListOfStringsHeaderParamResource(@HeaderParam("x") List<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<List<String>> m1() {
      return new GenericEntity<List<String>>(x) {};
    }
  }

  @Path("e")
  public static class SetOfStringsHeaderParamResource {
    private final Set<String> x;

    public SetOfStringsHeaderParamResource(@HeaderParam("x") Set<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<Set<String>> m1() {
      return new GenericEntity<Set<String>>(x) {};
    }
  }

  @Path("e")
  public static class SortedSetOfStringsHeaderParamResource {
    private final SortedSet<String> x;

    public SortedSetOfStringsHeaderParamResource(@HeaderParam("x") SortedSet<String> x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public GenericEntity<SortedSet<String>> m1() {
      return new GenericEntity<SortedSet<String>>(x) {};
    }
  }

  @Path("e")
  public static class StringValueOfHeaderParamResource {
    private final Integer x;

    public StringValueOfHeaderParamResource(@HeaderParam("x") Integer x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public Integer m1() {
      return x;
    }
  }

  @Path("e")
  public static class MultipleHeaderParamResource {
    private final String x;
    private final String y;

    public MultipleHeaderParamResource(@HeaderParam("x") String x, @HeaderParam("y") String y) {
      this.x = x;
      this.y = y;
    }

    @Path("1")
    @POST
    public String m1() {
      return x + y;
    }
  }

  @Path("e")
  public static class PrimitiveHeaderParamResource {
    private final int x;

    public PrimitiveHeaderParamResource(@HeaderParam("x") int x) {
      this.x = x;
    }

    @Path("1")
    @POST
    public int m1() {
      return x;
    }
  }

  @Path("f")
  public static class UriInfoResource {
    private final UriInfo uriInfo;

    public UriInfoResource(@Context UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    @Path("1")
    @POST
    public UriInfo m1() {
      return uriInfo;
    }
  }

  @Path("g")
  public static class RequestResource {
    private final Request request;

    public RequestResource(@Context Request request) {
      this.request = request;
    }

    @Path("1")
    @POST
    public Request m1() {
      return request;
    }
  }

  @Path("h")
  public static class HttpHeadersResource {
    private final HttpHeaders httpHeaders;

    public HttpHeadersResource(@Context HttpHeaders httpHeaders) {
      this.httpHeaders = httpHeaders;
    }

    @Path("1")
    @POST
    public HttpHeaders m1() {
      return httpHeaders;
    }
  }

  @Path("i")
  public static class SecurityContextResource {
    private final SecurityContext securityContext;

    public SecurityContextResource(@Context SecurityContext securityContext) {
      this.securityContext = securityContext;
    }

    @Path("1")
    @POST
    public SecurityContext m1() {
      return securityContext;
    }
  }

  @Path("j")
  public static class ProvidersResource {
    private final Providers providers;

    public ProvidersResource(@Context Providers providers) {
      this.providers = providers;
    }

    @Path("1")
    @POST
    public Providers m1() {
      return providers;
    }
  }

  @Path("k")
  public static class ApplicationResource {
    private final Application application;

    public ApplicationResource(@Context Application application) {
      this.application = application;
    }

    @Path("1")
    @POST
    public Application m1() {
      return application;
    }
  }
}
