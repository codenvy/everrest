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

import static com.google.common.collect.Lists.newArrayList;
import static org.everrest.core.impl.uri.UriComponent.HOST;
import static org.everrest.core.impl.uri.UriComponent.PATH_SEGMENT;
import static org.everrest.core.impl.uri.UriComponent.checkHexCharacters;
import static org.everrest.core.impl.uri.UriComponent.decode;
import static org.everrest.core.impl.uri.UriComponent.encode;
import static org.everrest.core.impl.uri.UriComponent.parsePathSegments;
import static org.everrest.core.impl.uri.UriComponent.parseQueryString;
import static org.everrest.core.impl.uri.UriComponent.recognizeEncode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class UriComponentTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void checksHexCharacters() {
    String str = "%20%23%a0%ag";
    assertTrue(checkHexCharacters(str, 0));
    assertFalse(checkHexCharacters(str, 1));
    assertTrue(checkHexCharacters(str, 3));
    assertTrue(checkHexCharacters(str, 6));
    assertFalse(checkHexCharacters(str, 9));
    assertFalse(checkHexCharacters(str, 11));
  }

  @Test
  public void encodesString() {
    String str = "\u041f?\u0440#\u0438 \u0432\u0456\u0442";
    String encoded = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
    assertEquals(encoded, encode(str, HOST, false));
  }

  @Test
  public void encodesStringDoesNotRespectEncoded() {
    String str = "to be%23or not to%20be";
    assertEquals("to%20be%2523or%20not%20to%2520be", encode(str, PATH_SEGMENT, false));
  }

  @Test
  public void encodesStringRespectEncoded() {
    String str = "to be%23or not to%20be";
    assertEquals("to%20be%23or%20not%20to%20be", recognizeEncode(str, PATH_SEGMENT, false));
  }

  @Test
  public void decodesString() {
    String str = "\u041f?\u0440#\u0438 \u0432\u0456\u0442";
    String encoded = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
    assertEquals(str, decode(encoded, HOST));
  }

  @Test
  public void parsesQueryString() {
    String queryString =
        "q1=to%20be%20or%20not%20to%20be&q2=foo&q2=%D0%9F%D1%80%D0%B8%D0%B2%D1%96%D1%82";
    MultivaluedMapImpl expected = new MultivaluedMapImpl();
    expected.add("q1", "to%20be%20or%20not%20to%20be");
    expected.add("q2", "foo");
    expected.add("q2", "%D0%9F%D1%80%D0%B8%D0%B2%D1%96%D1%82");

    MultivaluedMap<String, String> result = parseQueryString(queryString, false);
    assertEquals(expected, result);
  }

  @Test
  public void parsesAndDecodeQueryString() {
    String queryString =
        "q1=to%20be%20or%20not%20to%20be&q2=foo&q2=%D0%9F%D1%80%D0%B8%D0%B2%D1%96%D1%82";
    MultivaluedMapImpl expected = new MultivaluedMapImpl();
    expected.add("q1", "to be or not to be");
    expected.addAll("q2", "foo", "\u041f\u0440\u0438\u0432\u0456\u0442");

    MultivaluedMap<String, String> result = parseQueryString(queryString, true);
    assertEquals(expected, result);
  }

  @Test
  public void parsesPathSegments() {
    String path = "/to/be/or%20not/to/be;a=foo;b=b%20a%23r";
    List<PathSegment> expected =
        newArrayList(
            PathSegmentImpl.fromString("to", false),
            PathSegmentImpl.fromString("be", false),
            PathSegmentImpl.fromString("or not", false),
            PathSegmentImpl.fromString("to", false),
            PathSegmentImpl.fromString("be;a=foo;b=b a#r", false));
    List<PathSegment> result = parsePathSegments(path, true);
    assertEquals(expected, result);
  }

  @Test
  public void failsDecodeMalformedString() {
    String wrongEncoded = "%D0%9g%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%82";
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.format("Malformed string '%s' at index %d", wrongEncoded, 5));
    decode(wrongEncoded, HOST);
  }

  @Test
  public void failsDecodeUncompletedString() {
    String wrongEncoded = "%D0%9F%3F%D1%80%23%D0%B8%20%D0%B2%D1%96%D1%8";
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.format("Malformed string '%s' at index %d", wrongEncoded, 42));
    decode(wrongEncoded, HOST);
  }

  @UseDataProvider("forNormalizesUri")
  @Test
  public void normalizesUri(String originUri, String normalizedUri) throws Exception {
    assertEquals(normalizedUri, UriComponent.normalize(URI.create(originUri)).toString());
  }

  @DataProvider
  public static Object[][] forNormalizesUri() {
    return new Object[][] {
      {"http://localhost:8080/servlet/../1//2/3/./../../4", "http://localhost:8080/1/4"},
      {"http://localhost:8080/servlet/./1//2/3/./../../4", "http://localhost:8080/servlet/1/4"},
      {"http://localhost:8080/servlet/1//2/3/./../../4", "http://localhost:8080/servlet/1/4"},
      {"http://localhost:8080/servlet/1//2./3/./../4", "http://localhost:8080/servlet/1/2./4"},
      {"http://localhost:8080/servlet/1//.2/3/./../4", "http://localhost:8080/servlet/1/.2/4"},
      {"http://localhost:8080/servlet/1..//.2/3/./../4", "http://localhost:8080/servlet/1../.2/4"},
      {"http://localhost:8080/servlet/./1//2/3/./../../4", "http://localhost:8080/servlet/1/4"},
      {"http://localhost:8080/servlet/.", "http://localhost:8080/servlet/"},
      {"http://localhost:8080/servlet/..", "http://localhost:8080/"},
      {"http://localhost:8080/servlet/1", "http://localhost:8080/servlet/1"}
    };
  }

  @Test
  public void parsesQueryString_EVERREST_58() {
    String str = "q1=bar&&q2=foo&q2=test";
    MultivaluedMapImpl expected = new MultivaluedMapImpl();
    expected.add("q1", "bar");
    expected.addAll("q2", "foo", "test");

    MultivaluedMap<String, String> result = parseQueryString(str, false);
    assertEquals(expected, result);

    result = parseQueryString(str, true);
    assertEquals(expected, result);
  }

  @DataProvider
  public static Object[][] forResolvesUri() {
    return new Object[][] {
      {
        URI.create("http://localhost:8080/1/2/3?4=5"),
        URI.create("?7=8"),
        URI.create("http://localhost:8080/1/2/3?7=8")
      },
      {
        URI.create("http://localhost:8080/1/2/3?4=5"),
        URI.create(""),
        URI.create("http://localhost:8080/1/2/3?4=5")
      },
      {
        URI.create("http://localhost:8080/1/2/3?4=5"),
        URI.create("x/y/z"),
        URI.create("http://localhost:8080/1/2/x/y/z")
      },
      {
        URI.create("http://localhost:8080/1/2/3"),
        URI.create("?7=8"),
        URI.create("http://localhost:8080/1/2/3?7=8")
      },
      {
        URI.create("http://localhost:8080/1/2/3"),
        URI.create("x/y/z"),
        URI.create("http://localhost:8080/1/2/x/y/z")
      }
    };
  }

  @UseDataProvider("forResolvesUri")
  @Test
  public void resolvesUri(URI baseUri, URI resolvingUri, URI expectedResult) {
    assertEquals(expectedResult, UriComponent.resolve(baseUri, resolvingUri));
  }
}
