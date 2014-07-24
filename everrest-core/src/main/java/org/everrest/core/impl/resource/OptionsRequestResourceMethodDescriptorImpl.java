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
package org.everrest.core.impl.resource;

import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: OptionsRequestResourceMethodDescriptorImpl.java 285 2009-10-15
 *          16:21:30Z aparfonov $
 */
public final class OptionsRequestResourceMethodDescriptorImpl extends ResourceMethodDescriptorImpl {
    /**
     * @param method
     *         See {@link Method}
     * @param httpMethod
     *         HTTP request method designator
     * @param parameters
     *         list of method parameters. See {@link MethodParameter}
     * @param parentResource
     *         parent resource for this method
     * @param consumes
     *         list of media types which this method can consume
     * @param produces
     *         list of media types which this method can produce
     * @param additional
     *         additional annotations
     */
    public OptionsRequestResourceMethodDescriptorImpl(Method method,
                                                      String httpMethod,
                                                      List<MethodParameter> parameters,
                                                      AbstractResourceDescriptor parentResource,
                                                      List<MediaType> consumes,
                                                      List<MediaType> produces,
                                                      Annotation[] additional) {
        super(method, httpMethod, parameters, parentResource, consumes, produces, additional);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getResponseType() {
        return Response.class;
    }
}
