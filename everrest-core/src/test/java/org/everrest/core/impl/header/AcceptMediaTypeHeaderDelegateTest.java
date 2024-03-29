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
package org.everrest.core.impl.header;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class AcceptMediaTypeHeaderDelegateTest {
  @DataProvider
  public static Object[][] forTestParsingAcceptMediaTypeHeader() {
    return new Object[][] {
      {"text/xml;charset=utf8", "text", "xml", ImmutableMap.of("charset", "utf8"), 1.0f},
      {
        "text/xml;charset=utf8;q=.825",
        "text",
        "xml",
        ImmutableMap.of("charset", "utf8", "q", ".825"),
        0.825f
      },
      {"text/*;charset=utf8", "text", "*", ImmutableMap.of("charset", "utf8"), 1.0f},
      {
        "text/*;charset=utf8;q=0.777",
        "text",
        "*",
        ImmutableMap.of("charset", "utf8", "q", "0.777"),
        0.777f
      },
      {"text;charset=utf8", "text", "*", ImmutableMap.of("charset", "utf8"), 1.0f}
    };
  }

  @UseDataProvider("forTestParsingAcceptMediaTypeHeader")
  @Test
  public void testParsingAcceptMediaTypeHeader(
      String acceptMediaTypeHeader,
      String type,
      String subType,
      Map<String, String> parameters,
      float qValue) {
    AcceptMediaTypeHeaderDelegate headerDelegate = new AcceptMediaTypeHeaderDelegate();
    AcceptMediaType acceptMediaType = headerDelegate.fromString(acceptMediaTypeHeader);

    assertEquals(type, acceptMediaType.getType());
    assertEquals(subType, acceptMediaType.getSubtype());
    assertEquals(parameters, acceptMediaType.getParameters());
    assertEquals(qValue, acceptMediaType.getQvalue(), 0.0F);
  }

  @DataProvider
  public static Object[][] forTestAcceptMediaTypeToString() {
    return new Object[][] {
      {new AcceptMediaType(), "*/*"},
      {new AcceptMediaType("text", "xml"), "text/xml"},
      {
        new AcceptMediaType("text", "xml", ImmutableMap.of("charset", "utf8")),
        "text/xml;charset=utf8"
      },
      {
        new AcceptMediaType("text", "xml", ImmutableMap.of("charset", "utf8", "q", ".8")),
        "text/xml;charset=utf8"
      }
    };
  }

  @UseDataProvider("forTestAcceptMediaTypeToString")
  @Test
  public void testAcceptMediaTypeToString(AcceptMediaType acceptMediaType, String expectedResult) {
    AcceptMediaTypeHeaderDelegate headerDelegate = new AcceptMediaTypeHeaderDelegate();
    assertEquals(expectedResult, headerDelegate.toString(acceptMediaType));
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwExceptionWhenAcceptMediaTypeHeaderIsNull() {
    new AcceptMediaTypeHeaderDelegate().fromString(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwExceptionWhenAcceptMediaTypeIsNull() {
    new AcceptMediaTypeHeaderDelegate().toString(null);
  }
}
