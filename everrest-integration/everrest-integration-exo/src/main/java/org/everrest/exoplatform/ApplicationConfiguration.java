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

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class ApplicationConfiguration extends Application {
    private final String      applicationName;
    private final Application application;

    public ApplicationConfiguration(String applicationName, Application application) {
        this.applicationName = applicationName;
        this.application = application;
    }

    /** @see javax.ws.rs.core.Application#getClasses() */
    @Override
    public Set<Class<?>> getClasses() {
        return application.getClasses();
    }

    /** @see javax.ws.rs.core.Application#getSingletons() */
    @Override
    public Set<Object> getSingletons() {
        return application.getSingletons();
    }

    /**
     * @return the applicationName unique identifier of JAX-RS Application. Identifier may be used to create Application
     *         specific invocation context for Resources delivered via this <code>application</code>, e.g. use Providers
     *         delivered with Application instead of embedded Providers (javax.ws.rs.ext.MessageBodyReader,
     *         javax.ws.rs.ext.MessageBodyWriter, etc) with for same purpose
     */
    public String getApplicationName() {
        return applicationName;
    }
}
