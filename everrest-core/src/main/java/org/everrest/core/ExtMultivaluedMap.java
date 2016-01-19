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
package org.everrest.core;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * Extension of {@link MultivaluedMap} that allows to get not null value (empty
 * list) even there is no mapping value to supplied key.
 *
 * @param <K>
 *         key
 * @param <V>
 *         value
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see #getList(Object)
 */
public interface ExtMultivaluedMap<K, V> extends MultivaluedMap<K, V> {

    /**
     * @param key
     *         key
     * @return never null even any value not found in the map, return empty list
     * instead
     */
    List<V> getList(K key);

}
