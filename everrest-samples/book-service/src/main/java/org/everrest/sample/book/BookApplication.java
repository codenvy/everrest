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
package org.everrest.sample.book;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class BookApplication extends Application {

    private final Set<Class<?>> classes;
    private final Set<Object> singletons;

    public BookApplication() {
        classes = new HashSet<>(1);
        singletons = new HashSet<>(1);
        classes.add(BookService.class);
        singletons.add(new BookNotFoundExceptionMapper());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
