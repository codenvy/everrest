/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
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

import java.lang.reflect.Method;

/**
 * Produces collections each element of it is object of class which has static {@code valueOf} with single String argument.
 *
 * @author andrew00x
 */
public final class CollectionStringValueOfProducer extends BaseCollectionProducer {
    /** This method will be used for creation collection elements. */
    private Method valueOfMethod;

    /**
     * Constructs new instance of CollectionStringValueOfProducer.
     *
     * @param collectionClass
     *         class of collection which must be created
     * @param valueOfMethod
     *         this method will be used for produce elements of
     *         collection
     */
    CollectionStringValueOfProducer(Class<?> collectionClass, Method valueOfMethod) {
        super(collectionClass);
        this.valueOfMethod = valueOfMethod;
    }


    @Override
    protected Object createCollectionItem(String value) throws Exception {
        if (value == null) {
            return null;
        }

        return valueOfMethod.invoke(null, value);
    }
}
