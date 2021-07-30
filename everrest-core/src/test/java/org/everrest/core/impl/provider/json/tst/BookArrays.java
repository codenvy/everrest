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

public class BookArrays {
    public static BookArrays createBookArrays() {
        BookArrays bookArrays = new BookArrays();
        bookArrays.setBooks(new Book[]{createJunitBook(), createJavaScriptBook()});
        bookArrays.setBooksBooks(new Book[][]{
                {createJunitBook(), createJavaScriptBook()},
                {createCSharpBook(), createJunitBook()}
        });
        bookArrays.setBooksBooksBooks(new Book[][][]{
                {
                        {createJunitBook(), createJavaScriptBook()},
                        {createCSharpBook(), createJunitBook()}
                }
        });
        return bookArrays;
    }

    private Book[] books;
    private Book[][] booksBooks;
    private Book[][][] booksBooksBooks;

    public Book[] getBooks() {
        return books;
    }

    public void setBooks(Book[] books) {
        this.books = books;
    }

    public Book[][] getBooksBooks() {
        return booksBooks;
    }

    public void setBooksBooks(Book[][] booksBooks) {
        this.booksBooks = booksBooks;
    }

    public Book[][][] getBooksBooksBooks() {
        return booksBooksBooks;
    }

    public void setBooksBooksBooks(Book[][][] booksBooksBooks) {
        this.booksBooksBooks = booksBooksBooks;
    }
}
