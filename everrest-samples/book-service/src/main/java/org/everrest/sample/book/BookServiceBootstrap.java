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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BookServiceBootstrap implements ServletContextListener {
    static final String BOOK_STORAGE_NAME = BookStorage.class.getName();

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();
        sctx.removeAttribute(BOOK_STORAGE_NAME);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();
        sctx.setAttribute(BOOK_STORAGE_NAME, new BookStorage());
    }
}
