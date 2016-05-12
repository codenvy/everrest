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
package org.everrest.core;

import java.util.Map;

/**
 * Container for properties, that may be injected in resource by &#64Context
 * annotation.
 *
 * @author andrew00x
 */
public interface InitialProperties {

    /** @return all properties. */
    Map<String, String> getProperties();

    /**
     * Get property.
     *
     * @param name
     *         property name
     * @return value of property with specified name or null
     */
    String getProperty(String name);

    /**
     * Set property.
     *
     * @param name
     *         property name
     * @param value
     *         property value
     */
    void setProperty(String name, String value);

}
