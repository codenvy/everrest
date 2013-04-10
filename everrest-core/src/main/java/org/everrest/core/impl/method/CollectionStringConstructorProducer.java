/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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

    /** {@inheritDoc} */
    @Override
    protected Object createValue(String value) throws Exception {
        if (value == null) {
            return null;
        }

        return constructor.newInstance(value);
    }

}
