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
package org.everrest.sample.book;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class BookServiceBootstrap implements ServletContextListener {
  static final String BOOK_STORAGE_NAME = BookStorage.class.getName();

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();
    servletContext.removeAttribute(BOOK_STORAGE_NAME);
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();
    servletContext.setAttribute(BOOK_STORAGE_NAME, new BookStorage());
  }
}
