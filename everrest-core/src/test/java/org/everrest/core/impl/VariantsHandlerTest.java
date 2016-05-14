/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Variant;
import java.util.List;
import java.util.Locale;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VariantsHandlerTest {
    private VariantsHandler variantsHandler;

    @Before
    public void setUp() throws Exception {
        variantsHandler = new VariantsHandler();
    }

    @Test
    public void selectsVariant() throws Exception {
        List<Variant> variantList = Variant.mediaTypes(MediaType.valueOf("image/jpeg")).add()
                                           .mediaTypes(MediaType.valueOf("text/xml")).languages(new Locale("en", "gb")).add()
                                           .mediaTypes(MediaType.valueOf("text/xml;charset=utf-8")).languages(new Locale("en", "us")).add()
                                           .mediaTypes(MediaType.valueOf("text/xml;charset=utf-8")).languages(new Locale("en", "us")).encodings("gzip").add()
                                           .build();

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle(ACCEPT, Joiner.on(',').join("text/xml", "image/png", "text/html;q=0.9", "text/plain;q=0.8", "application/xml", "*/*;q=0.5"));
        headers.putSingle(ACCEPT_LANGUAGE, "en-us,en;q=0.5");
        headers.putSingle(ACCEPT_CHARSET, "utf-8,koi8;q=0.7");
        headers.putSingle(ACCEPT_ENCODING, "gzip");

        ContainerRequest request = new ContainerRequest("GET", null, null, null, headers, null);

        Variant variant = variantsHandler.handleVariants(request, variantList);
        assertNotNull(variant);
        assertEquals(new MediaType("text", "xml", ImmutableMap.of("charset", "utf-8")), variant.getMediaType());
        assertEquals(new Locale("en", "us"), variant.getLanguage());
        assertEquals("gzip", variant.getEncoding());
    }
}
