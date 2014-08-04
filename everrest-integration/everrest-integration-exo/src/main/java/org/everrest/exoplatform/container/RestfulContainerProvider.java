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

import org.exoplatform.container.ExoContainerContext;

import javax.inject.Provider;

/**
 * Provider may be used to get instance of RestfulContainer. It can be used for:
 * <ul>
 * <li>lazy initialization of RestfulContainer</li>
 * <li>if have circular dependency problem</li>
 * </ul>
 *
 * @author andrew00x
 */
public class RestfulContainerProvider implements Provider<RestfulContainer> {
    @Override
    public RestfulContainer get() {
        return (RestfulContainer)ExoContainerContext.getCurrentContainer().getComponentInstance("everrest");
    }
}
