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

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.everrest.core.impl.provider.json.tst.BookEnum.ADVANCED_JAVA_SCRIPT;
import static org.everrest.core.impl.provider.json.tst.BookEnum.BEGINNING_C;
import static org.everrest.core.impl.provider.json.tst.BookEnum.JUNIT_IN_ACTION;

public class BeanWithEnums {
    public static BeanWithEnums createBeanWithEnums() {
        BeanWithEnums beanWithEnums = new BeanWithEnums();
        beanWithEnums.setBook(ADVANCED_JAVA_SCRIPT);
        beanWithEnums.setBookArray(new BookEnum[]{BEGINNING_C, JUNIT_IN_ACTION});
        beanWithEnums.setBookList(newArrayList(BEGINNING_C, JUNIT_IN_ACTION));
        return beanWithEnums;
    }

    private BookEnum        book;
    private BookEnum[]      bookArray;
    private List<BookEnum>  bookList;

    public BookEnum getBook() {
        return book;
    }

    public void setBook(BookEnum book) {
        this.book = book;
    }

    public BookEnum[] getBookArray() {
        return bookArray;
    }

    public void setBookArray(BookEnum[] bookArray) {
        this.bookArray = bookArray;
    }

    public List<BookEnum> getBookList() {
        return bookList;
    }

    public void setBookList(List<BookEnum> bookList) {
        this.bookList = bookList;
    }
}