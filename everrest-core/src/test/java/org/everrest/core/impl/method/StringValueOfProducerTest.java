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
package org.everrest.core.impl.method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.ws.rs.core.MultivaluedMap;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;

public class StringValueOfProducerTest {
  private StringValueOfProducer producer;

  @Before
  public void setUp() throws Exception {
    producer = new StringValueOfProducer(Integer.class.getMethod("valueOf", String.class));
  }

  @Test
  public void createsInstanceAndUsesSingleValueFromMap() throws Exception {
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.putSingle("number", "2147483647");

    Object result = producer.createValue("number", values, null);
    assertEquals(new Integer("2147483647"), result);
  }

  @Test
  public void createsInstanceAndUsesFirstValueFromMap() throws Exception {
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("number", values, null);
    assertEquals(new Integer("2147483647"), result);
  }

  @Test
  public void createsInstanceAndUsesDefault() throws Exception {
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();

    Object result = producer.createValue("number", values, "-777");
    assertEquals(new Integer("-777"), result);
  }

  @Test
  public void returnsNullWhenMapDoesNotContainRequiredValueAndDefaultValueIsNull()
      throws Exception {
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();

    Object result = producer.createValue("number", values, null);
    assertNull(result);
  }
}
