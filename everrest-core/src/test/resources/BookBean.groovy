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
public class BookBean {
    static BookBean createJunitBook() {
        new BookBean(
                author: 'Vincent Massol',
                title: 'JUnit in Action',
                pages: 386,
                price: 19.37,
                isdn: 93011099534534L,
                availability: true,
                delivery: true)
    }

    static BookBean createCSharpBook() {
        new BookBean(
                author: 'Christian Gross',
                title: 'Beginning C# 2008 from novice to professional',
                pages: 511,
                price: 23.56,
                isdn: 9781590598696L,
                availability: true,
                delivery: true)
    }

    static BookBean createJavaScriptBook() {
        new BookBean(
                author: 'Chuck Easttom',
                title: 'Advanced JavaScript. Third Edition',
                pages: 617,
                price: 25.99,
                isdn: 9781598220339L,
                availability: false,
                delivery: false)
    }

    String author
    String title
    double price
    long isdn
    int pages
    boolean availability
    boolean delivery

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BookBean bookBean = (BookBean) o

        if (availability != bookBean.availability) return false
        if (delivery != bookBean.delivery) return false
        if (isdn != bookBean.isdn) return false
        if (pages != bookBean.pages) return false
        if (Double.compare(bookBean.price, price) != 0) return false
        if (author != bookBean.author) return false
        if (title != bookBean.title) return false

        return true
    }

    int hashCode() {
        int result
        long temp
        result = (author != null ? author.hashCode() : 0)
        result = 31 * result + (title != null ? title.hashCode() : 0)
        temp = price != +0.0d ? Double.doubleToLongBits(price) : 0L
        result = 31 * result + (int) (temp ^ (temp >>> 32))
        result = 31 * result + (int) (isdn ^ (isdn >>> 32))
        result = 31 * result + pages
        result = 31 * result + (availability ? 1 : 0)
        result = 31 * result + (delivery ? 1 : 0)
        return result
    }
}