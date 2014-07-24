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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: CollectionStringProducer.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
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
    protected Object createValue(String value) throws Exception {
        return value;
    }
}
