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
package org.everrest.guice;

import javax.ws.rs.core.UriBuilder;
import org.everrest.core.impl.RuntimeDelegateImpl;

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
