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

import org.everrest.core.header.AbstractHeaderDelegate;

import javax.ws.rs.core.EntityTag;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: EntityTagHeaderDelegate.java 285 2009-10-15 16:21:30Z aparfonov
 *          $
 */
public class EntityTagHeaderDelegate extends AbstractHeaderDelegate<EntityTag> {
    /** {@inheritDoc} */
    @Override
    public Class<EntityTag> support() {
        return EntityTag.class;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public String toString(EntityTag entityTag) {
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
