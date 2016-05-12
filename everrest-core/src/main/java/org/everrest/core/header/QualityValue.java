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
package org.everrest.core.header;

/**
 * Implementation of this interface is useful for sort accepted media type and languages by quality factor. For example see
 * {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">HTTP/1.1 documentation</a>}.
 *
 * @author andrew00x
 */
public interface QualityValue {
    /**
     * Default quality value. It should be used if quality value is not specified in accept token.
     */
    float DEFAULT_QUALITY_VALUE = 1.0F;

    /** Quality value. */
    String QVALUE = "q";

    /** @return value of quality parameter */
    float getQvalue();
}
