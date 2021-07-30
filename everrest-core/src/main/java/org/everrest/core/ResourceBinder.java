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
package org.everrest.core;

import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * Manages root resources.
 *
 * @author andrew00x
 */
public interface ResourceBinder {

    /**
     * Get list of all registered root resources. Returned list is the copy of original list. Any changes in this list
     * do not impact to original list.
     *
     * @return all registered root resources
     */
    List<ObjectFactory<ResourceDescriptor>> getResources();

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
     * Register supplied class as per-request root resource if it has valid JAX-RS annotations and no one resource with the same
     * <code>uriPattern</code> already registered. Resource class doesn't need to be annotated with &#064Path annotation (but may be).
     * Anyway <code>uriPattern</code> parameter overwrites value of &#064Path annotation.
     *
     * @param uriPattern
     *         class of candidate to be root resource
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
     *         <li><code>uriPattern</code> is <code>null</code> or empty</li>
     *         <li>resource has not any method with JAX-RS annotations</li>
     *         <li>JAX-RS annotations are ambiguous or invalid</li>
     *         <li>resource with the sane {@link UriPattern} already
     *         registered</li>
     *         </ul>
     * @see ObjectModel#getProperties()
     * @see ObjectModel#getProperty(String)
     */
    void addResource(String uriPattern, Class<?> resourceClass, MultivaluedMap<String, String> properties);

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
     * Register supplied object as singleton root resource if it has valid JAX-RS annotations and no one resource with the same
     * <code>uriPattern</code> already registered. Resource class doesn't need to be annotated with &#064Path annotation (but may be).
     * Anyway <code>uriPattern</code> parameter overwrite value of &#064Path annotation.
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
     *         <li><code>uriPattern</code> is <code>null</code> or empty</li>
     *         <li>resource has not any method with JAX-RS annotations</li>
     *         <li>JAX-RS annotations are ambiguous or invalid</li>
     *         <li>resource with the sane {@link UriPattern} already
     *         registered</li>
     *         </ul>
     * @see ObjectModel#getProperties()
     * @see ObjectModel#getProperty(String)
     */
    void addResource(String uriPattern, Object resource, MultivaluedMap<String, String> properties);

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
    void addResource(ObjectFactory<ResourceDescriptor> resourceFactory);

    /**
     * Get root resource matched to <code>requestPath</code>.
     *
     * @param requestPath
     *         request path
     * @param parameterValues
     *         see {@link ApplicationContext#getParameterValues()}
     * @return root resource matched to <code>requestPath</code> or
     * <code>null</code>
     */
    ObjectFactory<ResourceDescriptor> getMatchedResource(String requestPath, List<String> parameterValues);

    /**
     * Remove root resource of supplied class from root resource collection.
     *
     * @param clazz
     *         root resource class
     * @return removed resource or <code>null</code> if resource of specified
     * class not found
     */
    ObjectFactory<ResourceDescriptor> removeResource(Class<?> clazz);

    /**
     * Remove root resource with specified UriTemplate from root resource
     * collection.
     *
     * @param path
     *         root resource path
     * @return removed resource or <code>null</code> if resource for specified
     * template not found
     */
    ObjectFactory<ResourceDescriptor> removeResource(String path);

}
