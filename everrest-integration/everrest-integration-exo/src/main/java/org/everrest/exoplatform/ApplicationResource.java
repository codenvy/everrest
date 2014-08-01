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

import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;

/**
 * Resource descriptor that can provide information about the Application, through which it was delivered.
 * Need to know that to be able use specified set of Providers. Provides delivered via JAX-RS Application
 * always has an advantage over embedded Providers ({@link javax.ws.rs.ext.MessageBodyReader},
 * {@link javax.ws.rs.ext.MessageBodyWriter}, {@link javax.ws.rs.ext.ExceptionMapper}, etc).
 *
 * @author andrew00
 */
public final class ApplicationResource extends AbstractResourceDescriptorImpl {
    private final String applicationName;

    public ApplicationResource(String applicationName, Class<?> resourceClass) {
        super(resourceClass);
        this.applicationName = applicationName;
    }

    public ApplicationResource(String applicationName, Object resource) {
        super(resource);
        this.applicationName = applicationName;
    }

    /**
     * @return identifier of application-supplied subclass of {@link javax.ws.rs.core.Application}
     *         via this component was delivered
     */
    public String getApplicationName() {
        return applicationName;
    }
}
