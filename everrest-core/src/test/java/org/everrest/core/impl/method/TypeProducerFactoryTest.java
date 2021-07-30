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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.everrest.core.method.TypeProducer;
import org.everrest.core.util.ParameterizedTypeImpl;
import org.junit.Before;
import org.junit.Test;

public class TypeProducerFactoryTest {
  private TypeProducerFactory typeProducerFactory;

  @Before
  public void setUp() throws Exception {
    typeProducerFactory = new TypeProducerFactory();
  }

  @Test
  public void createsProducerForListOfString() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            List.class, ParameterizedTypeImpl.newParameterizedType(List.class, String.class));
    assertEquals(CollectionStringProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForSetOfString() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            Set.class, ParameterizedTypeImpl.newParameterizedType(Set.class, String.class));
    assertEquals(CollectionStringProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForSortedSetOfString() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            SortedSet.class,
            ParameterizedTypeImpl.newParameterizedType(SortedSet.class, String.class));
    assertEquals(CollectionStringProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForListOfTypeThatHasStringConstructor() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            List.class,
            ParameterizedTypeImpl.newParameterizedType(List.class, StringConstructorClass.class));
    assertEquals(CollectionStringConstructorProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForSetOfTypeThatHasStringConstructor() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            Set.class,
            ParameterizedTypeImpl.newParameterizedType(Set.class, StringConstructorClass.class));
    assertEquals(CollectionStringConstructorProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForSortedSetOfTypeThatHasStringConstructor() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            SortedSet.class,
            ParameterizedTypeImpl.newParameterizedType(
                SortedSet.class, StringConstructorClass.class));
    assertEquals(CollectionStringConstructorProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForListOfTypeThatHasStringValueOfMethod() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            List.class,
            ParameterizedTypeImpl.newParameterizedType(List.class, ValueOfStringClass.class));
    assertEquals(CollectionStringValueOfProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForSetOfTypeThatHasStringValueOfMethod() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            Set.class,
            ParameterizedTypeImpl.newParameterizedType(Set.class, ValueOfStringClass.class));
    assertEquals(CollectionStringValueOfProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForSortedSetOfTypeThatHasStringValueOfMethod() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(
            SortedSet.class,
            ParameterizedTypeImpl.newParameterizedType(SortedSet.class, ValueOfStringClass.class));
    assertEquals(CollectionStringValueOfProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForPrimitiveType() throws Exception {
    TypeProducer typeProducer = typeProducerFactory.createTypeProducer(int.class, null);
    assertEquals(PrimitiveTypeProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForString() throws Exception {
    TypeProducer typeProducer = typeProducerFactory.createTypeProducer(String.class, null);
    assertEquals(StringProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForTypeThatHasStringConstructor() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(StringConstructorClass.class, null);
    assertEquals(StringConstructorProducer.class, typeProducer.getClass());
  }

  @Test
  public void createsProducerForTypeThatHasStringValueOfMethod() throws Exception {
    TypeProducer typeProducer =
        typeProducerFactory.createTypeProducer(ValueOfStringClass.class, null);
    assertEquals(StringValueOfProducer.class, typeProducer.getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionWhenNeitherStringConstructorNorStringValueOfMethodAvailable()
      throws Exception {
    typeProducerFactory.createTypeProducer(NoStringConstructorNoValueOfClass.class, null);
  }

  public static class StringConstructorClass {
    public StringConstructorClass(String value) {}
  }

  public static class ValueOfStringClass {
    public static ValueOfStringClass valueOf(String value) {
      return new ValueOfStringClass();
    }
  }

  public static class NoStringConstructorNoValueOfClass {}
}
