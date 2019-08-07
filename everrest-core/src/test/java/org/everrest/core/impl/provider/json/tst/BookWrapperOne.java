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
package org.everrest.core.impl.provider.json.tst;

public class BookWrapperOne {
    public static BookWrapperOne createBookWrapperOne(Book book) {
        BookWrapperOne bookWrapperOne = new BookWrapperOne();
        bookWrapperOne.setBook(book);
        return bookWrapperOne;
    }

    private Book book;

    public void setBook(Book b) {
        book = b;
    }

    public Book getBook() {
        return book;
    }
}
