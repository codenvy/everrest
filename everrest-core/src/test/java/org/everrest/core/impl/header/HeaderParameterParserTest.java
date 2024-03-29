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
import java.text.ParseException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class HeaderParameterParserTest {

  private HeaderParameterParser headerParameterParser;

  @Before
  public void setUp() throws Exception {
    headerParameterParser = new HeaderParameterParser();
  }

  @DataProvider
  public static Object[][] forParses() {
    return new Object[][] {
      {"text/plain", ImmutableMap.of()},
      {"text/plain ; ", ImmutableMap.of()},
      {"text/plain;foo=bar", ImmutableMap.of("foo", "bar")},
      {"text/plain;foo=\"bar\"", ImmutableMap.of("foo", "bar")},
      {
        "text/plain;foo=\"\\\"he\\\";llo\\\"\"   ;  ba r  =  f o o       ; foo2=x",
        ImmutableMap.of("foo", "\"he\";llo\"", "ba r", "f o o", "foo2", "x")
      },
      {
        "text/plain;bar=\"foo\" \t ; test=\"\\a\\b\\c\\\"\"   ;  foo=bar",
        ImmutableMap.of("foo", "bar", "test", "\\a\\b\\c\"", "bar", "foo")
      }
    };
  }

  @UseDataProvider("forParses")
  @Test
  public void parses(String header, Map<String, String> expectedParsedParameters) throws Exception {
    assertEquals(expectedParsedParameters, headerParameterParser.parse(header));
  }

  @Test(expected = ParseException.class)
  public void throwsExceptionWhenStringContainsUnclosedQuote() throws Exception {
    headerParameterParser.parse("text/plain;foo=\"bar");
  }

  @Test(expected = ParseException.class)
  public void throwsExceptionWhenStringContainsIllegalCharacter() throws Exception {
    headerParameterParser.parse("text/plain;fo[o=\"bar\"");
  }
}
