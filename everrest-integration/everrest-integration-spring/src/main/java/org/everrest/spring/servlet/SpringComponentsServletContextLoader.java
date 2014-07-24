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
