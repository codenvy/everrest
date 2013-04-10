/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.spring.servlet;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.servlet.EverrestApplication;
import org.everrest.core.servlet.EverrestServletContextInitializer;
import org.everrest.spring.SpringComponentsLoader;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

/**
 * SpringComponentsLoader which obtains resources and providers delivered via {@link Application} or obtained after
 * scanning JAX-RS components if Application is not configured as JAX-RS specification says.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SpringComponentsServletContextLoader extends SpringComponentsLoader implements ServletContextAware {
    private ServletContext servletContext;

    public SpringComponentsServletContextLoader(ResourceBinder resources, ApplicationProviderBinder providers,
                                                DependencySupplier dependencies) {
        super(resources, providers, dependencies);
    }

    public SpringComponentsServletContextLoader(ResourceBinder resources, ApplicationProviderBinder providers,
                                                EverrestConfiguration configuration, DependencySupplier dependencies) {
        super(resources, providers, configuration, dependencies);
    }

    /** {@inheritDoc} */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /** @see org.everrest.spring.SpringComponentsLoader#makeEverrestApplication() */
    @Override
    protected EverrestApplication makeEverrestApplication() {
        EverrestApplication everrest = super.makeEverrestApplication();
        EverrestServletContextInitializer everrestInitializer = new EverrestServletContextInitializer(servletContext);
        Application application = everrestInitializer.getApplication();
        if (application != null) {
            everrest.addApplication(application);
        }
        return everrest;
    }

}
