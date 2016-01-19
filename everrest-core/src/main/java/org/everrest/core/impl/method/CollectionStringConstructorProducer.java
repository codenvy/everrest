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
package org.everrest.core.impl.method;

import java.lang.reflect.Constructor;

/**
 * Produce collections each element of it is object which has constructor with
 * single String argument.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: CollectionStringConstructorProducer.java 285 2009-10-15
 *          16:21:30Z aparfonov $
 */
public final class CollectionStringConstructorProducer extends BaseCollectionProducer {

    /** This constructor will be used for creation collection elements. */
    private Constructor<?> constructor;

    /**
     * Constructs new instance of CollectionStringConstructorProducer.
     *
     * @param collectionClass
     *         class of collection which must be created
     * @param constructor
     *         this constructor will be used for produce elements of
     *         collection
     */
    CollectionStringConstructorProducer(Class<?> collectionClass, Constructor<?> constructor) {
        super(collectionClass);
        this.constructor = constructor;
    }


    @Override
    protected Object createValue(String value) throws Exception {
        if (value == null) {
            return null;
        }

        return constructor.newInstance(value);
    }

}
