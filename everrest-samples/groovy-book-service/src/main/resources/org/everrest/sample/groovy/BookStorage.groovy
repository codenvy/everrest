/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.sample.groovy

import java.util.concurrent.ConcurrentHashMap

class BookStorage {
    private static int idCounter = 100

    synchronized String generateId() {
        idCounter++
        Integer.toString(idCounter)
    }

    private Map books = new ConcurrentHashMap()

    BookStorage() {
        init()
    }

    private void init() {
        putBook(new Book(title: 'JUnit in Action', author: 'Vincent Massol', pages: 386, price: 19.37))
    }

    Book getBook(String id) {
        books[id]
    }

    String putBook(Book book) {
        String id = book.getId()
        if (id == null || id.trim().length() == 0) {
            id = generateId()
            book.setId(id)
        }
        books[id] = book
        id
    }

    Collection<Book> getAll() {
        books.values()
    }

    int numberOfBooks() {
        books.size()
    }
}