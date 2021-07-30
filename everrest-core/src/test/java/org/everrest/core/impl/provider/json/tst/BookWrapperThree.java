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
