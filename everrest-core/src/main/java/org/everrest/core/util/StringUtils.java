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

public class StringUtils {
    public static boolean charAtIs(CharSequence str, int atPosition, char expectedChar) {
        return atPosition >= 0 && atPosition < str.length()
               && str.charAt(atPosition) == expectedChar;
    }

    public static boolean charAtIsNot(CharSequence str, int atPosition, char notExpectedChar) {
        return !charAtIs(str, atPosition, notExpectedChar);
    }

    public static boolean contains(String str, char containsIt) {
        return str.indexOf(containsIt) >= 0;
    }

    public static boolean doesNotContain(String str, char containsIt) {
        return !contains(str, containsIt);
    }

    public static int scan(CharSequence str, char findThisChar) {
        return scan(str, 0, findThisChar, str.length());
    }

    public static int scan(CharSequence str, int begin, char findThisChar) {
        return scan(str, begin, findThisChar, str.length());
    }

    public static int scan(CharSequence str, int begin, char findThisChar, int end) {
        for (int i = begin; i < end; i++) {
            if (charAtIs(str, i, findThisChar)) {
                return i;
            }
        }
        return end;
    }
}
