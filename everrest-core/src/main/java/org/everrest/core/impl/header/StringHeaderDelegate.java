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
package org.everrest.core.impl.header;

import javax.ws.rs.ext.RuntimeDelegate;

/** @author andrew00x */
public class StringHeaderDelegate implements RuntimeDelegate.HeaderDelegate<String> {

  @Override
  public String fromString(String value) {
    return value;
  }

  @Override
  public String toString(String value) {
    return value;
  }
}
