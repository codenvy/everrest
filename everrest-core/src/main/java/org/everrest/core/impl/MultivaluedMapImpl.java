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

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.LinkedList;
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
            list = new LinkedList<>();
            put(key, list);
        }
        return list;
    }
}
