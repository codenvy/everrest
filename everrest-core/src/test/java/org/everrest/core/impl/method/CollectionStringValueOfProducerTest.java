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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;

public class CollectionStringValueOfProducerTest {
  @Test
  public void createsListAndUsesValuesFromMap() throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            List.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("number", values, null);

    assertEquals(newArrayList(2147483647, 746384741), result);
  }

  @Test
  public void createsListAndUsesDefaultValue() throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            List.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("__number", values, "-2147483647");

    assertEquals(newArrayList(-2147483647), result);
  }

  @Test
  public void createsSetAndUsesValuesFromMap() throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            Set.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("number", values, null);

    assertEquals(newHashSet(2147483647, 746384741), result);
  }

  @Test
  public void createsSetAndUsesDefaultValue() throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            Set.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("__number", values, "-2147483647");

    assertEquals(newHashSet(-2147483647), result);
  }

  @Test
  public void createsSortedSetAndUsesValuesFromMap() throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            SortedSet.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("number", values, null);

    assertEquals(newTreeSet(newArrayList(2147483647, 746384741)), result);
  }

  @Test
  public void createsSortedSetAndUsesDefaultValue() throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            SortedSet.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();
    values.addAll("number", "2147483647", "746384741");

    Object result = producer.createValue("__number", values, "-2147483647");

    assertEquals(newTreeSet(newArrayList(-2147483647)), result);
  }

  @Test
  public void returnsNullWhenMapDoesNotContainRequiredValueAndDefaultValueIsNull()
      throws Exception {
    CollectionStringValueOfProducer producer =
        new CollectionStringValueOfProducer(
            List.class, Integer.class.getMethod("valueOf", String.class));
    MultivaluedMap<String, String> values = new MultivaluedMapImpl();

    Object result = producer.createValue("number", values, null);
    assertNull(result);
  }
}
