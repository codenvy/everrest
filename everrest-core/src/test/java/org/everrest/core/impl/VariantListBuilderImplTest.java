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
package org.everrest.core.impl;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.Variant;
import org.junit.Before;
import org.junit.Test;

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
    List<Variant> variants =
        variantListBuilder
            .mediaTypes(TEXT_PLAIN_TYPE)
            .languages(new Locale("en"))
            .add()
            .mediaTypes(APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE)
            .encodings("gzip")
            .add()
            .build();

    ArrayList<Variant> expectedVariants =
        newArrayList(
            new Variant(TEXT_PLAIN_TYPE, new Locale("en"), null),
            new Variant(APPLICATION_XML_TYPE, (Locale) null, "gzip"),
            new Variant(TEXT_PLAIN_TYPE, (Locale) null, "gzip"));
    assertEquals(expectedVariants, variants);
  }

  @Test
  public void addsLastVariantInListWhenCallBuild() {
    List<Variant> variants =
        variantListBuilder
            .mediaTypes(TEXT_PLAIN_TYPE)
            .languages(new Locale("en"))
            .add()
            .mediaTypes(APPLICATION_XML_TYPE, TEXT_PLAIN_TYPE)
            .encodings("gzip")
            .build();

    ArrayList<Variant> expectedVariants =
        newArrayList(
            new Variant(TEXT_PLAIN_TYPE, new Locale("en"), null),
            new Variant(APPLICATION_XML_TYPE, (Locale) null, "gzip"),
            new Variant(TEXT_PLAIN_TYPE, (Locale) null, "gzip"));
    assertEquals(expectedVariants, variants);
  }
}
