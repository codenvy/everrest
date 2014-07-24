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

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: BaseCollectionProducer.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
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
     * Create collection's element.
     *
     * @param value
     *         this String will be used for creation object, usually String
     *         will be used as parameter for constructor or static method
     *         <code>valueOf</code>
     * @return newly created collection's element
     * @throws Exception
     *         if any error occurs
     */
    protected abstract Object createValue(String value) throws Exception;

    /**
     * Create Object (it is Collection) and add in it elements. Elements must
     * either: <li>To be a Sting</li> <li>Have a constructor that accepts a
     * single String argument.</li> <li>Have a static method named
     * <code>valueOf</code> that accepts a single String argument</li>. If values
     * map does not contains any value for parameter <i>param</i> then
     * <i>defaultValue</i> will be used, in this case Collection will contains
     * single element. {@inheritDoc}
     */
    public Object createValue(String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception {
        List<String> list = values.get(param);
        if (list != null) {
            Collection<Object> coll = getCollection();

            for (String v : list) {
                coll.add(createValue(v));
            }

            return coll;
        } else if (defaultValue != null) {
            Collection<Object> coll = getCollection();
            coll.add(createValue(defaultValue));
            return coll;
        }

        return null;
    }

    /**
     * Create instance of collection corresponding to collection class, see
     * {@link #collectionClass} .
     *
     * @return newly created collection
     */
    private Collection<Object> getCollection() {
        if (collectionClass == List.class) {
            return new ArrayList<Object>();
        } else if (collectionClass == Set.class) {
            return new HashSet<Object>();
        } else if (collectionClass == SortedSet.class) {
            return new TreeSet<Object>();
        } else {
            throw new IllegalArgumentException();
        }
    }

}
