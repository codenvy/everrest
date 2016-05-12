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
package org.everrest.core.impl.provider.json.tst;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Book {
    public static Book createCSharpBook() {
        Book cSharpBook = new Book();
        cSharpBook.setAuthor("Christian Gross");
        cSharpBook.setTitle("Beginning C# 2008 from novice to professional");
        cSharpBook.setPages(511);
        cSharpBook.setPrice(23.56);
        cSharpBook.setIsdn(9781590598696L);
        cSharpBook.setAvailability(false);
        cSharpBook.setDelivery(false);
        return cSharpBook;
    }

    public static Book createJavaScriptBook() {
        Book javaScriptBook = new Book();
        javaScriptBook.setAuthor("Chuck Easttom");
        javaScriptBook.setTitle("Advanced JavaScript. Third Edition");
        javaScriptBook.setPages(617);
        javaScriptBook.setPrice(25.99);
        javaScriptBook.setIsdn(9781598220339L);
        javaScriptBook.setAvailability(false);
        javaScriptBook.setDelivery(false);
        return javaScriptBook;
    }

    public static Book createJunitBook() {
        Book junitBook = new Book();
        junitBook.setAuthor("Vincent Massol");
        junitBook.setTitle("JUnit in Action");
        junitBook.setPages(386);
        junitBook.setPrice(19.37);
        junitBook.setIsdn(93011099534534L);
        junitBook.setAvailability(false);
        junitBook.setDelivery(false);
        return junitBook;
    }

    private String  author;
    private String  title;
    private double  price;
    private long    isdn;
    private int     pages;
    private boolean availability;
    private boolean delivery;

    public Book() {
    }

    public Book(String author, String title, double price, long isdn, int pages, boolean availability, boolean delivery) {
        this.author = author;
        this.title = title;
        this.price = price;
        this.isdn = isdn;
        this.pages = pages;
        this.availability = availability;
        this.delivery = delivery;
    }

    public void setAuthor(String s) {
        author = s;
    }

    public void setTitle(String s) {
        title = s;
    }

    public void setPrice(double d) {
        price = d;
    }

    public void setIsdn(long i) {
        isdn = i;
    }

    public void setPages(int i) {
        pages = i;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public long getIsdn() {
        return isdn;
    }

    public int getPages() {
        return pages;
    }

    public boolean getAvailability() {
        return availability;
    }

    public boolean getDelivery() {
        return delivery;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                          .add("Author", author)
                          .add("Title", title)
                          .add("Pages", pages)
                          .add("Price", price)
                          .add("ISDN", isdn)
                          .add("Availability", availability)
                          .add("Delivery", delivery)
                          .toString();
    }

    public int hashCode() {
        int hash = 8;
        hash = hash * 31 + (author != null ? author.hashCode() : 0);
        hash = hash * 31 + (title != null ? title.hashCode() : 0);
        hash = (int)(hash * 31 + isdn);
        hash = hash * 31 + pages;
        hash = (int)(hash * 31 + Double.doubleToLongBits(pages));
        hash = (int)(hash * 31 + Double.doubleToLongBits(pages));
        hash = hash + (availability ? 1 : 0);
        hash = hash + (delivery ? 1 : 0);
        return hash;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if ((other instanceof Book)) {
            Book book = (Book)other;
            return Objects.equals(author, book.getAuthor())
                   && Objects.equals(title, book.getTitle())
                   && (isdn == book.getIsdn())
                   && (pages == book.getPages())
                   && (price == book.getPrice())
                   && (availability == book.getAvailability())
                   && (delivery == book.getDelivery());
        }
        return false;
    }
}
