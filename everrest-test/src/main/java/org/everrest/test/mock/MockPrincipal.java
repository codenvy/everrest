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
package org.everrest.test.mock;

/** @author Mestrallet Benjamin */
public class MockPrincipal implements java.security.Principal {

  private final String username;

  public MockPrincipal(String username) {
    this.username = username;
  }

  @Override
  public String getName() {
    return username;
  }
}
