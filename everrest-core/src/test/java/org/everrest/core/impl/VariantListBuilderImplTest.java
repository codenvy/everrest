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

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Variant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

public class VariantListBuilderImplTest {
    private VariantListBuilderImpl variantListBuilder;

    @Before
    public void setUp() throws Exception {
        variantListBuilder = new VariantListBuilderImpl();
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionOnAddIfNotAtLeastOneMediaTypeLanguageOrEncodingSet() {
        variantListBuilder.add();
    }

    @Test
    public void addsVariantsInList() {
        List<Variant> variants = variantListBuilder.mediaTypes(TEXT_PLAIN_TYPE).languages(new Locale("en")).add()
                                                   .mediaTypes(APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE).encodings("gzip").add()
                                                   .build();

        ArrayList<Variant> expectedVariants = newArrayList(new Variant(TEXT_PLAIN_TYPE, new Locale("en"), null),
                                                           new Variant(APPLICATION_XML_TYPE, (Locale)null, "gzip"),
                                                           new Variant(TEXT_PLAIN_TYPE, (Locale)null, "gzip"));
        assertEquals(expectedVariants, variants);
    }

    @Test
    public void addsLastVariantInListWhenCallBuild() {
        List<Variant> variants = variantListBuilder.mediaTypes(TEXT_PLAIN_TYPE).languages(new Locale("en")).add()
                                                   .mediaTypes(APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE).encodings("gzip")
                                                   .build();

        ArrayList<Variant> expectedVariants = newArrayList(new Variant(TEXT_PLAIN_TYPE, new Locale("en"), null),
                                                           new Variant(APPLICATION_XML_TYPE, (Locale)null, "gzip"),
                                                           new Variant(TEXT_PLAIN_TYPE, (Locale)null, "gzip"));
        assertEquals(expectedVariants, variants);
    }
}
