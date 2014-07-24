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

import org.everrest.core.util.CaselessMultivaluedMap;

import java.util.List;
import java.util.Map;

/**
 * Output HTTP headers..
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class OutputHeadersMap extends CaselessMultivaluedMap<Object> {
    private static final long serialVersionUID = -8663077335341263345L;

    public OutputHeadersMap() {
    }

    public OutputHeadersMap(Map<String, List<Object>> m) {
        super(m);
    }
}
