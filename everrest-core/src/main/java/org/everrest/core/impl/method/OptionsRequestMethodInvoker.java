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
package org.everrest.core.impl.method;

import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericMethodResource;
import org.everrest.core.wadl.WadlProcessor;
import org.everrest.core.wadl.research.Application;

import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: OptionsRequestMethodInvoker.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class OptionsRequestMethodInvoker implements MethodInvoker {

    @Override
    public Object invokeMethod(Object resource, GenericMethodResource genericMethodResource, ApplicationContext context) {
        Application wadlApplication =
                new WadlProcessor().process(genericMethodResource.getParentResource(), context.getBaseUri());
        return Response.ok(wadlApplication, MediaTypeHelper.WADL_TYPE).build();
    }
}
