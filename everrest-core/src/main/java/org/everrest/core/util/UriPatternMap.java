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
package org.everrest.core.util;

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.uri.UriPattern;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @param <V>
 *         {@link org.everrest.core.RequestFilter} or
 *         {@link org.everrest.core.ResponseFilter}
 */
public class UriPatternMap<V> extends ConcurrentHashMap<UriPattern, List<V>> implements ExtMultivaluedMap<UriPattern, V> {
    private static final long serialVersionUID = 8248982446381545144L;

    /**
     * @param uriPattern
     *         the key
     * @return List of Object mapped to specified <tt>uriPattern</tt>. Method
     * never return null, empty List instead.
     */
    @Override
    public List<V> getList(UriPattern uriPattern) {
        List<V> list = get(uriPattern);
        if (list == null) {
            List<V> newList = new CopyOnWriteArrayList<>();
            list = putIfAbsent(uriPattern, newList);
            if (list == null) {
                list = newList;
            }
        }
        return list;
    }

    @Override
    public void add(UriPattern uriPattern, V value) {
        if (value == null) {
            return;
        }
        List<V> list = getList(uriPattern);
        list.add(value);
    }

    @Override
    public V getFirst(UriPattern uriPattern) {
        List<V> list = getList(uriPattern);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void putSingle(UriPattern uriPattern, V value) {
        if (value == null) {
            remove(uriPattern);
            return;
        }
        List<V> list = getList(uriPattern);
        list.clear();
        list.add(value);
    }
}
