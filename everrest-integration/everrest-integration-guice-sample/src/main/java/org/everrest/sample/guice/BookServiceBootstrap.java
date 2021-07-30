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
package org.everrest.sample.guice;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.everrest.guice.servlet.EverrestGuiceContextListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andrew00x
 */
public class BookServiceBootstrap extends EverrestGuiceContextListener {
    @Override
    protected List<Module> getModules() {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(BookService.class);
                binder.bind(BookStorage.class);
                binder.bind(BookNotFoundExceptionMapper.class);
            }
        });
        return modules;
    }
}
