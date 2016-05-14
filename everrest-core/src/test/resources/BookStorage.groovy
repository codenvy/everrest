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