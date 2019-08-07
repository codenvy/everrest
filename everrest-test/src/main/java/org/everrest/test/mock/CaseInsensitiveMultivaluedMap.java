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
package org.everrest.test.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
public class CaseInsensitiveMultivaluedMap<T> extends HashMap<String, List<T>> {

    private static final long serialVersionUID = 6637313979061607685L;

    //override putAll since in java8 in doesn't use method put.
    @Override
    public void putAll(Map<? extends String, ? extends List<T>> m) {
        for (Map.Entry<? extends String, ? extends List<T>> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }


    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(getKey(key));
    }


    @Override
    public List<T> get(Object key) {
        return getList(getKey(key));
    }


    @Override
    public List<T> put(String key, List<T> value) {
        return super.put(getKey(key), value);
    }


    @Override
    public List<T> remove(Object key) {
        return super.remove(getKey(key));
    }

    public T getFirst(String key) {
        List<T> l = getList(key);
        if (l.size() == 0)
            return null;
        return l.get(0);
    }

    private List<T> getList(String key) {
        List<T> l = super.get(getKey(key));
        if (l == null)
            l = new ArrayList<T>();
        put(key, l);
        return l;
    }

    private String getKey(Object key) {
        if (key == null) {
            return null;
        }
        return key.toString().toLowerCase();
    }

}