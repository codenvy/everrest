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
package org.everrest.sample.pico;

import org.everrest.pico.EverrestComposer;
import org.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BookApplicationComposer extends EverrestComposer {
    @Override
    protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext) {
        container.addComponent(BookNotFoundExceptionMapper.class);
        container.addComponent(BookStorage.class);
    }

    @Override
    protected void doComposeRequest(MutablePicoContainer container) {
        container.addComponent(BookService.class);
    }

    @Override
    protected void doComposeSession(MutablePicoContainer container) {
    }
}
