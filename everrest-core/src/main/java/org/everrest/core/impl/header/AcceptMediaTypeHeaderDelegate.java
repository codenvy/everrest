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
package org.everrest.core.impl.header;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.everrest.core.header.QualityValue.QVALUE;

public class AcceptMediaTypeHeaderDelegate implements RuntimeDelegate.HeaderDelegate<AcceptMediaType> {

    @Override
    public AcceptMediaType fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }
        return new AcceptMediaType(MediaType.valueOf(header));
    }

    @Override
    public String toString(AcceptMediaType acceptMediaType) {
        if (acceptMediaType == null) {
            throw new IllegalArgumentException();
        }
        final Map<String, String> parameters = acceptMediaType.getParameters();
        if (parameters.isEmpty() || (parameters.size() == 1 && parameters.containsKey(QVALUE))) {
            return acceptMediaType.getMediaType().toString();
        }
        final Map<String, String> copyParameters = new LinkedHashMap<>(parameters);
        copyParameters.remove(QVALUE);
        return new MediaType(acceptMediaType.getType(), acceptMediaType.getSubtype(), copyParameters).toString();
    }
}
