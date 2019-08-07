/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtils {

    /**
     * Get static {@link Method} with single string argument and name 'valueOf' for supplied class.
     *
     * @param clazz
     *         class for discovering to have public static method with name 'valueOf' and single string argument
     * @return valueOf method or {@code null} if class has not it
     */
    public static Method getStringValueOfMethod(Class<?> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("valueOf", String.class);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get constructor with single string argument for supplied class.
     *
     * @param clazz
     *         class for discovering to have constructor with single string argument
     * @return constructor or {@code null} if class has not constructor with single string argument
     */
    public static Constructor<?> getStringConstructor(Class<?> clazz) {
        try {
            return clazz.getConstructor(String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private ReflectionUtils() {
    }
}
