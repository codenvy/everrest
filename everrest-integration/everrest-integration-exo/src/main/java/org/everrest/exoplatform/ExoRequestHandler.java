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

import org.everrest.core.DependencySupplier;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.exoplatform.container.xml.InitParams;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class ExoRequestHandler extends RequestHandlerImpl {
    public ExoRequestHandler(RequestDispatcher dispatcher, DependencySupplier dependencySupplier, InitParams initParams) {
        super(dispatcher, dependencySupplier, EverrestConfigurationHelper.createEverrestConfiguration(initParams));
    }

    public ExoRequestHandler(RequestDispatcher dispatcher, DependencySupplier dependencySupplier) {
        this(dispatcher, dependencySupplier, null);
    }
}
