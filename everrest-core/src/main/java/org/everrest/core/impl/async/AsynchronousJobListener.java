/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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
