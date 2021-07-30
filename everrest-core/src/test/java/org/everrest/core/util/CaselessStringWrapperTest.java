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
package org.everrest.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CaselessStringWrapperTest {
  private String string = "To Be Or Not To Be";
  private CaselessStringWrapper caselessString = new CaselessStringWrapper(string);

  @Test
  public void keepsOriginalStringAsIs() {
    assertEquals(string, caselessString.getString());
  }

  @Test
  public void testEquals() throws Exception {
    assertTrue(caselessString.equals(new CaselessStringWrapper(string.toUpperCase())));
  }

  @Test
  public void testHashCode() throws Exception {
    assertEquals(
        caselessString.hashCode(), new CaselessStringWrapper(string.toUpperCase()).hashCode());
  }
}
