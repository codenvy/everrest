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
package org.everrest.websockets;

/**
 * Notified when WSConnection opened and closed. Implementation of this interface should be
 * registered in WSConnectionContext context.
 *
 * @author andrew00x
 * @see WSConnectionContext#registerConnectionListener(WSConnectionListener)
 * @see WSConnectionContext#removeConnectionListener(WSConnectionListener)
 */
public interface WSConnectionListener {
  /**
   * Called when new connection opened.
   *
   * @param connection new connection
   */
  void onOpen(WSConnection connection);

  /**
   * Called when connection closed.
   *
   * @param connection closed connection
   */
  void onClose(WSConnection connection);
}
