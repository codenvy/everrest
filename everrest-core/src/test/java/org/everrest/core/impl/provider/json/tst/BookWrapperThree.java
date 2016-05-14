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
package org.everrest.core.impl.provider.json.tst;

public class BookWrapperThree {
    public static BookWrapperThree createBookWrapperThree(Book book) {
        BookWrapperThree bookWrapperThree = new BookWrapperThree();
        bookWrapperThree.setBookWrapper(BookWrapperTwo.createBookWrapperTwo(book));
        return bookWrapperThree;
    }

    private BookWrapperTwo bookWrapper;

    public void setBookWrapper(BookWrapperTwo bw) {
        bookWrapper = bw;
    }

    public BookWrapperTwo getBookWrapper() {
        return bookWrapper;
    }
}
