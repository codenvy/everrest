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
package org.everrest.sample.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import org.everrest.guice.servlet.EverrestGuiceContextListener;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author andrew00x
 */
public class BookServiceBootstrap extends EverrestGuiceContextListener {
    @Override
    protected List<Module> getModules() {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new Module() {
            public void configure(Binder binder) {
                binder.bind(BookService.class);
                binder.bind(BookStorage.class);
                binder.bind(BookNotFoundExceptionMapper.class);
            }
        });
        return modules;
    }
}
