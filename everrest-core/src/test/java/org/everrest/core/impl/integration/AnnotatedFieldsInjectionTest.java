/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.integration;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class AnnotatedFieldsInjectionTest extends BaseTest {

    @UseDataProvider("injectParametersTestData")
    @Test
    public void injectsParameters(Class<?> resource, String path, Map<String, List<String>> requestHeaders, Object responseEntity) throws Exception {
        processor.addApplication(new Application() {
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
        return new Object[][]{
                {StringPathParamResource.class,             "/a/test/1",    null, "test"},
                {EncodedStringPathParamResource.class,      "/a/te%20st/1", null, "te%20st"},
                {StringPathParamResource.class,             "/a/te%20st/1", null, "te st"},
                {ListOfStringsPathParamResource.class,      "/a/test/1",    null, newArrayList("test")},
                {SetOfStringsPathParamResource.class,       "/a/test/1",    null, newHashSet("test")},
                {SortedSetOfStringsPathParamResource.class, "/a/test/1",    null, newTreeSet(newArrayList("test"))},
                {StringValueOfPathParamResource.class,      "/a/123/1",     null, 123},
                {MultiplePathParamResource.class,           "/a/foo/1/bar", null, "foobar"},
                {PrimitivePathParamResource.class,          "/a/123/1",     null, 123},

                {StringQueryParamResource.class,             "/b/1?x=test",      null, "test"},
                {EncodedStringQueryParamResource.class,      "/b/1?x=te%20st",   null, "te%20st"},
                {StringQueryParamResource.class,             "/b/1?x=te%20st",   null, "te st"},
                {DefaultValueQueryParamResource.class,       "/b/1",             null, "default"},
                {ListOfStringsQueryParamResource.class,      "/b/1?x=foo&x=bar", null, newArrayList("foo", "bar")},
                {SetOfStringsQueryParamResource.class,       "/b/1?x=foo&x=bar", null, newHashSet("foo", "bar")},
                {SortedSetOfStringsQueryParamResource.class, "/b/1?x=foo&x=bar", null, newTreeSet(newArrayList("foo", "bar"))},
                {StringValueOfQueryParamResource.class,      "/b/1?x=123",       null, 123},
                {MultipleQueryParamResource.class,           "/b/1?x=foo&y=bar", null, "foobar"},
                {PrimitiveQueryParamResource.class,          "/b/1?x=123",       null, 123},

                {StringMatrixParamResource.class,             "/c/1;x=test",      null, "test"},
                {EncodedStringMatrixParamResource.class,      "/c/1;x=te%20st",   null, "te%20st"},
                {StringMatrixParamResource.class,             "/c/1;x=te%20st",   null, "te st"},
                {DefaultValueMatrixParamResource.class,       "/c/1",             null, "default"},
                {ListOfStringsMatrixParamResource.class,      "/c/1;x=foo;x=bar", null, newArrayList("foo", "bar")},
                {SetOfStringsMatrixParamResource.class,       "/c/1;x=foo;x=bar", null, newHashSet("foo", "bar")},
                {SortedSetOfStringsMatrixParamResource.class, "/c/1;x=foo;x=bar", null, newTreeSet(newArrayList("foo", "bar"))},
                {StringValueOfMatrixParamResource.class,      "/c/1;x=123",       null, 123},
                {MultipleMatrixParamResource.class,           "/c/1;x=foo;y=bar", null, "foobar"},
                {PrimitiveMatrixParamResource.class,          "/c/1;x=123",       null, 123},

                {CookieCookieParamResource.class,             "/d/1", ImmutableMap.of("Cookie", newArrayList("x=test")),      new Cookie("x", "test")},
                {StringCookieParamResource.class,             "/d/1", ImmutableMap.of("Cookie", newArrayList("x=test")),      "test"},
                {DefaultValueCookieParamResource.class,       "/d/1", null,                                                   "default"},
                {ListOfStringsCookieParamResource.class,      "/d/1", ImmutableMap.of("Cookie", newArrayList("x=test")),      newArrayList("test")},
                {SetOfStringsCookieParamResource.class,       "/d/1", ImmutableMap.of("Cookie", newArrayList("x=test")),      newHashSet("test")},
                {SortedSetOfStringsCookieParamResource.class, "/d/1", ImmutableMap.of("Cookie", newArrayList("x=test")),      newTreeSet(newArrayList("test"))},
                {StringValueOfCookieParamResource.class,      "/d/1", ImmutableMap.of("Cookie", newArrayList("x=123")),       123},
                {MultipleCookieParamResource.class,           "/d/1", ImmutableMap.of("Cookie", newArrayList("x=foo,y=bar")), "foobar"},
                {PrimitiveCookieParamResource.class,          "/d/1", ImmutableMap.of("Cookie", newArrayList("x=123")),       123},

                {StringHeaderParamResource.class,             "/e/1", ImmutableMap.of("x", newArrayList("test")),                          "test"},
                {DefaultValueHeaderParamResource.class,       "/e/1", null,                                                                "default"},
                {ListOfStringsHeaderParamResource.class,      "/e/1", ImmutableMap.of("x", newArrayList("foo", "bar")),                    newArrayList("foo", "bar")},
                {SetOfStringsHeaderParamResource.class,       "/e/1", ImmutableMap.of("x", newArrayList("foo", "bar")),                    newHashSet("foo", "bar")},
                {SortedSetOfStringsHeaderParamResource.class, "/e/1", ImmutableMap.of("x", newArrayList("foo", "bar")),                    newTreeSet(newArrayList("foo", "bar"))},
                {StringValueOfHeaderParamResource.class,      "/e/1", ImmutableMap.of("x", newArrayList("123")),                           123},
                {MultipleHeaderParamResource.class,           "/e/1", ImmutableMap.of("x", newArrayList("foo"), "y", newArrayList("bar")), "foobar"},
                {PrimitiveHeaderParamResource.class,          "/e/1", ImmutableMap.of("x", newArrayList("123")),                           123},
        };
    }

    @Test
    public void injectsUriInfo() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(UriInfoResource.class);
            }
        });
        ContainerResponse response = launcher.service("POST", "/f/1", "", null, null, null);

        assertTrue(String.format("Expected %s injected", UriInfo.class), response.getEntity() instanceof UriInfo);
    }

    @Test
    public void injectsRequest() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(RequestResource.class);
            }
        });
        ContainerResponse response = launcher.service("POST", "/g/1", "", null, null, null);

        assertTrue(String.format("Expected %s injected", Request.class), response.getEntity() instanceof Request);
    }

    @Test
    public void injectsHttpHeaders() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(HttpHeadersResource.class);
            }
        });
        ContainerResponse response = launcher.service("POST", "/h/1", "", null, null, null);

        assertTrue(String.format("Expected %s injected", HttpHeaders.class), response.getEntity() instanceof HttpHeaders);
    }

    @Test
    public void injectsSecurityContext() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(SecurityContextResource.class);
            }
        });
        ContainerResponse response = launcher.service("POST", "/i/1", "", null, null, null);

        assertTrue(String.format("Expected %s injected", SecurityContext.class), response.getEntity() instanceof SecurityContext);
    }

    @Test
    public void injectsProviders() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(ProvidersResource.class);
            }
        });
        ContainerResponse response = launcher.service("POST", "/j/1", "", null, null, null);

        assertTrue(String.format("Expected %s injected", Providers.class), response.getEntity() instanceof Providers);
    }

    @Test
    public void injectsApplication() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return newHashSet(ApplicationResource.class);
            }
        });
        ContainerResponse response = launcher.service("POST", "/k/1", "", null, null, null);

        assertTrue(String.format("Expected %s injected", Application.class), response.getEntity() instanceof Application);
    }


    @Path("a/{x}")
    public static class StringPathParamResource {
        @PathParam("x")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("a/{x}")
    public static class EncodedStringPathParamResource {
        @PathParam("x")
        @Encoded
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("a/{x}")
    public static class ListOfStringsPathParamResource {
        @PathParam("x")
        private List<String> x;

        @Path("1")
        @POST
        public GenericEntity<List<String>> m1() {
            return new GenericEntity<List<String>>(x) {
            };
        }
    }

    @Path("a/{x}")
    public static class SetOfStringsPathParamResource {
        @PathParam("x")
        private Set<String> x;

        @Path("1")
        @POST
        public GenericEntity<Set<String>> m1() {
            return new GenericEntity<Set<String>>(x) {
            };
        }
    }

    @Path("a/{x}")
    public static class SortedSetOfStringsPathParamResource {
        @PathParam("x")
        private SortedSet<String> x;

        @Path("1")
        @POST
        public GenericEntity<SortedSet<String>> m1() {
            return new GenericEntity<SortedSet<String>>(x) {
            };
        }
    }

    @Path("a/{x}")
    public static class StringValueOfPathParamResource {
        @PathParam("x")
        private Integer x;

        @Path("1")
        @POST
        public Integer m1() {
            return x;
        }
    }

    @Path("a/{x}/1/{y}")
    public static class MultiplePathParamResource {
        @PathParam("x")
        private String x;
        @PathParam("y")
        private String y;

        @POST
        public String m1() {
            return x + y;
        }
    }

    @Path("a/{x}")
    public static class PrimitivePathParamResource {
        @PathParam("x")
        private int x;

        @Path("1")
        @POST
        public int m1() {
            return x;
        }
    }


    @Path("b")
    public static class StringQueryParamResource {
        @QueryParam("x")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("b")
    public static class EncodedStringQueryParamResource {
        @QueryParam("x")
        @Encoded
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("b")
    public static class DefaultValueQueryParamResource {
        @QueryParam("x")
        @DefaultValue("default")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("b")
    public static class ListOfStringsQueryParamResource {
        @QueryParam("x")
        private List<String> x;

        @Path("1")
        @POST
        public GenericEntity<List<String>> m1() {
            return new GenericEntity<List<String>>(x) {
            };
        }
    }

    @Path("b")
    public static class SetOfStringsQueryParamResource {
        @QueryParam("x")
        private Set<String> x;

        @Path("1")
        @POST
        public GenericEntity<Set<String>> m1() {
            return new GenericEntity<Set<String>>(x) {
            };
        }
    }

    @Path("b")
    public static class SortedSetOfStringsQueryParamResource {
        @QueryParam("x")
        private SortedSet<String> x;

        @Path("1")
        @POST
        public GenericEntity<SortedSet<String>> m1() {
            return new GenericEntity<SortedSet<String>>(x) {
            };
        }
    }

    @Path("b")
    public static class StringValueOfQueryParamResource {
        @QueryParam("x")
        private Integer x;

        @Path("1")
        @POST
        public Integer m1() {
            return x;
        }
    }

    @Path("b")
    public static class MultipleQueryParamResource {
        @QueryParam("x")
        private String x;
        @QueryParam("y")
        private String y;

        @Path("1")
        @POST
        public String m1() {
            return x + y;
        }
    }

    @Path("b")
    public static class PrimitiveQueryParamResource {
        @QueryParam("x")
        private int x;

        @Path("1")
        @POST
        public int m1() {
            return x;
        }
    }


    @Path("c")
    public static class StringMatrixParamResource {
        @MatrixParam("x")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("c")
    public static class EncodedStringMatrixParamResource {
        @MatrixParam("x")
        @Encoded
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("c")
    public static class DefaultValueMatrixParamResource {
        @MatrixParam("x")
        @DefaultValue("default")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("c")
    public static class ListOfStringsMatrixParamResource {
        @MatrixParam("x")
        private List<String> x;

        @Path("1")
        @POST
        public GenericEntity<List<String>> m1() {
            return new GenericEntity<List<String>>(x) {
            };
        }
    }

    @Path("c")
    public static class SetOfStringsMatrixParamResource {
        @MatrixParam("x")
        private Set<String> x;

        @Path("1")
        @POST
        public GenericEntity<Set<String>> m1() {
            return new GenericEntity<Set<String>>(x) {
            };
        }
    }

    @Path("c")
    public static class SortedSetOfStringsMatrixParamResource {
        @MatrixParam("x")
        private SortedSet<String> x;

        @Path("1")
        @POST
        public GenericEntity<SortedSet<String>> m1() {
            return new GenericEntity<SortedSet<String>>(x) {
            };
        }
    }

    @Path("c")
    public static class StringValueOfMatrixParamResource {
        @MatrixParam("x")
        private Integer x;

        @Path("1")
        @POST
        public Integer m1() {
            return x;
        }
    }

    @Path("c")
    public static class MultipleMatrixParamResource {
        @MatrixParam("x")
        private String x;
        @MatrixParam("y")
        private String y;

        @Path("1")
        @POST
        public String m1() {
            return x + y;
        }
    }

    @Path("c")
    public static class PrimitiveMatrixParamResource {
        @MatrixParam("x")
        private int x;

        @Path("1")
        @POST
        public int m1() {
            return x;
        }
    }


    @Path("d")
    public static class CookieCookieParamResource {
        @CookieParam("x")
        private Cookie x;

        @Path("1")
        @POST
        public Cookie m1() {
            return x;
        }
    }

    @Path("d")
    public static class StringCookieParamResource {
        @CookieParam("x")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("d")
    public static class DefaultValueCookieParamResource {
        @CookieParam("x")
        @DefaultValue("default")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("d")
    public static class ListOfStringsCookieParamResource {
        @CookieParam("x")
        private List<String> x;

        @Path("1")
        @POST
        public GenericEntity<List<String>> m1() {
            return new GenericEntity<List<String>>(x) {
            };
        }
    }

    @Path("d")
    public static class SetOfStringsCookieParamResource {
        @CookieParam("x")
        private Set<String> x;

        @Path("1")
        @POST
        public GenericEntity<Set<String>> m1() {
            return new GenericEntity<Set<String>>(x) {
            };
        }
    }

    @Path("d")
    public static class SortedSetOfStringsCookieParamResource {
        @CookieParam("x")
        private SortedSet<String> x;

        @Path("1")
        @POST
        public GenericEntity<SortedSet<String>> m1() {
            return new GenericEntity<SortedSet<String>>(x) {
            };
        }
    }

    @Path("d")
    public static class StringValueOfCookieParamResource {
        @CookieParam("x")
        private Integer x;

        @Path("1")
        @POST
        public Integer m1() {
            return x;
        }
    }

    @Path("d")
    public static class MultipleCookieParamResource {
        @CookieParam("x")
        private String x;
        @CookieParam("y")
        private String y;

        @Path("1")
        @POST
        public String m1() {
            return x + y;
        }
    }

    @Path("d")
    public static class PrimitiveCookieParamResource {
        @CookieParam("x")
        private int x;

        @Path("1")
        @POST
        public int m1() {
            return x;
        }
    }


    @Path("e")
    public static class StringHeaderParamResource {
        @HeaderParam("x")
        private String x;

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
        @HeaderParam("x")
        @DefaultValue("default")
        private String x;

        @Path("1")
        @POST
        public String m1() {
            return x;
        }
    }

    @Path("e")
    public static class ListOfStringsHeaderParamResource {
        @HeaderParam("x")
        private List<String> x;

        @Path("1")
        @POST
        public GenericEntity<List<String>> m1() {
            return new GenericEntity<List<String>>(x) {
            };
        }
    }

    @Path("e")
    public static class SetOfStringsHeaderParamResource {
        @HeaderParam("x")
        private Set<String> x;

        @Path("1")
        @POST
        public GenericEntity<Set<String>> m1() {
            return new GenericEntity<Set<String>>(x) {
            };
        }
    }

    @Path("e")
    public static class SortedSetOfStringsHeaderParamResource {
        @HeaderParam("x")
        private SortedSet<String> x;

        @Path("1")
        @POST
        public GenericEntity<SortedSet<String>> m1() {
            return new GenericEntity<SortedSet<String>>(x) {
            };
        }
    }

    @Path("e")
    public static class StringValueOfHeaderParamResource {
        @HeaderParam("x")
        private Integer x;

        @Path("1")
        @POST
        public Integer m1() {
            return x;
        }
    }

    @Path("e")
    public static class MultipleHeaderParamResource {
        @HeaderParam("x")
        private String x;
        @HeaderParam("y")
        private String y;

        @Path("1")
        @POST
        public String m1() {
            return x + y;
        }
    }

    @Path("e")
    public static class PrimitiveHeaderParamResource {
        @HeaderParam("x")
        private int x;

        @Path("1")
        @POST
        public int m1() {
            return x;
        }
    }


    @Path("f")
    public static class UriInfoResource {
        @Context
        private UriInfo uriInfo;

        @Path("1")
        @POST
        public UriInfo m1() {
            return uriInfo;
        }
    }

    @Path("g")
    public static class RequestResource {
        @Context
        private Request request;

        @Path("1")
        @POST
        public Request m1() {
            return request;
        }
    }

    @Path("h")
    public static class HttpHeadersResource {
        @Context
        private HttpHeaders httpHeaders;

        @Path("1")
        @POST
        public HttpHeaders m1() {
            return httpHeaders;
        }
    }

    @Path("i")
    public static class SecurityContextResource {
        @Context
        private SecurityContext securityContext;

        @Path("1")
        @POST
        public SecurityContext m1() {
            return securityContext;
        }
    }

    @Path("j")
    public static class ProvidersResource {
        @Context
        private Providers providers;

        @Path("1")
        @POST
        public Providers m1() {
            return providers;
        }
    }

    @Path("k")
    public static class ApplicationResource {
        @Context
        private Application application;

        @Path("1")
        @POST
        public Application m1() {
            return application;
        }
    }
}
