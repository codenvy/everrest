/**
 * Copyright (c) 2012-2022 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
public class BookStorage {
    static BookStorage createBookStorage() {
        return new BookStorage([BookBean.createJunitBook(), BookBean.createCSharpBook(), BookBean.createJavaScriptBook()])
    }

    List<BookBean> books = new ArrayList<BookBean>()

    BookStorage() {
    }

    BookStorage(List<BookBean> books) {
        this.books = books
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BookStorage that = (BookStorage) o

        if (books != that.books) return false

        return true
    }

    int hashCode() {
        return (books != null ? books.hashCode() : 0)
    }
}