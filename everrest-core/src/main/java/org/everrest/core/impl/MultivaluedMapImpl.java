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
package org.everrest.core.impl;

import org.everrest.core.ExtMultivaluedMap;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @author andrew00x
 * @see javax.ws.rs.core.MultivaluedMap
 */
public class MultivaluedMapImpl extends MultivaluedHashMap<String, String> implements ExtMultivaluedMap<String, String> {
    private static final long serialVersionUID = -6066678602537059655L;

    @Override
    public List<String> getList(String key) {
        List<String> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            put(key, list);
        }
        return list;
    }
}
