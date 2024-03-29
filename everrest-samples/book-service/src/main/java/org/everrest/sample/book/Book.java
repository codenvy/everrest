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
package org.everrest.sample.book;

public class Book {
  private String title;
  private String author;
  private int pages;
  private double price;
  private String id;

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getPages() {
    return pages;
  }

  public void setPages(int pages) {
    this.pages = pages;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Book:{")
        .append("Author: ")
        .append(author)
        .append(" ")
        .append("Title: ")
        .append(title)
        .append(" ")
        .append("Pages: ")
        .append(pages)
        .append(" ")
        .append("Price: ")
        .append(price)
        .append(" ")
        .append("ID: ")
        .append(id)
        .append("} ");
    return sb.toString();
  }
}
