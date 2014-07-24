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
package org.everrest.guice;

import com.google.inject.Injector;

import org.everrest.core.BaseDependencySupplier;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GuiceDependencySupplier extends BaseDependencySupplier {
    private final Injector injector;

    public GuiceDependencySupplier(Injector injector) {
        this.injector = injector;
    }

    /** {@inheritDoc} */
    public Object getComponent(Class<?> type) {
        return injector.getInstance(type);
    }
}
