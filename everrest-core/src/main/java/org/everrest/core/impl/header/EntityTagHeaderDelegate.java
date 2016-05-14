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

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author andrew00x
 */
public class EntityTagHeaderDelegate implements RuntimeDelegate.HeaderDelegate<EntityTag> {

    @Override
    public EntityTag fromString(String header) {
        if (header == null) {
            throw new IllegalArgumentException();
        }

        boolean isWeak = header.startsWith("W/");

        String value;
        if (isWeak) {
            value = cutWeakPrefix(header);
        } else {
            value = header;
        }
        value = value.substring(1, value.length() - 1);
        value = HeaderHelper.removeQuoteEscapes(value);

        return new EntityTag(value, isWeak);
    }

    private String cutWeakPrefix(String header) {
        return header.substring(2);
    }

    @Override
    public String toString(EntityTag entityTag) {
        if (entityTag == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder();
        if (entityTag.isWeak()) {
            sb.append('W').append('/');
        }

        sb.append('"');
        HeaderHelper.appendEscapeQuote(sb, entityTag.getValue());
        sb.append('"');

        return sb.toString();
    }
}
