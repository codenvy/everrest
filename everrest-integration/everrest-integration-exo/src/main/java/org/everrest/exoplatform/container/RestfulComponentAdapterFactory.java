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
package org.everrest.exoplatform.container;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.NotConcreteRegistrationException;

/**
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class RestfulComponentAdapterFactory implements ComponentAdapterFactory {
    private final ComponentAdapterFactory delegate;

    public RestfulComponentAdapterFactory(ComponentAdapterFactory delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
    }

    /**
     * @see org.picocontainer.defaults.ComponentAdapterFactory#createComponentAdapter(java.lang.Object, java.lang.Class,
     *      org.picocontainer.Parameter[])
     */
    @SuppressWarnings("rawtypes")
    @Override
    public ComponentAdapter createComponentAdapter(Object componentKey, Class componentImplementation,
                                                   Parameter[] parameters)
            throws PicoIntrospectionException, AssignabilityRegistrationException,
                   NotConcreteRegistrationException {
        if (RestfulComponentAdapter.isRestfulComponent(componentImplementation)) {
            return new RestfulComponentAdapter(componentKey, componentImplementation);
        }
        return delegate.createComponentAdapter(componentKey, componentImplementation, parameters);
    }
}
