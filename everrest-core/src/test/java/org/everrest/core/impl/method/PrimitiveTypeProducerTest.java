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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jakarta.ws.rs.core.MultivaluedMap;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class PrimitiveTypeProducerTest {

  @DataProvider
  public static Object[][] testData() {
    return new Object[][] {
      {Boolean.TYPE, singletonMultiValuedMap("true", "false"), null, true},
      {Boolean.TYPE, singletonMultiValuedMap("foo", "false"), null, false},
      {Boolean.TYPE, singletonMultiValuedMap("true"), null, true},
      {Boolean.TYPE, singletonMultiValuedMap(), "true", true},
      {Boolean.TYPE, singletonMultiValuedMap(), null, false},
      {Byte.TYPE, singletonMultiValuedMap("55", "33"), null, (byte) 55},
      {Byte.TYPE, singletonMultiValuedMap("33"), null, (byte) 33},
      {Byte.TYPE, singletonMultiValuedMap(), "55", (byte) 55},
      {Byte.TYPE, singletonMultiValuedMap(), null, (byte) 0},
      {Short.TYPE, singletonMultiValuedMap("555", "333"), null, (short) 555},
      {Short.TYPE, singletonMultiValuedMap("333"), null, (short) 333},
      {Short.TYPE, singletonMultiValuedMap(), "555", (short) 555},
      {Short.TYPE, singletonMultiValuedMap(), null, (short) 0},
      {Integer.TYPE, singletonMultiValuedMap("55555", "33333"), null, 55555},
      {Integer.TYPE, singletonMultiValuedMap("33333"), null, 33333},
      {Integer.TYPE, singletonMultiValuedMap(), "55555", 55555},
      {Integer.TYPE, singletonMultiValuedMap(), null, 0},
      {Long.TYPE, singletonMultiValuedMap("55555555555", "33333333333"), null, 55555555555L},
      {Long.TYPE, singletonMultiValuedMap("33333333333"), null, 33333333333L},
      {Long.TYPE, singletonMultiValuedMap(), "33333333333", 33333333333L},
      {Long.TYPE, singletonMultiValuedMap(), null, 0L},
      {Float.TYPE, singletonMultiValuedMap("5.5", "3.3"), null, 5.5F},
      {Float.TYPE, singletonMultiValuedMap("3.3"), null, 3.3F},
      {Float.TYPE, singletonMultiValuedMap(), "5.5", 5.5F},
      {Float.TYPE, singletonMultiValuedMap(), null, 0.0F},
      {Double.TYPE, singletonMultiValuedMap("5.5", "3.3"), null, 5.5D},
      {Double.TYPE, singletonMultiValuedMap("3.3"), null, 3.3D},
      {Double.TYPE, singletonMultiValuedMap(), "5.5", 5.5D},
      {Double.TYPE, singletonMultiValuedMap(), null, 0.0D}
    };
  }

  @UseDataProvider("testData")
  @Test
  public void createsPrimitiveValue(
      Class<?> primitiveTypeWrapper,
      MultivaluedMap<String, String> values,
      String defaultValue,
      Object expected)
      throws Exception {
    PrimitiveTypeProducer producer = new PrimitiveTypeProducer(primitiveTypeWrapper);
    Object result = producer.createValue("value", values, defaultValue);
    assertEquals(expected, result);
  }

  private static MultivaluedMap<String, String> singletonMultiValuedMap(String... values) {
    MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.addAll("value", values);
    return map;
  }
}
