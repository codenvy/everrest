/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.everrest.sample.book;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class BookStorage {
    private static int idCounter = 100;

    public synchronized String generateId() {
        idCounter++;
        return Integer.toString(idCounter);
    }

    private final Map<String, Book> books = new ConcurrentHashMap<String, Book>();

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
