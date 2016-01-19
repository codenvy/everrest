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
package org.everrest.sample.book;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BookStorage {
    private static int idCounter = 100;

    public synchronized String generateId() {
        idCounter++;
        return Integer.toString(idCounter);
    }

    private Map<String, Book> books = new ConcurrentHashMap<String, Book>();

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
