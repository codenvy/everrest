/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.test.mock;

import java.util.Map;

/** @author andrew00x */
public class CaseInsensetiveMap<T> extends java.util.HashMap<String, T> {

    private static final long serialVersionUID = -8562529039657285360L;

    //override putAll since in java8 in doesn't use method put.
    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        for (Map.Entry<? extends String, ? extends T> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }

    }


    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(getKey(key));
    }


    @Override
    public T get(Object key) {
        return super.get(getKey(key));
    }


    @Override
    public T put(String key, T value) {
        return super.put(getKey(key), value);
    }


    @Override
    public T remove(Object key) {
        return super.remove(getKey(key));
    }

    private String getKey(Object key) {
        if (key == null) {
            return null;
        }
        return key.toString().toLowerCase();
    }

}
