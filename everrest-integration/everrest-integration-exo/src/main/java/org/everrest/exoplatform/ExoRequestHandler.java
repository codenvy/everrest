/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.exoplatform;

import org.everrest.core.DependencySupplier;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.exoplatform.container.xml.InitParams;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class ExoRequestHandler extends RequestHandlerImpl {
    public ExoRequestHandler(RequestDispatcher dispatcher, DependencySupplier dependencySupplier, InitParams initParams) {
        super(dispatcher, dependencySupplier, EverrestConfigurationHelper.createEverrestConfiguration(initParams));
    }

    public ExoRequestHandler(RequestDispatcher dispatcher, DependencySupplier dependencySupplier) {
        this(dispatcher, dependencySupplier, null);
    }
}
