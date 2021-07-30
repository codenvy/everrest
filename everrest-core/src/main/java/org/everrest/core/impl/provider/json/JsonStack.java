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
package org.everrest.core.impl.provider.json;

import java.util.ArrayList;
import java.util.List;

class JsonStack<T> {
    private final List<T> elements;

    JsonStack() {
        elements = new ArrayList<>();
    }

    boolean isEmpty() {
        return elements.isEmpty();
    }

    T peek() {
        return isEmpty() ? null : elements.get(elements.size() - 1);
    }

    T pop() {
        return isEmpty() ? null : elements.remove(elements.size() - 1);
    }

    void push(T token) {
        elements.add(token);
    }

    void clear() {
        elements.clear();
    }
}
