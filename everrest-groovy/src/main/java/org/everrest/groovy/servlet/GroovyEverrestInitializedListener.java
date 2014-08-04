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
package org.everrest.groovy.servlet;

import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.servlet.EverrestServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Application;

/**
 * @author andrew00x
 */
public class GroovyEverrestInitializedListener implements ServletContextListener {

    /** {@inheritDoc} */
    public void contextDestroyed(ServletContextEvent event) {
    }

    /** {@inheritDoc} */
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sctx = event.getServletContext();
        EverrestProcessor processor = (EverrestProcessor)sctx.getAttribute(EverrestProcessor.class.getName());
        if (processor == null) {
            throw new RuntimeException("EverrestProcessor not found. ");
        }
        EverrestServletContextInitializer initializer = new GroovyEverrestServletContextInitializer(sctx);
        Application application = initializer.getApplication();
        if (application != null) {
            processor.addApplication(application);
        }
    }
}
