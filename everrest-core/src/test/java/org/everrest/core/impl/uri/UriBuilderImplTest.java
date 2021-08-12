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
package org.everrest.core.impl.uri;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class UriBuilderImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void replacesScheme() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("https").build();
    assertEquals(URI.create("https://localhost:8080/a/b/c"), uri);
  }

  @Test
  public void removesScheme() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme(null).build();
    assertEquals(URI.create("//localhost:8080/a/b/c"), uri);
  }

  @Test
  public void replacesUserInfo() {
    URI uri = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo("andrew").build();
    assertEquals(URI.create("http://andrew@localhost:8080/a/b/c"), uri);
  }

  @Test
  public void removesUserInfo() {
    URI uri = UriBuilder.fromUri("http://exo@localhost:8080/a/b/c").userInfo(null).build();
    assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);
  }

  @Test
  public void replacesHost() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("exoplatform.org").build();
    assertEquals(URI.create("http://exoplatform.org:8080/a/b/c"), uri);
  }

  @Test
  public void replacesEncodedHost() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("te st.org").build();
    assertEquals(URI.create("http://te%20st.org:8080/a/b/c"), uri);
  }

  @Test
  public void removesHost() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host(null).build();
    assertEquals(URI.create("http://:8080/a/b/c"), uri);
  }

  @Test
  public void replacesPort() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(9000).build();
    assertEquals(URI.create("http://localhost:9000/a/b/c"), uri);
  }

  @Test
  public void removesPort() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(-1).build();
    assertEquals(URI.create("http://localhost/a/b/c"), uri);
  }

  @Test
  @UseDataProvider("forAddsPath")
  public void addsPath(String source, String path, String expected) {
    URI uri = UriBuilder.fromUri(source).path(path).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forAddsPath() {
    return new Object[][] {
      {"http://localhost:8080/x", "a/b/c", "http://localhost:8080/x/a/b/c"},
      {"http://localhost:8080/x/", "a/b/c", "http://localhost:8080/x/a/b/c"},
      {"http://localhost:8080/x", "/a/b/c", "http://localhost:8080/x/a/b/c"},
      {"http://localhost:8080/x/", "/a/b/c", "http://localhost:8080/x/a/b/c"},
      {"http://localhost:8080/a/b/c/", "/", "http://localhost:8080/a/b/c/"},
      {"http://localhost:8080/a/b/c", "/", "http://localhost:8080/a/b/c/"},
      {"http://localhost:8080/a/b/c%20", "/x/y /z", "http://localhost:8080/a/b/c%20/x/y%20/z"}
    };
  }

  @Test
  public void addsPathFromResource() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/base").path(R.class).build();
    assertEquals(URI.create("http://localhost:8080/base/resource"), uri);
  }

  @Test
  public void addsPathFromSubResourceMethod() throws Exception {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("get")).build();
    assertEquals(URI.create("http://localhost:8080/base/method1"), uri);
  }

  @Test
  public void addsPathFromResourceMethod() throws Exception {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/base").path(R.class.getMethod("post")).build();
    assertEquals(URI.create("http://localhost:8080/base"), uri);
  }

  @Test
  public void addsPathFromResourceClassAndSubResourceMethod() {
    URI u =
        UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "get").build();
    assertEquals(URI.create("http://localhost:8080/base/resource/method1"), u);
  }

  @Test
  public void addsPathFromResourceClassAndResourceMethod() {
    URI u =
        UriBuilder.fromUri("http://localhost:8080/base")
            .path(R.class)
            .path(R.class, "post")
            .build();
    assertEquals(URI.create("http://localhost:8080/base/resource"), u);
  }

  @Test
  public void replacesPath() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replacePath("/x/y/z").build();
    assertEquals(URI.create("http://localhost:8080/x/y/z"), uri);
  }

  @Test
  @UseDataProvider("forAddsSegment")
  public void addsSegment(String source, String[] segments, String expected) {
    URI uri = UriBuilder.fromUri(source).segment(segments).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forAddsSegment() {
    return new Object[][] {
      {"http://localhost:8080", new String[] {"a/b/c"}, "http://localhost:8080/a%2Fb%2Fc"},
      {
        "http://localhost:8080",
        new String[] {"a/b/c", "/x/y/z"},
        "http://localhost:8080/a%2Fb%2Fc/%2Fx%2Fy%2Fz"
      },
      {
        "http://localhost:8080",
        new String[] {"/a/b/c/", "/x/y/z"},
        "http://localhost:8080/%2Fa%2Fb%2Fc%2F/%2Fx%2Fy%2Fz"
      },
      {
        "http://localhost:8080",
        new String[] {"/a/b/c", "x/y/z"},
        "http://localhost:8080/%2Fa%2Fb%2Fc/x%2Fy%2Fz"
      }
    };
  }

  @Test
  @UseDataProvider("forAddsMatrixParam")
  public void addsMatrixParams(
      String source, String matrixParamName, String[] matrixParamValues, String expected) {
    URI uri = UriBuilder.fromUri(source).matrixParam(matrixParamName, matrixParamValues).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forAddsMatrixParam() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c;a=x;b=y",
        "c",
        new String[] {"z"},
        "http://localhost:8080/a/b/c;a=x;b=y;c=z"
      },
      {
        "http://localhost:8080/a/b/c;a=x;b=y",
        " c",
        new String[] {"z"},
        "http://localhost:8080/a/b/c;a=x;%20c=z;b=y"
      },
      {
        "http://localhost:8080/a/b/c;a=x;b=y",
        " c",
        new String[] {"%z"},
        "http://localhost:8080/a/b/c;a=x;%20c=%25z;b=y"
      },
      {
        "http://localhost:8080/a/b/c;a=x;b=y",
        "a",
        new String[] {"z"},
        "http://localhost:8080/a/b/c;a=x;a=z;b=y"
      }
    };
  }

  @Test
  public void addsPathAndMatrixParams() {
    URI u =
        UriBuilder.fromUri("http://localhost:8080/")
            .path("a")
            .matrixParam("x", " foo")
            .matrixParam("y", "%20bar")
            .path("b")
            .matrixParam("x", "f o%20o")
            .build();
    assertEquals(URI.create("http://localhost:8080/a;x=%20foo;y=%20bar/b;x=f%20o%20o"), u);
  }

  @Test
  @UseDataProvider("forReplacesMatrixParam")
  public void replacesMatrixParam(
      String source, String matrixParamName, String[] matrixParamValues, String expected) {
    URI uri =
        UriBuilder.fromUri(source).replaceMatrixParam(matrixParamName, matrixParamValues).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forReplacesMatrixParam() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c;a=x;b=y;a=z",
        "a",
        new String[] {"b", "c"},
        "http://localhost:8080/a/b/c;a=b;a=c;b=y"
      },
      {
        "http://localhost:8080/a/b/c;a=x;b=y;a=z",
        "a",
        new String[] {"b", "c"},
        "http://localhost:8080/a/b/c;a=b;a=c;b=y"
      },
      {
        "http://localhost:8080/a/b/c;a=x;b=y;a=z",
        "a",
        new String[] {"%b", " c"},
        "http://localhost:8080/a/b/c;a=%25b;a=%20c;b=y"
      },
      {
        "http://localhost:8080/a/b/c;%25a=x;b=y;%25a=z",
        "%a",
        new String[] {"%b", " c"},
        "http://localhost:8080/a/b/c;b=y;%25a=%25b;%25a=%20c"
      },
      {
        "http://localhost:8080/a;b=y;c=z",
        "a",
        new String[] {"b", "c"},
        "http://localhost:8080/a;a=b;a=c;b=y;c=z"
      },
      {
        "http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z",
        "b",
        null,
        "http://localhost:8080/a/b/c;a=x;a=z;y=b"
      },
      {
        "http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z",
        "b",
        new String[0],
        "http://localhost:8080/a/b/c;a=x;a=z;y=b"
      },
      {
        "http://localhost:8080/a/b;a=x;b=y;a=z/c;a=x;b=y;a=z",
        "a",
        new String[] {"b", "c"},
        "http://localhost:8080/a/b;a=x;b=y;a=z/c;a=b;a=c;b=y"
      }
    };
  }

  @Test
  @UseDataProvider("forReplacesMatrix")
  public void replacesMatrix(String source, String matrix, String expected) {
    URI uri = UriBuilder.fromUri(source).replaceMatrix(matrix).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forReplacesMatrix() {
    return new Object[][] {
      {"http://localhost:8080/a/b/c;a=x;b=y", "x=a;y=b", "http://localhost:8080/a/b/c;x=a;y=b"},
      {"http://localhost:8080/a/b/c", "x=a;y=b", "http://localhost:8080/a/b/c;x=a;y=b"},
      {"http://localhost:8080/a/b/c", null, "http://localhost:8080/a/b/c"}
    };
  }

  @Test
  @UseDataProvider("forAddsQueryParam")
  public void addsQueryParam(
      String source, String queryParamName, String[] queryParamValues, String expected) {
    URI u = UriBuilder.fromUri(source).queryParam(queryParamName, queryParamValues).build();
    assertEquals(URI.create(expected), u);
  }

  @DataProvider
  public static Object[][] forAddsQueryParam() {
    return new Object[][] {
      {"http://localhost:8080/a/b/c", "a", new String[] {"x"}, "http://localhost:8080/a/b/c?a=x"},
      {
        "http://localhost:8080/a/b/c?a=x&b=y",
        "c ",
        new String[] {"%25z"},
        "http://localhost:8080/a/b/c?a=x&b=y&c%20=%25z"
      },
      {
        "http://localhost:8080/a/b/c?a=x&b=y",
        "a",
        new String[] {"z"},
        "http://localhost:8080/a/b/c?a=x&b=y&a=z"
      }
    };
  }

  @Test
  @UseDataProvider("forReplacesQueryParam")
  public void replacesQueryParam(
      String source, String queryParamName, String[] queryParamValues, String expected) {
    URI uri =
        UriBuilder.fromUri(source).replaceQueryParam(queryParamName, queryParamValues).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forReplacesQueryParam() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c?a=x&b=y&a=z",
        "a",
        new String[] {"b", "c"},
        "http://localhost:8080/a/b/c?b=y&a=b&a=c"
      },
      {
        "http://localhost:8080/a/b/c?a=x&a=z&b=y",
        "a",
        new String[] {"b", "c"},
        "http://localhost:8080/a/b/c?b=y&a=b&a=c"
      },
      {
        "http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z",
        "a",
        new String[] {"b%20", "c%"},
        "http://localhost:8080/a/b/c?b=y&y=b&a=b%20&a=c%25"
      },
      {
        "http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z",
        "x",
        new String[] {"b"},
        "http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z&x=b"
      },
      {
        "http://localhost:8080/a/b/c?b=y&a=x&b=&y=b&a=z",
        "b",
        null,
        "http://localhost:8080/a/b/c?a=x&y=b&a=z"
      },
      {
        "http://localhost:8080/a/b/c?b=y&a=x&b=&y=b&a=z",
        "b",
        new String[0],
        "http://localhost:8080/a/b/c?a=x&y=b&a=z"
      }
    };
  }

  @Test
  @UseDataProvider("forReplacesQueryParamsWithQueryString")
  public void replacesQueryParamsWithQueryString(
      String source, String queryString, String expected) {
    URI uri = UriBuilder.fromUri(source).replaceQuery(queryString).build();
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forReplacesQueryParamsWithQueryString() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c?a=x&b=y",
        "x=a&y=b&zzz=",
        "http://localhost:8080/a/b/c?x=a&y=b&zzz="
      },
      {"http://localhost:8080/a/b/c?a=x&b=y", "", "http://localhost:8080/a/b/c"},
      {"http://localhost:8080/a/b/c?a=x&b=y", null, "http://localhost:8080/a/b/c"}
    };
  }

  @Test
  public void replacesFragment() {
    URI u = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y#hi").fragment("hel lo").build();
    assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y#hel%20lo"), u);
  }

  @DataProvider
  public static Object[][] forCopiesComponentFromOtherUri() {
    return new Object[][] {
      {
        "http://exo@localhost:8080/a/b/c?a=x&b=y#fragment",
        "https://exo@localhost:8080",
        "https://exo@localhost:8080/a/b/c?a=x&b=y#fragment"
      },
      {
        "http://exo@localhost:8080/a/b/c?a=x&b=y#fragment",
        "http://andrew@localhost:8080",
        "http://andrew@localhost:8080/a/b/c?a=x&b=y#fragment"
      },
      {
        "http://exo@localhost:8080/a/b/c?a=x&b=y#fragment",
        "/x/y/z",
        "http://exo@localhost:8080/x/y/z?a=x&b=y#fragment"
      },
      {
        "http://exo@localhost:8080/a/b/c?a=x&b=y#fragment",
        "?x=a&b=y",
        "http://exo@localhost:8080/a/b/c?x=a&b=y#fragment"
      },
      {
        "http://exo@localhost:8080/a/b/c?a=x&b=y#fragment",
        "#new_fragment",
        "http://exo@localhost:8080/a/b/c?a=x&b=y#new_fragment"
      }
    };
  }

  @Test
  @UseDataProvider("forCopiesComponentFromOtherUri")
  public void copiesComponentFromOtherUri(String source, String otherUri, String expected) {
    URI uri = UriBuilder.fromUri(source).uri(otherUri).build();
    assertEquals(URI.create(expected), uri);
  }

  @Test
  public void replacesSchemeSpecificPart() {
    URI uri1 = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
    URI uri2 =
        UriBuilder.fromUri(uri1)
            .schemeSpecificPart("//andrew@exoplatform.org:9000/x /y/z?x= a&y=b")
            .build();
    assertEquals(
        URI.create("http://andrew@exoplatform.org:9000/x%20/y/z?x=%20a&y=b#fragment"), uri2);
  }

  @Test
  public void failsSetInvalidScheme() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("htt\tp").build();
  }

  @Test
  public void failsSetInvalidPort() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c").port(-10).build();
  }

  @Test
  public void failsSetNullUri() {
    URI origin = URI.create("http://exo@localhost:8080/a/b/c?a=x&b=y#fragment");
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri(origin).uri((String) null).build();
  }

  @Test
  public void failsSetNullSchemeSpecificPart() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c").schemeSpecificPart(null).build();
  }

  @Test
  public void failsAddNullPath() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/").path((String) null).build();
  }

  @Test
  public void failsAddPathFromNotResourceClass() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/base").path(Object.class).build();
  }

  @Test
  public void failsAddPathFromNotExistedMethod() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R.class, "wrong").build();
  }

  @Test
  public void failsAddPathWhenClassContainsMoreThenOneMethodWithTheSameName() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/base").path(R.class).path(R2.class, "get").build();
  }

  @Test
  public void failsAddNullSegment() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/").segment((String[]) null).build();
  }

  @Test
  public void failsAddSegmentsWhenOneOfThemIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/").segment("a", null, "b").build();
  }

  @Test
  public void failsAddMatrixParamWhenNameIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam(null, "x").build();
  }

  @Test
  public void failsReplaceMatrixParamWhenNameIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c;y=b;a=x;b=y;a=z")
        .replaceMatrixParam(null, "a")
        .build();
  }

  @Test
  public void failsAddMatrixParamWhenValueIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c").matrixParam("a", (String[]) null).build();
  }

  @Test
  public void failsAddMatrixParamWhenOneValueIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c")
        .matrixParam("a", new String[] {"x", null})
        .build();
  }

  @Test
  public void failsAddQueryParamWhenNameIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam(null, "z").build();
  }

  @Test
  public void failsReplaceQueryParamWhenNameIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c?b=y&a=x&y=b&a=z")
        .replaceQueryParam(null, "b", "c")
        .build();
  }

  @Test
  public void failsAddQueryParamWhenValuesIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y")
        .queryParam("c", (String[]) null)
        .build();
  }

  @Test
  public void failsAddQueryParamWhenOneValueIsNull() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y")
        .queryParam("c", new String[] {"z", null})
        .build();
  }

  @Test
  public void failsSetInvalidQueryString() {
    thrown.expect(IllegalArgumentException.class);
    UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&=zzz&y=b").build();
  }

  @UseDataProvider("forBuildsUriFromMap")
  @Test
  public void buildsUriFromMap(String source, Map<String, Object> map, String expected) {
    URI uri = UriBuilder.fromUri(source).buildFromMap(map);
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forBuildsUriFromMap() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{baz}/{foo}",
        ImmutableMap.of("foo", "%25x", "bar", "%y", "baz", "z"),
        "http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/baz/foo",
        ImmutableMap.of("foo", "%25x", "bar", "%y"),
        "http://localhost:8080/a/b/c/%2525x/%25y/baz/foo"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{foo}/baz",
        ImmutableMap.of("foo", "%25x", "bar", "%y"),
        "http://localhost:8080/a/b/c/%2525x/%25y/%2525x/baz"
      }
    };
  }

  @UseDataProvider("forBuildsUriFromEncodedMap")
  @Test
  public void buildsUriFromEncodedMap(String source, Map<String, Object> map, String expected) {
    URI uri = UriBuilder.fromUri(source).buildFromEncodedMap(map);
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forBuildsUriFromEncodedMap() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{baz}/{foo}",
        ImmutableMap.of("foo", "%25x", "bar", "%y", "baz", "z"),
        "http://localhost:8080/a/b/c/%25x/%25y/z/%25x"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/baz/foo",
        ImmutableMap.of("foo", "%25x", "bar", "%y"),
        "http://localhost:8080/a/b/c/%25x/%25y/baz/foo"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{foo}/baz",
        ImmutableMap.of("foo", "%25x", "bar", "%y"),
        "http://localhost:8080/a/b/c/%25x/%25y/%25x/baz"
      }
    };
  }

  @UseDataProvider("forBuildsUriFromArray")
  @Test
  public void buildsUriFromArray(String source, Object[] array, String expected) {
    URI uri = UriBuilder.fromUri(source).build(array);
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forBuildsUriFromArray() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{baz}/{foo}",
        new Object[] {"%25x", "%y", "z", "wrong"},
        "http://localhost:8080/a/b/c/%2525x/%25y/z/%2525x"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/baz/foo",
        new Object[] {"z", "y"},
        "http://localhost:8080/a/b/c/z/y/baz/foo"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{foo}/baz",
        new Object[] {"z", "y"},
        "http://localhost:8080/a/b/c/z/y/z/baz"
      }
    };
  }

  @UseDataProvider("forBuildsUriFromArrayEncodeSlashInPath")
  @Test
  public void buildsUriFromArrayEncodeSlashInPath(
      String source, boolean encodeSlashInPath, Object[] array, String expected) {
    URI uri = UriBuilder.fromUri(source).build(array, encodeSlashInPath);
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forBuildsUriFromArrayEncodeSlashInPath() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/baz/foo",
        false,
        new Object[] {"z", "y"},
        "http://localhost:8080/a/b/c/z/y/baz/foo"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/baz/foo",
        true,
        new Object[] {"z", "y"},
        "http://localhost:8080/a/b/c/z/y/baz/foo"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{foo}/baz",
        false,
        new Object[] {"z", "y"},
        "http://localhost:8080/a/b/c/z/y/z/baz"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{foo}/baz",
        true,
        new Object[] {"z", "y"},
        "http://localhost:8080/a/b/c/z/y/z/baz"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}",
        false,
        new Object[] {"x/z", "y/x"},
        "http://localhost:8080/a/b/c/x/z/y/x"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}",
        false,
        new Object[] {"x%2Fz", "y%2Fx"},
        "http://localhost:8080/a/b/c/x%252Fz/y%252Fx"
      }
    };
  }

  @UseDataProvider("forBuildsUriFromEncodedArray")
  @Test
  public void buildsUriFromEncodedArray(String source, Object[] array, String expected) {
    URI uri = UriBuilder.fromUri(source).buildFromEncoded(array);
    assertEquals(URI.create(expected), uri);
  }

  @DataProvider
  public static Object[][] forBuildsUriFromEncodedArray() {
    return new Object[][] {
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{baz}/{foo}",
        new Object[] {"%25x", "%y", "z", "wrong"},
        "http://localhost:8080/a/b/c/%25x/%25y/z/%25x"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/baz/foo",
        new Object[] {"%25x", "%y", "wrong"},
        "http://localhost:8080/a/b/c/%25x/%25y/baz/foo"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}/{foo}/baz",
        new Object[] {"%25x", "%y", "wrong"},
        "http://localhost:8080/a/b/c/%25x/%25y/%25x/baz"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}",
        new Object[] {"x/z", "y/x"},
        "http://localhost:8080/a/b/c/x/z/y/x"
      },
      {
        "http://localhost:8080/a/b/c/{foo}/{bar}",
        new Object[] {"x%2Fz", "y%2Fx"},
        "http://localhost:8080/a/b/c/x%2Fz/y%2Fx"
      }
    };
  }

  @UseDataProvider("forResolvesTemplateWithOneParameter")
  @Test
  public void resolvesTemplateWithOneParameter(
      String source, String paramName, Object paramValue, String expected) {
    String template =
        UriBuilder.fromUri(source).resolveTemplate(paramName, paramValue).toTemplate();
    assertEquals(expected, template);
  }

  @DataProvider
  public static Object[][] forResolvesTemplateWithOneParameter() {
    return new Object[][] {
      {
        "{scheme}://{host}:{port}/a/{path}?{query}#{fragment}",
        "scheme",
        "https",
        "https://{host}:{port}/a/{path}?{query}#{fragment}"
      },
      {
        "{scheme}://{host}:{port}/a/{path}?{query}#{fragment}",
        "host",
        "localhost",
        "{scheme}://localhost:{port}/a/{path}?{query}#{fragment}"
      },
      {
        "{scheme}://{host}:{port}/a/{path}?{query}#{fragment}",
        "port",
        "8080",
        "{scheme}://{host}:8080/a/{path}?{query}#{fragment}"
      },
      {
        "{scheme}://{host}:{port}/a/{path}?{query}#{fragment}",
        "path",
        "b/c",
        "{scheme}://{host}:{port}/a/b/c?{query}#{fragment}"
      },
      {
        "{scheme}://{host}:{port}/a/{path}?{query}#{fragment}",
        "query",
        "qqq",
        "{scheme}://{host}:{port}/a/{path}?qqq#{fragment}"
      },
      {
        "{scheme}://{host}:{port}/a/{path}?{query}#{fragment}",
        "fragment",
        "fff",
        "{scheme}://{host}:{port}/a/{path}?{query}#fff"
      }
    };
  }

  @Test
  public void resolvesTemplateWithOneParameterEncodeSlashInPath() {
    String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
    UriBuilder builder = UriBuilder.fromUri(template);
    builder.resolveTemplate("path", "b/c", true);
    assertEquals("{scheme}://{host}/a/b%2Fc?{q}={v}#{fragment}", builder.toTemplate());
  }

  @Test
  public void resolvesTemplateWithOneParameterFromEncoded() {
    String template = "http://localhost/a/{path}";
    UriBuilder builder = UriBuilder.fromUri(template);
    builder.resolveTemplateFromEncoded("path", "b%2fc");
    assertEquals("http://localhost/a/b%2fc", builder.toTemplate());
  }

  @Test
  public void resolvesTemplateWithMap() {
    String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
    UriBuilder builder = UriBuilder.fromUri(template);
    Map<String, Object> templateValues = new HashMap<>(8);
    templateValues.put("scheme", "https");
    templateValues.put("host", "localhost");
    templateValues.put("path", "b/c");
    templateValues.put("q", "test");
    templateValues.put("v", "hello");
    templateValues.put("fragment", "hello_fragment");
    builder.resolveTemplates(templateValues);
    assertEquals("https://localhost/a/b/c?test=hello#hello_fragment", builder.toTemplate());
  }

  @Test
  public void resolvesTemplateWithMapEncodeSlashInPath() {
    String template = "{scheme}://{host}/a/{path}?{q}={v}#{fragment}";
    UriBuilder builder = UriBuilder.fromUri(template);
    builder.resolveTemplate("path", "b/c", true);
    assertEquals("{scheme}://{host}/a/b%2Fc?{q}={v}#{fragment}", builder.toTemplate());
  }

  @Test
  public void resolvesTemplateWithMapFromEncoded() {
    String template = "http://localhost/a/{path}";
    UriBuilder builder = UriBuilder.fromUri(template);
    builder.resolveTemplateFromEncoded("path", "b%2fc");
    assertEquals("http://localhost/a/b%2fc", builder.toTemplate());
  }

  @Test
  public void clones() {
    UriBuilder builder = UriBuilder.fromUri("http://user@localhost:8080/a?query#fragment");

    assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), builder.build());
    assertEquals(
        URI.create("http://user@localhost:8080/a/b?query#fragment"),
        builder.clone().path("b").build());
  }

  @Path("resource")
  class R {
    @GET
    @Path("method1")
    public void get() {}

    @POST
    public void post() {}
  }

  @Path("resource2")
  class R2 {
    @GET
    @Path("method1")
    public void get() {}

    @GET
    @Path("method2")
    public void get(String s) {}
  }
}
