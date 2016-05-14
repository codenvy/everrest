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
package org.everrest.core.impl.resource;

import org.everrest.core.Parameter;
import org.everrest.core.resource.ResourceDescriptor;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Resource method that produces response for OPTIONS request. This resource method is just placeholder to mark fact that OPTIONS request is supported
 * and it is not linked with any real method of class.
 */
public final class OptionsRequestResourceMethodDescriptorImpl extends ResourceMethodDescriptorImpl {
    /**
     * @param httpMethod
     *         HTTP request method designator
     * @param parameters
     *         list of method parameters. See {@link Parameter}
     * @param parentResource
     *         parent resource for this method
     * @param consumes
     *         list of media types which this method can consume
     * @param produces
     *         list of media types which this method can produce
     * @param additional
     *         additional annotations
     */
    public OptionsRequestResourceMethodDescriptorImpl(String httpMethod,
                                                      List<Parameter> parameters,
                                                      ResourceDescriptor parentResource,
                                                      List<MediaType> consumes,
                                                      List<MediaType> produces,
                                                      Annotation[] additional) {
        super(null, httpMethod, parameters, parentResource, consumes, produces, additional);
    }

    @Override
    public Class<?> getResponseType() {
        return Response.class;
    }
}
