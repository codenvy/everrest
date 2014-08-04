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
package org.everrest.exoplatform;

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValueParam;

import java.util.Iterator;

/**
 * @author andrew00x
 */
public final class EverrestConfigurationHelper extends EverrestConfiguration {
    public static final String DEFAULT_RESTFUL_CONTAINER_NAME = "everrest";

    public static EverrestConfiguration createEverrestConfiguration(final InitParams initParams) {
        // Get all parameters from init-params so not need servlet context and pass null instead.
        EverrestConfiguration configuration = new EverrestServletContextInitializer(null) {
            @Override
            public String getParameter(String name) {
                if (initParams != null) {
                    ValueParam valueParam = initParams.getValueParam(name);
                    if (valueParam != null) {
                        return valueParam.getValue();
                    }
                }
                return null;
            }
        }.getConfiguration();
        if (initParams != null) {
            PropertiesParam properties = initParams.getPropertiesParam("properties");
            if (properties != null) {
                for (Iterator<Property> iterator = properties.getPropertyIterator(); iterator.hasNext(); ) {
                    Property prop = iterator.next();
                    configuration.setProperty(prop.getName(), prop.getValue());
                }
            }
        }
        return configuration;
    }

    private EverrestConfigurationHelper() {
    }
}
