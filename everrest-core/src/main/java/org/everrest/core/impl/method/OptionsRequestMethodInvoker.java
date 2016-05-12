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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.wadl.WadlProcessor;
import org.everrest.core.wadl.research.Application;

import javax.ws.rs.core.Response;

public class OptionsRequestMethodInvoker implements MethodInvoker {
    private WadlProcessor wadlProcessor;

    public OptionsRequestMethodInvoker(WadlProcessor wadlProcessor) {
        this.wadlProcessor = wadlProcessor;
    }

    @Override
    public Object invokeMethod(Object resource, GenericResourceMethod genericResourceMethod, ApplicationContext context) {
        Application wadlApplication = wadlProcessor.process(genericResourceMethod.getParentResource(), context.getBaseUri());
        return Response.ok(wadlApplication, MediaTypeHelper.WADL_TYPE).build();
    }
}
