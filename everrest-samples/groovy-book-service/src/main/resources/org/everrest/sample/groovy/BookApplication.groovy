/**
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
