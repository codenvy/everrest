/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.util;

import org.everrest.core.ExtMultivaluedMap;
import org.everrest.core.uri.UriPattern;

import java.util.ArrayList;
import java.util.HashMap;
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
     *         never return null, empty List instead.
     */
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

    public void add(UriPattern uriPattern, V value) {
        if (value == null) {
            return;
        }
        List<V> list = getList(uriPattern);
        list.add(value);
    }

    public V getFirst(UriPattern uriPattern) {
        List<V> list = getList(uriPattern);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

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
