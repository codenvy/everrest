/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.header;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.CacheControl;

import static com.google.common.collect.Lists.newArrayList;
import static org.everrest.core.impl.header.CacheControlBuilder.aCacheControl;
import static org.junit.Assert.assertEquals;

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
        CacheControl cacheControl = aCacheControl()
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

        assertEquals("private=\"aaa\", no-cache=\"bbb\", no-store, no-transform, must-revalidate, proxy-revalidate, 60, 60, foo=bar",
                     cacheControlHeaderDelegate.toString(cacheControl));
    }

}