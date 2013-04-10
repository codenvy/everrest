/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.everrest.core.impl.provider.json;

import junit.framework.TestCase;

/**
 * @author <a href="andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public abstract class JsonTest extends TestCase {

    protected Book junitBook;

    protected Book csharpBook;

    protected Book javaScriptBook;

    protected void setUp() throws Exception {
        super.setUp();

        junitBook = new Book();
        junitBook.setAuthor("Vincent Masson");
        junitBook.setTitle("JUnit in Action");
        junitBook.setPages(386);
        junitBook.setPrice(19.37);
        junitBook.setIsdn(93011099534534L);
        junitBook.setAvailability(false);
        junitBook.setDelivery(false);

        csharpBook = new Book();
        csharpBook.setAuthor("Christian Gross");
        csharpBook.setTitle("Beginning C# 2008 from novice to professional");
        csharpBook.setPages(511);
        csharpBook.setPrice(23.56);
        csharpBook.setIsdn(9781590598696L);
        csharpBook.setAvailability(false);
        csharpBook.setDelivery(false);

        javaScriptBook = new Book();
        javaScriptBook.setAuthor("Chuck Easttom");
        javaScriptBook.setTitle("Advanced JavaScript. Third Edition");
        javaScriptBook.setPages(617);
        javaScriptBook.setPrice(25.99);
        javaScriptBook.setIsdn(9781598220339L);
        javaScriptBook.setAvailability(false);
        javaScriptBook.setDelivery(false);
    }

}
