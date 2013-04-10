/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.exoplatform;

import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValueParam;

import java.util.Iterator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
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
