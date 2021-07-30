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

import static com.google.common.collect.Lists.newArrayList;
import static org.everrest.core.impl.header.CacheControlBuilder.aCacheControl;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import javax.ws.rs.core.CacheControl;
import org.junit.Before;
import org.junit.Test;

public class CacheControlHeaderDelegateTest {

  private CacheControlHeaderDelegate cacheControlHeaderDelegate;

  @Before
  public void setUp() throws Exception {
    cacheControlHeaderDelegate = new CacheControlHeaderDelegate();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void fromStringIsNotSupported() throws Exception {
    cacheControlHeaderDelegate.fromString("public");
  }

  @Test
  public void testToStringCacheControlWithAllArguments() throws Exception {
    CacheControl cacheControl =
        aCacheControl()
            .withMaxAge(60)
            .withMustRevalidate(true)
            .withNoCache(true)
            .withNoStore(true)
            .withNoTransform(true)
            .withPrivate(true)
            .withProxyRevalidate(true)
            .withSMaxAge(60)
            .withPrivateFields(newArrayList("aaa"))
            .withNoCacheFields(newArrayList("bbb"))
            .withCacheExtension(ImmutableMap.of("foo", "bar"))
            .build();

    assertEquals(
        "private=\"aaa\", no-cache=\"bbb\", no-store, no-transform, must-revalidate, proxy-revalidate, 60, 60, foo=bar",
        cacheControlHeaderDelegate.toString(cacheControl));
  }
}
