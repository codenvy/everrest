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
public class BookBean {

    String author

    String title

    double price

    long isdn

    int pages

    boolean availability

    boolean delivery

    String toString() {
        StringBuffer sb = new StringBuffer()
        sb.append("Book:{").append("Author: ").append(author).append(" ").append("Title: ").append(title).append(" ")
                .append("Pages: ").append(pages).append(" ").append("Price: ").append(price).append(" ").append("ISDN: ")
                .append(isdn).append("Availability: ").append(availability).append(" ").append("Delivery: ").append(delivery)
                .append(" ").append("} ")
        sb.toString()
    }

    boolean equals(Object other) {
        return other != null && other instanceof BookBean && other.author == author && other.title == title && other.isdn == isdn && other.pages == pages && other.price == price && other.availability == availability && other.delivery == delivery
    }
}