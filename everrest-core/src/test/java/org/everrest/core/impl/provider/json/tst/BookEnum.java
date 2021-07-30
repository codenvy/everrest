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
package org.everrest.core.impl.provider.json.tst;

import static org.everrest.core.impl.provider.json.tst.Book.createCSharpBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJavaScriptBook;
import static org.everrest.core.impl.provider.json.tst.Book.createJunitBook;

public enum BookEnum {
    JUNIT_IN_ACTION(createJunitBook()),
    ADVANCED_JAVA_SCRIPT(createJavaScriptBook()),
    BEGINNING_C(createCSharpBook());

    private final Book book;

    BookEnum(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return book.toString();
    }
}
