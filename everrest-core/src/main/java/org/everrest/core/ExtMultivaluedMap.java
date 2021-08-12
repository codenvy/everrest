/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * Extension of {@link MultivaluedMap} that allows to get not null value (empty list) even there is
 * no mapping value to supplied key.
 *
 * @param <K> key
 * @param <V> value
 * @author andrew00x
 * @see #getList(Object)
 */
public interface ExtMultivaluedMap<K, V> extends MultivaluedMap<K, V> {

  /** @return never null even any value not found in the map, return empty list instead */
  List<V> getList(K key);
}
