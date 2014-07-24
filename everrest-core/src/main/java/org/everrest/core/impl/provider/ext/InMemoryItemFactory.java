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
package org.everrest.core.impl.provider.ext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
class InMemoryItemFactory implements FileItemFactory {
    private final int maxSize;

    public InMemoryItemFactory(int maxSize) {
        this.maxSize = maxSize;
    }

    /** {@inheritDoc} */
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new InMemoryFileItem(contentType, fieldName, isFormField, fileName, maxSize);
    }
}
