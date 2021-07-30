/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.guice.servlet;

import com.google.inject.Singleton;
import org.everrest.core.servlet.EverrestServlet;

/**
 * Has additional {@link Singleton} annotation which required for web components by guice container.
 * Since we want to have possibility to deploy servlet via {@link
 * com.google.inject.servlet.ServletModule#configureServlets ServletModule.configureServlets} .
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
@Singleton
public final class GuiceEverrestServlet extends EverrestServlet {}
