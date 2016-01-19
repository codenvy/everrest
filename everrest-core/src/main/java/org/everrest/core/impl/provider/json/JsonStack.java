/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.provider.json;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
class JsonStack<T> {

    private final List<T> elements;

    JsonStack() {
        elements = new ArrayList<T>(16);
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
