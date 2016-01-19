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

/**
 * Describe Resource Class or Root Resource Class. Resource Class is any Java
 * class that uses JAX-RS annotations to implement corresponding Web resource.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: AbstractResourceDescriptor.java 285 2009-10-15 16:21:30Z
 *          aparfonov $
 */
public interface AbstractResourceDescriptor extends ResourceDescriptor, ObjectModel {

    /** @return See {@link PathValue} */
    PathValue getPathValue();

    /**
     * @return resource methods
     * @see ResourceMethodDescriptor
     */
    ResourceMethodMap<ResourceMethodDescriptor> getResourceMethods();

    /**
     * @return sub-resource locators
     * @see SubResourceLocatorDescriptor
     */
    SubResourceLocatorMap getSubResourceLocators();

    /**
     * @return sub-resource methods
     * @see SubResourceMethodDescriptor
     */
    SubResourceMethodMap getSubResourceMethods();

    /** @return See {@link UriPattern} */
    UriPattern getUriPattern();

    /**
     * @return true if resource is root resource false otherwise. Root resource
     * is class which has own {@link javax.ws.rs.Path} annotation
     */
    boolean isRootResource();

}
