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
package org.everrest.guice.servlet;

import com.google.inject.Singleton;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.config.WebXMLReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;

import org.everrest.core.servlet.EverrestServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Has additional {@link Singleton} annotation which required for web components
 * by guice container. Since we want to have possibility to deploy servlet via
 * {@link com.google.inject.servlet.ServletModule#configureServlets
 * ServletModule.configureServlets} .
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
@SuppressWarnings("serial")
@Singleton
public final class GuiceEverrestServlet extends EverrestServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        ConfigFactory.setConfig(new WebXMLReader(config));
//        final SwaggerConfig swaggerConfig = new SwaggerConfig();
//        swaggerConfig.setBasePath("http://localhost:8080/guice-book-service");
//        ConfigFactory.setConfig(swaggerConfig);
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());
        super.init(config);
    }
}
