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

import java.lang.reflect.Constructor;

/**
 * Create object which has constructor with single String parameter.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: StringConstructorProducer.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public final class StringConstructorProducer extends BaseTypeProducer {
    /** Constructor which must be used for creation object. */
    private Constructor<?> constructor;

    /**
     * @param constructor
     *         this constructor will be used for creation instance of
     *         object
     */
    StringConstructorProducer(Constructor<?> constructor) {
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
