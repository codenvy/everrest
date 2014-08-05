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

import org.everrest.core.header.QualityValue;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class AcceptToken extends Token implements QualityValue {
    /** Quality value factor. */
    private final float qValue;

    /**
     * Create AcceptToken with default quality value 1.0 .
     *
     * @param token
     *         a token
     */
    public AcceptToken(String token) {
        super(token);
        qValue = DEFAULT_QUALITY_VALUE;
    }

    /**
     * Create AcceptToken with specified quality value.
     *
     * @param token
     *         a token
     * @param qValue
     *         a quality value
     */
    public AcceptToken(String token, float qValue) {
        super(token);
        this.qValue = qValue;
    }

    // QualityValue


    @Override
    public float getQvalue() {
        return qValue;
    }
}
