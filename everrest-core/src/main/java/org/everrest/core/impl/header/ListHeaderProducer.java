/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import org.everrest.core.header.QualityValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.everrest.core.impl.header.HeaderHelper.QUALITY_VALUE_COMPARATOR;
import static org.everrest.core.util.StringUtils.charAtIs;
import static org.everrest.core.util.StringUtils.scan;

/**
 * Produces sorted by quality value list of 'accept' headers, e.g. 'accept', 'accept-language'.
 *
 * @param <T>
 *         type that implements {@link QualityValue}
 * @author andrew00x
 */
public class ListHeaderProducer<T extends QualityValue> {

    private final ListItemFactory<T> itemFactory;

    public ListHeaderProducer(ListItemFactory<T> itemFactory) {
        this.itemFactory = itemFactory;
    }

    /**
     * Parses given string to list of QualityValue. List is sorted by {@link QualityValue#getQvalue()}.
     *
     * @param header
     *         source header string
     * @return sorted list QualityValue
     * @see ListItemFactory
     */
    public List<T> createQualitySortedList(String header) {
        final List<T> tokens = new ArrayList<>();
        int n;
        int p = 0;
        final int length = header.length();
        while (p < length) {
            n = scan(header, p, ',');

            String token;
            if (charAtIs(header, n, ',')) {
                token = header.substring(p, n);
            } else {
                token = header.substring(p);
                n = length;
            }

            tokens.add(itemFactory.createItem(token));

            p = n + 1;
        }

        if (tokens.size() > 1) {
            Collections.sort(tokens, QUALITY_VALUE_COMPARATOR);
        }

        return tokens;
    }

    public interface ListItemFactory<T> {
        T createItem(String part);
    }
}
