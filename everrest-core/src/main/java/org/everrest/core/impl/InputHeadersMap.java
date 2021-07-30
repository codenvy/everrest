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
package org.everrest.core.impl;

import org.everrest.core.util.CaselessUnmodifiableMultivaluedMap;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

/**
 * Read only case insensitive {@link MultivaluedMap}.
 */
public final class InputHeadersMap extends CaselessUnmodifiableMultivaluedMap<String> {
    private static final long serialVersionUID = -96963220577144285L;

    public InputHeadersMap(Map<String, List<String>> map) {
        super(map);
    }
}
