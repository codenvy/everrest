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
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Locale;
import javax.ws.rs.ext.RuntimeDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class AcceptLanguageTest {
  private RuntimeDelegate.HeaderDelegate<AcceptLanguage> headerDelegate;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    headerDelegate = mock(RuntimeDelegate.HeaderDelegate.class);

    RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
    when(runtimeDelegate.createHeaderDelegate(AcceptLanguage.class)).thenReturn(headerDelegate);

    RuntimeDelegate.setInstance(runtimeDelegate);
  }

  @After
  public void tearDown() throws Exception {
    RuntimeDelegate.setInstance(null);
  }

  @Test
  public void testValueOf() {
    AcceptLanguage acceptLanguage = acceptLanguage("en", "us");
    when(headerDelegate.fromString("en-us")).thenReturn(acceptLanguage);

    assertSame(acceptLanguage, AcceptLanguage.valueOf("en-us"));
  }

  @Test
  public void testToString() {
    AcceptLanguage acceptLanguage = acceptLanguage("en", "us");
    when(headerDelegate.toString(acceptLanguage)).thenReturn("en-us");

    assertEquals("en-us", acceptLanguage.toString());
    verify(headerDelegate).toString(acceptLanguage);
  }

  @DataProvider
  public static Object[][] forTestIsCompatible() {
    return new Object[][] {
      {acceptLanguage("en", ""), null, false},
      {acceptLanguage("*", ""), acceptLanguage("en", ""), true},
      {acceptLanguage("en", ""), acceptLanguage("en", ""), true},
      {acceptLanguage("en", "gb"), acceptLanguage("en", ""), false},
      {acceptLanguage("en", ""), acceptLanguage("en", "gb"), true},
      {acceptLanguage("en", "us"), acceptLanguage("en", "gb"), false},
      {acceptLanguage("en", ""), acceptLanguage("fr", ""), false},
    };
  }

  @UseDataProvider("forTestIsCompatible")
  @Test
  public void testIsCompatible(
      AcceptLanguage acceptLanguage, AcceptLanguage checkAcceptLanguage, boolean expectedResult) {
    assertEquals(expectedResult, acceptLanguage.isCompatible(checkAcceptLanguage));
  }

  @Test
  public void retrievesLocaleFromLanguage() {
    Locale locale = new Locale("en", "us");
    AcceptLanguage acceptLanguage = new AcceptLanguage(new Language(locale));
    assertEquals(locale, acceptLanguage.getLocale());
  }

  private static AcceptLanguage acceptLanguage(String primaryTag, String subTag) {
    return new AcceptLanguage(new Locale(primaryTag, subTag));
  }
}
