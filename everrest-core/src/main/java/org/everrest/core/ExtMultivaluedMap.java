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
     *         instead
     */
    List<V> getList(K key);

}
