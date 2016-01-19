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

import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.uri.UriPattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: SubResourceLocatorDescriptorImpl.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class SubResourceLocatorDescriptorImpl implements SubResourceLocatorDescriptor {

    /** See {@link PathValue}. */
    private final PathValue path;

    /** See {@link UriPattern}. */
    private final UriPattern uriPattern;

    /** See {@link Method}. */
    private final Method method;

    /**
     * Parent resource for this method resource, in other words class which
     * contains this method.
     */
    private final AbstractResourceDescriptor parentResource;

    /** List of method's parameters. See {@link MethodParameter} . */
    private final List<MethodParameter> parameters;

    private final Annotation[] additional;

    /**
     * Constructs new instance of {@link SubResourceLocatorDescriptor}.
     *
     * @param path
     *         See {@link PathValue}
     * @param method
     *         See {@link Method}
     * @param parameters
     *         list of method parameters. See {@link MethodParameter}
     * @param parentResource
     *         parent resource for this method
     * @param additional
     *         set of additional (not JAX-RS annotations)
     */
    SubResourceLocatorDescriptorImpl(PathValue path,
                                     Method method,
                                     List<MethodParameter> parameters,
                                     AbstractResourceDescriptor parentResource,
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
    public void accept(ResourceDescriptorVisitor visitor) {
        visitor.visitSubResourceLocatorDescriptor(this);
    }


    @Override
    public Method getMethod() {
        return method;
    }


    @Override
    public List<MethodParameter> getMethodParameters() {
        return parameters;
    }


    @Override
    public AbstractResourceDescriptor getParentResource() {
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
        StringBuilder sb = new StringBuilder("[ SubResourceMethodDescriptorImpl: ");
        sb.append("resource: ");
        sb.append(getParentResource());
        sb.append("; path: ");
        sb.append(getPathValue());
        sb.append("; return type: ");
        sb.append(getResponseType());
        sb.append(" ]");
        return sb.toString();
    }
}
