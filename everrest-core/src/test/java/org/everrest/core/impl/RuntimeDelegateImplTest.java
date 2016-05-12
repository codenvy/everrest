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
package org.everrest.core.impl;

import org.everrest.core.impl.header.AcceptLanguage;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.Ranges;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.net.URI;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;

public class RuntimeDelegateImplTest {
    private RuntimeDelegateImpl runtimeDelegate;

    @Before
    public void setUp() throws Exception {
        runtimeDelegate = new RuntimeDelegateImpl();
    }

    @Test
    public void createsHeaderDelegateForAcceptLanguage() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(AcceptLanguage.class));
    }

    @Test
    public void createsHeaderDelegateForAcceptMediaType() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(AcceptMediaType.class));
    }

    @Test
    public void createsHeaderDelegateForCacheControl() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(CacheControl.class));
    }

    @Test
    public void createsHeaderDelegateForCookie() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(Cookie.class));
    }

    @Test
    public void createsHeaderDelegateForDate() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(Date.class));
    }

    @Test
    public void createsHeaderDelegateForEntityTag() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(EntityTag.class));
    }

    @Test
    public void createsHeaderDelegateForLocale() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(Locale.class));
    }

    @Test
    public void createsHeaderDelegateForMediaType() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(MediaType.class));
    }

    @Test
    public void createsHeaderDelegateForNewCookie() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(NewCookie.class));
    }

    @Test
    public void createsHeaderDelegateForRanges() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(Ranges.class));
    }

    @Test
    public void createsHeaderDelegateForString() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(String.class));
    }

    @Test
    public void createsHeaderDelegateForURI() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(URI.class));
    }

    @Test
    public void createsHeaderDelegateForLink() throws Exception {
        assertNotNull(runtimeDelegate.createHeaderDelegate(Link.class));
    }
}