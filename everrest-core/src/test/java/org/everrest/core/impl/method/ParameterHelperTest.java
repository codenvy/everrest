/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.method;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class ParameterHelperTest extends TestCase {

    public void testString() throws Exception {
        Method method = getClass().getMethod("m1", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(String.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(String.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(String.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testByte() throws Exception {
        Method method = getClass().getMethod("m2", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Byte.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Byte.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Byte.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testShort() throws Exception {
        Method method = getClass().getMethod("m3", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Short.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Short.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Short.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testInteger() throws Exception {
        Method method = getClass().getMethod("m4", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Integer.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Integer.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Integer.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testLong() throws Exception {
        Method method = getClass().getMethod("m5", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Long.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Long.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Long.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testFloat() throws Exception {
        Method method = getClass().getMethod("m6", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Float.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Float.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Float.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testDouble() throws Exception {
        Method method = getClass().getMethod("m7", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Double.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Double.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Double.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testBoolean() throws Exception {
        Method method = getClass().getMethod("m8", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(Boolean.class, ParameterHelper.getGenericType(types[0]));
        assertEquals(Boolean.class, ParameterHelper.getGenericType(types[1]));
        assertEquals(Boolean.class, ParameterHelper.getGenericType(types[2]));
    }

    public void testNull() throws Exception {
        Method method = getClass().getMethod("m9", List.class, Set.class, SortedSet.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(null, ParameterHelper.getGenericType(types[0]));
        assertEquals(null, ParameterHelper.getGenericType(types[1]));
        assertEquals(null, ParameterHelper.getGenericType(types[2]));
    }

    ////////////////////////////////
    public void m1(List<String> l, Set<String> s, SortedSet<String> ss) {
        // used for test
    }

    public void m2(List<Byte> l, Set<Byte> s, SortedSet<Byte> ss) {
        // used for test
    }

    public void m3(List<Short> l, Set<Short> s, SortedSet<Short> ss) {
        // used for test
    }

    public void m4(List<Integer> l, Set<Integer> s, SortedSet<Integer> ss) {
        // used for test
    }

    public void m5(List<Long> l, Set<Long> s, SortedSet<Long> ss) {
        // used for test
    }

    public void m6(List<Float> l, Set<Float> s, SortedSet<Float> ss) {
        // used for test
    }

    public void m7(List<Double> l, Set<Double> s, SortedSet<Double> ss) {
        // used for test
    }

    public void m8(List<Boolean> l, Set<Boolean> s, SortedSet<Boolean> ss) {
        // used for test
    }

    public void m9(List l, Set s, SortedSet ss) {
        // used for test
    }

}
