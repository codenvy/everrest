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
