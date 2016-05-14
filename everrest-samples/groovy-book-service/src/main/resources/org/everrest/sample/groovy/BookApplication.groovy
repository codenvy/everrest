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
package org.everrest.sample.groovy

class BookApplication extends javax.ws.rs.core.Application {
    def bookService
    def bookNotFoundExceptionMapper

    BookApplication() {
        bookService = new BookService(bookStorage: new BookStorage())
        bookNotFoundExceptionMapper = new BookNotFoundExceptionMapper()
    }

    Set<Object> getSingletons() {
        new HashSet<Object>([bookService, this.bookNotFoundExceptionMapper])
    }
}
