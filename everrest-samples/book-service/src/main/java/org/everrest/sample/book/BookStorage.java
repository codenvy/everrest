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
package org.everrest.sample.book;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookStorage {
    private static int idCounter = 100;

    public synchronized String generateId() {
        idCounter++;
        return Integer.toString(idCounter);
    }

    private Map<String, Book> books = new ConcurrentHashMap<>();

    public BookStorage() {
        init();
    }

    private void init() {
        Book book = new Book();
        book.setTitle("JUnit in Action");
        book.setAuthor("Vincent Massol");
        book.setPages(386);
        book.setPrice(19.37);
        putBook(book);
    }

    public Book getBook(String id) {
        return books.get(id);
    }

    public String putBook(Book book) {
        String id = book.getId();
        if (id == null || id.trim().length() == 0) {
            id = generateId();
            book.setId(id);
        }
        books.put(id, book);
        return id;
    }

    public Collection<Book> getAll() {
        return books.values();
    }

    public int numberOfBooks() {
        return books.size();
    }
}
