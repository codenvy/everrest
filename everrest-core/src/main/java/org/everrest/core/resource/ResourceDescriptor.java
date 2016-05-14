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
package org.everrest.core.resource;

import org.everrest.core.ObjectModel;
import org.everrest.core.impl.resource.PathValue;
import org.everrest.core.uri.UriPattern;

import java.util.List;
import java.util.Map;

/**
 * Describes Resource Class or Root Resource Class. Resource Class is any Java class that uses JAX-RS annotations to implement corresponding
 * Web resource.
 *
 * @author andrew00x
 */
public interface ResourceDescriptor extends ObjectModel {

    /** @return See {@link PathValue} */
    PathValue getPathValue();

    /**
     * @return resource methods
     * @see ResourceMethodDescriptor
     */
    Map<String, List<ResourceMethodDescriptor>> getResourceMethods();

    /**
     * @return sub-resource locators
     * @see SubResourceLocatorDescriptor
     */
    Map<UriPattern, SubResourceLocatorDescriptor> getSubResourceLocators();

    /**
     * @return sub-resource methods
     * @see SubResourceMethodDescriptor
     */
    Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> getSubResourceMethods();

    /** @return See {@link UriPattern} */
    UriPattern getUriPattern();

    /**
     * @return {@code true} if resource is root resource false otherwise. Root resource is class which has own {@link javax.ws.rs.Path}
     * annotation
     */
    boolean isRootResource();
}
