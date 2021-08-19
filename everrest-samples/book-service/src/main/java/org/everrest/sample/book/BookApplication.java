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

import jakarta.ws.rs.core.Application;
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
