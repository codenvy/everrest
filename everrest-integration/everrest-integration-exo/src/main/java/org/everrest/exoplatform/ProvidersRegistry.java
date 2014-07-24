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

import org.everrest.core.impl.ProviderBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps mapping set of Providers to Application name.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ProvidersRegistry {
    protected final Map<String, ProviderBinder> all = new HashMap<String, ProviderBinder>();

    public void addProviders(String applicationName, ProviderBinder apb) {
        all.put(applicationName, apb);
    }

    public ProviderBinder getProviders(String applicationName) {
        return all.get(applicationName);
    }
}
