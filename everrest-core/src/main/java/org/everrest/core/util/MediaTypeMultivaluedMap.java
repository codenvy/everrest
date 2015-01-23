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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MediaTypeMultivaluedMap<V> extends MediaTypeMap<List<V>> implements ExtMultivaluedMap<MediaType, V> {
    private static final long serialVersionUID = 7082102018450744774L;

    /**
     * Get {@link List} with specified key. If it does not exist new one be
     * created.
     *
     * @param mediaType
     *         MediaType
     * @return List of ProviderFactory if no value mapped to the specified key
     * then empty list will be returned instead null
     */
    @Override
    public List<V> getList(MediaType mediaType) {
        List<V> list = get(mediaType);
        if (list == null) {
            List<V> newList = new CopyOnWriteArrayList<>();
            list = putIfAbsent(mediaType, newList);
            if (list == null) {
                list = newList;
            }
        }
        return list;
    }

    @Override
    public void add(MediaType mediaType, V value) {
        if (value == null) {
            return;
        }
        List<V> list = getList(mediaType);
        list.add(value);
    }

    @Override
    public V getFirst(MediaType mime) {
        List<V> list = get(mime);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void addAll(MediaType key, V... newValues) {
        if (newValues == null) {
            throw new NullPointerException("Null array of values isn't acceptable.");
        }
        if (newValues.length == 0) {
            return;
        }
        Collections.addAll(getList(key), newValues);
    }

    @Override
    public void addAll(MediaType key, List<V> valueList) {
        if (valueList == null) {
            throw new NullPointerException("Null list of values isn't acceptable.");
        }
        if (valueList.isEmpty()) {
            return;
        }
        getList(key).addAll(valueList);
    }

    @Override
    public void addFirst(MediaType key, V value) {
        getList(key).add(0, value);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<MediaType, V> otherMap) {
        if (this == otherMap) {
            return true;
        }
        Set<MediaType> myKeys = keySet();
        Set<MediaType> otherKeys = otherMap.keySet();
        if (!myKeys.equals(otherKeys)) {
            return false;
        }
        for (Entry<MediaType, List<V>> e : entrySet()) {
            List<V> myValues = e.getValue();
            List<V> otherValues = otherMap.get(e.getKey());
            if (myValues != null || otherValues != null) {
                if (myValues != null) {
                    if (myValues.size() != otherValues.size()) {
                        return false;
                    }
                    for (V value : myValues) {
                        if (!otherValues.contains(value)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void putSingle(MediaType mediaType, V value) {
        if (value == null) {
            remove(mediaType);
            return;
        }
        List<V> list = getList(mediaType);
        list.clear();
        list.add(value);
    }
}
