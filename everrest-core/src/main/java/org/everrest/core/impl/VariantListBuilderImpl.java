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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * See {@link VariantListBuilder}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class VariantListBuilderImpl extends VariantListBuilder {
    /** Languages. */
    private final List<Locale> languages = new ArrayList<Locale>();

    /** Encodings. */
    private final List<String> encodings = new ArrayList<String>();

    /** Media Types. */
    private final List<MediaType> mediaTypes = new ArrayList<MediaType>();

    /** List of {@link Variant}. */
    private List<Variant> variants;


    @Override
    public VariantListBuilder add() {
        if (variants == null) {
            variants = new ArrayList<Variant>();
        }

        Iterator<MediaType> mediaTypesIterator = mediaTypes.iterator();

        // do iteration at least one time, even all list are empty
        do {
            MediaType mediaType = mediaTypesIterator.hasNext() ? mediaTypesIterator.next() : null;
            Iterator<Locale> languagesIterator = languages.iterator();

            do {
                Locale language = languagesIterator.hasNext() ? languagesIterator.next() : null;
                Iterator<String> encodingsIterator = encodings.iterator();

                do {
                    String encoding = encodingsIterator.hasNext() ? encodingsIterator.next() : null;
                    variants.add(new Variant(mediaType, language, encoding));
                }
                while (encodingsIterator.hasNext());

            }
            while (languagesIterator.hasNext());

        }
        while (mediaTypesIterator.hasNext());

        clearAll();
        return this;
    }


    @Override
    public List<Variant> build() {
        return variants == null ? variants = new ArrayList<Variant>() : variants;
    }


    @Override
    public VariantListBuilder encodings(String... encs) {
        Collections.addAll(encodings, encs);
        return this;
    }


    @Override
    public VariantListBuilder languages(Locale... langs) {
        Collections.addAll(languages, langs);
        return this;
    }


    @Override
    public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        Collections.addAll(this.mediaTypes, mediaTypes);
        return this;
    }

    /** Reset builder to default state. */
    private void clearAll() {
        mediaTypes.clear();
        languages.clear();
        encodings.clear();
    }
}
