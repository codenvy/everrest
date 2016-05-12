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
package org.everrest.core.impl.async;

/**
 * Implementation of this interface may be notified when asynchronous job is done.
 *
 * @author andrew00x
 * @see AsynchronousJobPool
 */
public interface AsynchronousJobListener {
    void done(AsynchronousJob job);
}
