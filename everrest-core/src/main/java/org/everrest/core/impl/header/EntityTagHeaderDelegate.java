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
        // cut 'W/' prefix if exists
        if (isWeak) {
            value = header.substring(2);
        } else {
            value = header;
        }
        // remove quotes
        value = value.substring(1, value.length() - 1);
        value = HeaderHelper.filterEscape(value);

        return new EntityTag(value, isWeak);
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
