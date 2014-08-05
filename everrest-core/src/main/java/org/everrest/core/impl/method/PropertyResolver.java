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

import org.everrest.core.ApplicationContext;
import org.everrest.core.Property;

/**
 * Obtain value of property (see {@link org.everrest.core.InitialProperties}) with name supplied
 * in {@link Property#value()} .
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class PropertyResolver extends ParameterResolver<Property> {
    /** See {@link Property} */
    private final Property property;

    /**
     * @param property
     *         Property
     */
    PropertyResolver(Property property) {
        this.property = property;
    }


    @Override
    public Object resolve(org.everrest.core.Parameter parameter, ApplicationContext context) throws Exception {
        if (parameter.getParameterClass() != String.class) {
            throw new IllegalArgumentException(
                    "Only parameters and fields with string type may be annotated by @Property.");
        }
        String param = this.property.value();

        Object value = context.getInitialProperties().getProperties().get(param);
        if (value == null) {
            return parameter.getDefaultValue();
        }

        return value;
    }
}
