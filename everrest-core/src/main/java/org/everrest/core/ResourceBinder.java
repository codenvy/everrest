/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core;

import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * Manages root resources.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public interface ResourceBinder {

    /**
     * Get list of all registered root resources. Returned list is the copy of original list. Any changes in this list
     * does not impact to original list.
     *
     * @return all registered root resources
     */
    List<ObjectFactory<AbstractResourceDescriptor>> getResources();

    /** @return number of bound resources */
    int getSize();

    /**
     * Register supplied class as per-request root resource if it has valid
     * JAX-RS annotations and no one resource with the same UriPattern already
     * registered.
     *
     * @param resourceClass
     *         class of candidate to be root resource
     * @param properties
     *         optional resource properties. It may contains additional
     *         info about resource, e.g. description of resource, its
     *         responsibility, etc. This info can be retrieved
     *         {@link ObjectModel#getProperties()}. This parameter may be
     *         <code>null</code>
     * @throws ResourcePublicationException
     *         if resource can't be published
     *         because to:
     *         <ul>
     *         <li>&#64javax.ws.rs.Path annotation is missing</li>
     *         <li>resource has not any method with JAX-RS annotations</li>
     *         <li>JAX-RS annotations are ambiguous or invalid</li>
     *         <li>resource with the sane {@link UriPattern} already
     *         registered</li>
     *         </ul>
     * @see ObjectModel#getProperties()
     * @see ObjectModel#getProperty(String)
     */
    void addResource(Class<?> resourceClass, MultivaluedMap<String, String> properties);

    /**
     * Register supplied Object as singleton root resource if it has valid JAX-RS
     * annotations and no one resource with the same UriPattern already
     * registered.
     *
     * @param resource
     *         candidate to be root resource
     * @param properties
     *         optional resource properties. It may contains additional
     *         info about resource, e.g. description of resource, its
     *         responsibility, etc. This info can be retrieved
     *         {@link ObjectModel#getProperties()}. This parameter may be
     *         <code>null</code>
     * @throws ResourcePublicationException
     *         if resource can't be published
     *         because to:
     *         <ul>
     *         <li>&#64javax.ws.rs.Path annotation is missing</li>
     *         <li>resource has not any method with JAX-RS annotations</li>
     *         <li>JAX-RS annotations are ambiguous or invalid</li>
     *         <li>resource with the sane {@link UriPattern} already
     *         registered</li>
     *         </ul>
     * @see ObjectModel#getProperties()
     * @see ObjectModel#getProperty(String)
     */
    void addResource(Object resource, MultivaluedMap<String, String> properties);

    /**
     * Register supplied root resource if no one resource with the same
     * UriPattern already registered.
     *
     * @param resourceFactory
     *         root resource
     * @throws ResourcePublicationException
     *         if resource can't be published
     *         because resource with the sane {@link UriPattern} already
     *         registered
     */
    void addResource(ObjectFactory<AbstractResourceDescriptor> resourceFactory);

    /**
     * Get root resource matched to <code>requestPath</code>.
     *
     * @param requestPath
     *         request path
     * @param parameterValues
     *         see {@link ApplicationContext#getParameterValues()}
     * @return root resource matched to <code>requestPath</code> or
     *         <code>null</code>
     */
    ObjectFactory<AbstractResourceDescriptor> getMatchedResource(String requestPath, List<String> parameterValues);

    /**
     * Remove root resource of supplied class from root resource collection.
     *
     * @param clazz
     *         root resource class
     * @return removed resource or <code>null</code> if resource of specified
     *         class not found
     */
    ObjectFactory<AbstractResourceDescriptor> removeResource(Class<?> clazz);

    /**
     * Remove root resource with specified UriTemplate from root resource
     * collection.
     *
     * @param path
     *         root resource path
     * @return removed resource or <code>null</code> if resource for specified
     *         template not found
     */
    ObjectFactory<AbstractResourceDescriptor> removeResource(String path);

}
