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

import static com.google.common.base.Preconditions.checkState;

/**
 * See {@link VariantListBuilder}.
 */
public class VariantListBuilderImpl extends VariantListBuilder {
    /** Languages. */
    private final List<Locale> languages;
    /** Encodings. */
    private final List<String> encodings;
    /** Media Types. */
    private final List<MediaType> mediaTypes;

    /** List of {@link Variant}. */
    private List<Variant> variants;

    public VariantListBuilderImpl() {
        languages = new ArrayList<>();
        encodings = new ArrayList<>();
        mediaTypes = new ArrayList<>();
    }

    @Override
    public VariantListBuilder add() {
        checkState(!(mediaTypes.isEmpty() && languages.isEmpty() && encodings.isEmpty()),
                   "At least one media type, language or encoding must be set");

        if (variants == null) {
            variants = new ArrayList<>();
        }

        Iterator<MediaType> mediaTypesIterator = mediaTypes.iterator();
        do {
            MediaType mediaType = mediaTypesIterator.hasNext() ? mediaTypesIterator.next() : null;
            Iterator<Locale> languagesIterator = languages.iterator();
            do {
                Locale language = languagesIterator.hasNext() ? languagesIterator.next() : null;
                Iterator<String> encodingsIterator = encodings.iterator();
                do {
                    String encoding = encodingsIterator.hasNext() ? encodingsIterator.next() : null;
                    variants.add(new Variant(mediaType, language, encoding));
                } while (encodingsIterator.hasNext());
            } while (languagesIterator.hasNext());
        } while (mediaTypesIterator.hasNext());

        clearAll();
        return this;
    }

    @Override
    public List<Variant> build() {
        if (mediaTypes.isEmpty() && languages.isEmpty() && encodings.isEmpty()) {
            if (variants == null) {
                variants = new ArrayList<>();
            }
        } else {
            add();
        }
        return variants;
    }

    @Override
    public VariantListBuilder encodings(String... encodings) {
        if (encodings != null) {
            Collections.addAll(this.encodings, encodings);
        }
        return this;
    }

    @Override
    public VariantListBuilder languages(Locale... languages) {
        if (languages != null) {
            Collections.addAll(this.languages, languages);
        }
        return this;
    }

    @Override
    public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        if (mediaTypes != null) {
            Collections.addAll(this.mediaTypes, mediaTypes);
        }
        return this;
    }

    /** Reset builder to default state. */
    private void clearAll() {
        mediaTypes.clear();
        languages.clear();
        encodings.clear();
    }
}
