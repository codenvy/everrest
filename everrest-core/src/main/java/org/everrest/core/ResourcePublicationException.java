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
package org.everrest.core;

/**
 * Throws if root resource can't be published, e.g. resource can't be registered
 * because to conflict of root path
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@SuppressWarnings("serial")
public class ResourcePublicationException extends RuntimeException {
    public ResourcePublicationException(String message) {
        super(message);
    }

    public ResourcePublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
