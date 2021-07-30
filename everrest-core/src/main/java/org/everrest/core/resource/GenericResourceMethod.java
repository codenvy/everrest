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
package org.everrest.core.resource;

import org.everrest.core.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Abstraction for method in resource, this essence is common for {@link ResourceMethodDescriptor}, {@link SubResourceMethodDescriptor},
 * {@link SubResourceLocatorDescriptor} .
 */
public interface GenericResourceMethod {

    /** @return {@link Method} */
    Method getMethod();

    /** @return List of method parameters */
    List<Parameter> getMethodParameters();

    /** @return parent resource descriptor */
    ResourceDescriptor getParentResource();

    /** @return Java type returned by method, see {@link #getMethod()} */
    Class<?> getResponseType();

    /**
     * Get set or additional (not JAX-RS specific) annotation. Set of annotations in implementation specific and it is not guaranteed this
     * method will return all annotations applied to the method.
     *
     * @return addition annotation
     */
    Annotation[] getAnnotations();
}
