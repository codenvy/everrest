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
package org.everrest.guice.impl;

import org.everrest.core.impl.RuntimeDelegateImpl;
import org.everrest.guice.uri.GuiceUriBuilderImpl;

import javax.ws.rs.core.UriBuilder;

/**
 * @author Max Shaposhnik
 */
public class GuiceRuntimeDelegateImpl extends RuntimeDelegateImpl {

    @Override
    public UriBuilder createUriBuilder() {
        return new GuiceUriBuilderImpl();
    }
}
