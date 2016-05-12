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

import static org.everrest.core.impl.provider.json.tst.BookWrapperOne.createBookWrapperOne;

public class BookWrapperTwo {
    public static BookWrapperTwo createBookWrapperTwo(Book book) {
        BookWrapperTwo bookWrapperTwo = new BookWrapperTwo();
        bookWrapperTwo.setBookWrapper(createBookWrapperOne(book));
        return bookWrapperTwo;
    }

    private BookWrapperOne bookWrapper;

    public void setBookWrapper(BookWrapperOne bw) {
        bookWrapper = bw;
    }

    public BookWrapperOne getBookWrapper() {
        return bookWrapper;
    }
}
