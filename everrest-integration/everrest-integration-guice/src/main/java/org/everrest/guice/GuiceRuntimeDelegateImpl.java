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

import org.everrest.core.impl.RuntimeDelegateImpl;

import javax.ws.rs.core.UriBuilder;

/**
 * RuntimeDelegate implementation which provides adopted to guice proxies instance of UriBuilder.
 *
 * @author Max Shaposhnik
 */
public class GuiceRuntimeDelegateImpl extends RuntimeDelegateImpl {

    @Override
    public UriBuilder createUriBuilder() {
        return new GuiceUriBuilderImpl();
    }
}
