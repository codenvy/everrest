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
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: SubResourceMethodDescriptorImpl.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public class SubResourceMethodDescriptorImpl implements SubResourceMethodDescriptor {

    /** See {@link PathValue}. */
    private final PathValue path;

    /** See {@link UriPattern}. */
    private final UriPattern uriPattern;

    /** This method will be invoked. */
    private final Method method;

    /** HTTP request method designator. */
    private final String httpMethod;

    /** List of method's parameters. See {@link MethodParameter} . */
    private final List<MethodParameter> parameters;

    /**
     * Parent resource for this method resource, in other words class which
     * contains this method.
     */
    private final AbstractResourceDescriptor parentResource;

    /**
     * List of media types which this method can consume. See
     * {@link javax.ws.rs.Consumes} .
     */
    private final List<MediaType> consumes;

    /**
     * List of media types which this method can produce. See
     * {@link javax.ws.rs.Produces} .
     */
    private final List<MediaType> produces;

    private final Annotation[] additional;

    /**
     * Constructs new instance of {@link SubResourceMethodDescriptor}.
     *
     * @param path
     *         See {@link PathValue}
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
     *         set of additional (not JAX-RS annotations)
     */
    SubResourceMethodDescriptorImpl(PathValue path,
                                    Method method,
                                    String httpMethod,
                                    List<MethodParameter> parameters,
                                    AbstractResourceDescriptor parentResource,
                                    List<MediaType> consumes,
                                    List<MediaType> produces,
                                    Annotation[] additional) {
        this.path = path;
        this.uriPattern = new UriPattern(path.getPath());
        this.method = method;
        this.httpMethod = httpMethod;
        this.parameters = parameters;
        this.parentResource = parentResource;
        this.consumes = consumes;
        this.produces = produces;
        this.additional = additional;
    }


    @Override
    public List<MediaType> consumes() {
        return consumes;
    }


    @Override
    public String getHttpMethod() {
        return httpMethod;
    }


    @Override
    public List<MediaType> produces() {
        return produces;
    }


    @Override
    public void accept(ResourceDescriptorVisitor visitor) {
        visitor.visitSubResourceMethodDescriptor(this);
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
        sb.append("; HTTP method: ");
        sb.append(getHttpMethod());
        sb.append("; produces media type: ");
        sb.append(produces());
        sb.append("; consumes media type: ");
        sb.append(consumes());
        sb.append("; return type: ");
        sb.append(getResponseType());
        sb.append(" ]");
        return sb.toString();
    }

}
