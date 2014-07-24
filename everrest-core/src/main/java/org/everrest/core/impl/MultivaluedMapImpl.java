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

import org.everrest.core.ExtMultivaluedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see javax.ws.rs.core.MultivaluedMap
 */
public class MultivaluedMapImpl extends HashMap<String, List<String>> implements ExtMultivaluedMap<String, String> {
    private static final long serialVersionUID = -6066678602537059655L;

    /** {@inheritDoc} */
    public void add(String key, String value) {
        if (value == null) {
            return;
        }
        List<String> list = getList(key);
        list.add(value);
    }

    /** {@inheritDoc} */
    public String getFirst(String key) {
        List<String> list = get(key);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    /** {@inheritDoc} */
    public void putSingle(String key, String value) {
        if (value == null) {
            remove(key);
            return;
        }
        List<String> list = getList(key);
        list.clear();
        list.add(value);
    }

    /** {@inheritDoc} */
    public List<String> getList(String key) {
        List<String> list = get(key);
        if (list == null) {
            list = new ArrayList<String>();
            put(key, list);
        }
        return list;
    }
}
