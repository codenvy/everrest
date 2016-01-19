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
package org.everrest.sample.book;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BookApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> cls = new HashSet<Class<?>>(1);
        cls.add(BookService.class);
        return cls;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objs = new HashSet<Object>(1);
        objs.add(new BookNotFoundExceptionMapper());
        return objs;
    }
}
