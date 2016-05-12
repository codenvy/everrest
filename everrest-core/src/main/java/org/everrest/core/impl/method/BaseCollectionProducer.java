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
package org.everrest.core.impl.method;

import org.everrest.core.method.TypeProducer;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class BaseCollectionProducer implements TypeProducer {
    /** Class of collection. */
    private Class<?> collectionClass;

    /**
     * Constructs BaseCollectionProducer.
     *
     * @param collectionClass
     *         class of Collections, should be one of {@link List}
     *         , {@link Set}, {@link SortedSet} .
     */
    protected BaseCollectionProducer(Class<?> collectionClass) {
        this.collectionClass = collectionClass;
    }

    /**
     * Creates collection's element.
     *
     * @param value
     *         this String will be used for creation object, usually String
     *         will be used as parameter for constructor or static method
     *         {@code valueOf}
     * @return newly created collection's element
     * @throws Exception
     *         if any error occurs
     */
    protected abstract Object createCollectionItem(String value) throws Exception;

    /**
     * Creates Object (it is Collection) and adds elements in it. Each element either:
     * <ul><li>Is a Sting</li> <li>Has a constructor that accepts a
     * single String argument</li> <li>Has a static method named
     * {@code valueOf} that accepts a single String argument</li></ul>. If values
     * map does not contain any value for parameter {@code param} then
     * {@code defaultValue} will be used, in this case Collection will contains
     * single element. {@inheritDoc}
     */
    @Override
    public Object createValue(String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception {
        List<String> list = values.get(param);
        if (values.get(param) != null) {
            Collection<Object> result = getCollection();
            for (String item : list) {
                result.add(createCollectionItem(item));
            }
            return result;
        } else if (defaultValue != null) {
            Collection<Object> result = getCollection();
            result.add(createCollectionItem(defaultValue));
            return result;
        }

        return null;
    }

    /**
     * Creates instance of collection corresponding to collection class, see {@link #collectionClass} .
     *
     * @return newly created collection
     */
    private Collection<Object> getCollection() {
        if (collectionClass == List.class) {
            return new ArrayList<>();
        } else if (collectionClass == Set.class) {
            return new HashSet<>();
        } else if (collectionClass == SortedSet.class) {
            return new TreeSet<>();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
