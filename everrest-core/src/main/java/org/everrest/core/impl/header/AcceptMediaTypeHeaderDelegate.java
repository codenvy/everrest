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

import org.everrest.core.header.QualityValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
public class AcceptMediaTypeHeaderDelegate implements RuntimeDelegate.HeaderDelegate<AcceptMediaType> {

    @Override
    public AcceptMediaType fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }
        MediaType mediaType = MediaType.valueOf(header);
        return new AcceptMediaType(mediaType.getType(), mediaType.getSubtype(), mediaType.getParameters());
    }

    @Override
    public String toString(AcceptMediaType acceptedMediaType) {
        // Accept header maybe reused as content type header but need remove quality factor parameter.
        if (acceptedMediaType == null) {
            throw new IllegalArgumentException();
        }
        final Map<String, String> parameters = acceptedMediaType.getParameters();
        if (parameters.isEmpty() || (parameters.size() == 1 && parameters.containsKey(QualityValue.QVALUE))) {
            return new MediaType(acceptedMediaType.getType(), acceptedMediaType.getSubtype()).toString();
        }
        final Map<String, String> copyParameters = new LinkedHashMap<>(parameters);
        copyParameters.remove(QualityValue.QVALUE);
        return new MediaType(acceptedMediaType.getType(), acceptedMediaType.getSubtype(), copyParameters).toString();
    }
}
