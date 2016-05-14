/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.pico;

import org.everrest.core.BaseDependencySupplier;
import org.everrest.pico.servlet.EverrestPicoFilter;

/**
 * @author andrew00x
 */
public class PicoDependencySupplier extends BaseDependencySupplier {
    @Override
    public Object getInstance(Class<?> type) {
        return EverrestPicoFilter.getComponent(type);
    }

    @Override
    public Object getInstanceByName(String name) {
        return EverrestPicoFilter.getComponent(name);
    }
}
