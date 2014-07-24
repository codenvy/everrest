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
import java.util.List;
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
     *         then empty list will be returned instead null
     */
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

    public void add(MediaType mediaType, V value) {
        if (value == null) {
            return;
        }
        List<V> list = getList(mediaType);
        list.add(value);
    }

    public V getFirst(MediaType mime) {
        List<V> list = get(mime);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

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
