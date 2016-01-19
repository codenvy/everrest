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
package org.everrest.core;

/**
 * Interface provides methods lifecycle control.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface Lifecycle {
    /**
     * Star Lifecycle. If this interface implemented by container it must notify all its components. If Lifecycle already
     * started repeated calling of this method has no effect.
     */
    void start();

    /**
     * Stop Lifecycle. If this interface implemented by container it must notify all its components. If Lifecycle already
     * stopped repeated calling of this method has no effect.
     */
    void stop();
}
