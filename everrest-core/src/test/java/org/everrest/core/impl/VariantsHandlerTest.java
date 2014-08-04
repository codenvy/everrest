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

import org.everrest.core.tools.SimpleSecurityContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Variant;
import java.util.List;
import java.util.Locale;

/**
 * @author andrew00x
 */
public class VariantsHandlerTest {
    List<Variant> variantList;

    @Before
    public void setUp() {
        variantList = Variant.VariantListBuilder.newInstance().mediaTypes(MediaType.valueOf("image/jpeg")).add()
                                                .mediaTypes(MediaType.valueOf("application/xml")).languages(new Locale("en", "us")).add()
                                                .mediaTypes(MediaType.valueOf("text/xml")).languages(new Locale("en")).add()
                                                .mediaTypes(MediaType.valueOf("text/xml")).languages(new Locale("en", "us")).add().build();
    }

    @Test
    public void testVariantHandler1() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Accept", glue("text/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8", "application/xml", "*/*;q=0.5"));
        h.putSingle("Accept-Language", "en-us,en;q=0.5");
        ContainerRequest r = new ContainerRequest("GET", null, null, null, h, null);
        Variant v = VariantsHandler.handleVariants(r, variantList);
        Assert.assertEquals(new MediaType("text", "xml"), v.getMediaType());
        Assert.assertEquals(new Locale("en", "us"), v.getLanguage());
    }

    @Test
    public void testVariantHandler2() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Accept", glue("text/xml;q=0.95", "text/html;q=0.9", "application/xml", "image/png", "text/plain;q=0.8", "*/*;q=0.5"));
        h.putSingle("Accept-Language", "en-us;q=0.5,en;q=0.7");
        ContainerRequest r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
        Variant v = VariantsHandler.handleVariants(r, variantList);
        // 'application/xml' has higher 'q' value then 'text/xml'
        Assert.assertEquals(new MediaType("application", "xml"), v.getMediaType());
        Assert.assertEquals(new Locale("en", "us"), v.getLanguage());

    }

    @Test
    public void testVariantHandler3() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Accept", glue("text/xml", "application/xml", "text/plain;q=0.8", "image/png", "text/html;q=0.9", "*/*;q=0.5"));
        h.putSingle("Accept-Language", "en,en-us");
        ContainerRequest r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
        Variant v = VariantsHandler.handleVariants(r, variantList);
        Assert.assertEquals(new MediaType("text", "xml"), v.getMediaType());
        // then 'en' goes first in 'accept' list
        Assert.assertEquals(new Locale("en"), v.getLanguage());

    }

    @Test
    public void testVariantHandler4() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Accept", glue("text/xml", "application/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8", "*/*;q=0.5"));
        h.putSingle("Accept-Language", "uk");
        ContainerRequest r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
        Variant v = VariantsHandler.handleVariants(r, variantList);
        // no language 'uk' in variants then '*/*;q=0.5' will work
        Assert.assertEquals(new MediaType("image", "jpeg"), v.getMediaType());
    }

    @Test
    public void testVariantHandler5() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Accept", glue("text/xml", "application/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8"));
        h.putSingle("Accept-Language", "uk");
        ContainerRequest r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
        Variant v = VariantsHandler.handleVariants(r, variantList);
        // no language 'uk' in variants and '*/*;q=0.5' removed
        Assert.assertNull(v); // 'Not Acceptable' (406) will be generated here

    }

    @Test
    public void testVariantHandler6() throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("Accept", glue("text/xml", "application/xml", "image/*", "text/html;q=0.9", "text/plain;q=0.8"));
        h.putSingle("Accept-Language", "uk");
        ContainerRequest r = new ContainerRequest("GET", null, null, null, h, new SimpleSecurityContext(false));
        Variant v = VariantsHandler.handleVariants(r, variantList);
        // no language 'uk' in variants then 'image/*' will work
        Assert.assertEquals(new MediaType("image", "jpeg"), v.getMediaType());
    }

    private static String glue(String... s) {
        StringBuilder sb = new StringBuilder();
        for (String _s : s) {
            if (sb.length() > 0)
                sb.append(',');
            sb.append(_s);
        }
        return sb.toString();
    }
}
