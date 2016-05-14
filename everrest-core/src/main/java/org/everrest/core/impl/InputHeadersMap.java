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
