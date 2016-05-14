/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import org.everrest.core.servlet.EverrestServlet;

/**
 * Has additional {@link Singleton} annotation which required for web components
 * by guice container. Since we want to have possibility to deploy servlet via
 * {@link com.google.inject.servlet.ServletModule#configureServlets
 * ServletModule.configureServlets} .
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
@Singleton
public final class GuiceEverrestServlet extends EverrestServlet {
}
