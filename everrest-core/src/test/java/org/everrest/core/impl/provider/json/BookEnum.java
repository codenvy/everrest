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
package org.everrest.core.impl.provider.json;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public enum BookEnum {
    JUNIT_IN_ACTION(new Book("Vincent Masson", "JUnit in Action", 19.37, 93011099534534L, 386, true, false)), //
    BEGINNING_C(new Book("Christian Gross", "Beginning C# 2008 from novice to professional", 23.56, 9781590598696L, 511,
                         false, false));
    private final Book book;

    private BookEnum(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return book.toString();
    }
}
