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
package org.everrest.core.impl.method;

/**
 * Produce collections each element of it is String.
 *
 * @author andrew00x
 */
public final class CollectionStringProducer extends BaseCollectionProducer {
    /**
     * Constructs new instance of CollectionStringProducer.
     *
     * @param collectionClass
     *         class of collection which must be created
     */
    CollectionStringProducer(Class<?> collectionClass) {
        super(collectionClass);
    }

    /**
     * Don't need to do anything just return passed in method String as is.
     * {@inheritDoc}
     */
    @Override
    protected Object createCollectionItem(String value) throws Exception {
        return value;
    }
}
