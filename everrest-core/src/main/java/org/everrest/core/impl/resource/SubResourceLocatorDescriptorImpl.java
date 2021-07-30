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
package org.everrest.core.impl.resource;

import com.google.common.base.MoreObjects;

import org.everrest.core.Parameter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.uri.UriPattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class SubResourceLocatorDescriptorImpl implements SubResourceLocatorDescriptor {

    /** See {@link PathValue}. */
    private final PathValue path;

    /** See {@link UriPattern}. */
    private final UriPattern uriPattern;

    /** See {@link Method}. */
    private final Method method;

    /**
     * Parent resource for this method resource, in other words class which contains this method.
     */
    private final ResourceDescriptor parentResource;

    /** List of method's parameters. See {@link Parameter} . */
    private final List<Parameter> parameters;

    private final Annotation[] additional;

    /**
     * Constructs new instance of {@link SubResourceLocatorDescriptor}.
     *
     * @param path
     *         See {@link PathValue}
     * @param method
     *         See {@link Method}
     * @param parameters
     *         list of method parameters. See {@link Parameter}
     * @param parentResource
     *         parent resource for this method
     * @param additional
     *         set of additional (not JAX-RS annotations)
     */
    SubResourceLocatorDescriptorImpl(PathValue path,
                                     Method method,
                                     List<Parameter> parameters,
                                     ResourceDescriptor parentResource,
                                     Annotation[] additional) {
        this.path = path;
        this.uriPattern = new UriPattern(path.getPath());
        this.method = method;
        this.parameters = parameters;
        this.parentResource = parentResource;
        this.additional = additional;
    }


    @Override
    public PathValue getPathValue() {
        return path;
    }


    @Override
    public UriPattern getUriPattern() {
        return uriPattern;
    }


    @Override
    public Method getMethod() {
        return method;
    }


    @Override
    public List<Parameter> getMethodParameters() {
        return parameters;
    }


    @Override
    public ResourceDescriptor getParentResource() {
        return parentResource;
    }


    @Override
    public Class<?> getResponseType() {
        return getMethod().getReturnType();
    }


    @Override
    public Annotation[] getAnnotations() {
        return additional;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("resource", parentResource.getObjectClass())
                          .add("path", path)
                          .add("returned type", getResponseType())
                          .toString();
    }
}
