/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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
