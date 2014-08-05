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

import org.everrest.core.method.TypeProducer;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Abstraction for single (not for collections) types.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class BaseTypeProducer implements TypeProducer {
    /**
     * Create object from given string. In all extends for this class this method
     * must be specified to produce object of required type. String will be used
     * as parameter for constructor of object or static valueOf method.
     *
     * @param value
     *         string value
     * @return newly created object
     * @throws Exception
     *         if any error occurs
     */
    protected abstract Object createValue(String value) throws Exception;


    @Override
    public Object createValue(String param, MultivaluedMap<String, String> values, String defaultValue) throws Exception {

        String value = values.getFirst(param);

        if (value != null) {
            return createValue(value);
        } else if (defaultValue != null) {
            return createValue(defaultValue);
        }

        return null;
    }
}
