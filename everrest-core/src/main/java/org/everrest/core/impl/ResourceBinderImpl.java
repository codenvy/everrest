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
package org.everrest.core.impl;

import com.google.common.collect.Iterables;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ObjectFactory;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResourcePublicationException;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.uri.UriPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author andrew00x
 */
public class ResourceBinderImpl implements ResourceBinder {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ResourceBinderImpl.class);

    /**
     * Compare two ResourceClass for order. Method compare returns positive, zero or negative dependent of {@link UriPattern} comparison.
     * @see UriPattern
     * @see UriPattern#URIPATTERN_COMPARATOR
     */
    static final Comparator<ObjectFactory<ResourceDescriptor>> RESOURCE_COMPARATOR =
            (resourceOne, resourceTwo) -> UriPattern.URIPATTERN_COMPARATOR
                    .compare(resourceOne.getObjectModel().getUriPattern(), resourceTwo.getObjectModel().getUriPattern());

    /** Root resource descriptors. */
    private volatile List<ObjectFactory<ResourceDescriptor>> resources = new ArrayList<>();

    /** Update resources (add, remove, clear) lock. */
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void addResource(Class<?> resourceClass, MultivaluedMap<String, String> properties) {
        if (!resourceClass.isAnnotationPresent(Path.class)) {
            throw new ResourcePublicationException(String.format(
                    "Resource class %s it is not root resource. Path annotation javax.ws.rs.Path is not specified for this class.",
                    resourceClass.getName()));
        }
        try {
            addResource(new PerRequestObjectFactory<>(newResourceDescriptor(null, resourceClass, properties)));
        } catch (ResourcePublicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourcePublicationException(e.getMessage(), e);
        }
    }

    @Override
    public void addResource(String uriPattern, Class<?> resourceClass, MultivaluedMap<String, String> properties) {
        addResource(new PerRequestObjectFactory<>(newResourceDescriptor(uriPattern, resourceClass, properties)));
    }

    private ResourceDescriptor newResourceDescriptor(String path,
                                                     Class<?> resourceClass,
                                                     MultivaluedMap<String, String> properties) {
        ResourceDescriptor descriptor = path == null ? new AbstractResourceDescriptor(resourceClass) : new AbstractResourceDescriptor(path, resourceClass);
        if (properties != null) {
            descriptor.getProperties().putAll(properties);
        }
        return descriptor;
    }

    @Override
    public void addResource(Object resource, MultivaluedMap<String, String> properties) {
        if (!resource.getClass().isAnnotationPresent(Path.class)) {
            throw new ResourcePublicationException(String.format(
                    "Resource class %s it is not root resource. Path annotation javax.ws.rs.Path is not specified for this class.",
                    resource.getClass().getName()));
        }
        addResource(new SingletonObjectFactory<>(newResourceDescriptor(null, resource, properties), resource));
    }

    @Override
    public void addResource(String uriPattern, Object resource, MultivaluedMap<String, String> properties) {
        addResource(new SingletonObjectFactory<>(newResourceDescriptor(uriPattern, resource, properties), resource));
    }

    private ResourceDescriptor newResourceDescriptor(String path,
                                                     Object resource,
                                                     MultivaluedMap<String, String> properties) {
        ResourceDescriptor descriptor =
                path == null ? new AbstractResourceDescriptor(resource) : new AbstractResourceDescriptor(path, resource);
        if (properties != null) {
            descriptor.getProperties().putAll(properties);
        }
        return descriptor;
    }

    @Override
    public void addResource(ObjectFactory<ResourceDescriptor> newResourceFactory) {
        UriPattern pattern = newResourceFactory.getObjectModel().getUriPattern();
        lock.lock();
        try {
            List<ObjectFactory<ResourceDescriptor>> snapshot = new ArrayList<>(resources);
            for (ObjectFactory<ResourceDescriptor> resourceFactory : snapshot) {
                if (resourceFactory.getObjectModel().getUriPattern().equals(newResourceFactory.getObjectModel().getUriPattern())) {
                    if (resourceFactory.getObjectModel().getObjectClass() == newResourceFactory.getObjectModel().getObjectClass()) {
                        LOG.debug("Resource {} already registered", newResourceFactory.getObjectModel().getObjectClass().getName());
                        return;
                    }
                    throw new ResourcePublicationException(String.format(
                            "Resource class %s loaded from %s can't be registered. Resource class %s loaded from %s with the same pattern %s already registered.",
                            newResourceFactory.getObjectModel().getObjectClass().getName(), getCodeSource(newResourceFactory.getObjectModel().getObjectClass()),
                            resourceFactory.getObjectModel().getObjectClass().getName(), getCodeSource(resourceFactory.getObjectModel().getObjectClass()), pattern));
                }
            }
            snapshot.add(newResourceFactory);
            Collections.sort(snapshot, RESOURCE_COMPARATOR);
            LOG.debug("Add resource: {}", newResourceFactory.getObjectModel());
            resources = snapshot;
        } finally {
            lock.unlock();
        }
    }

    private CodeSource getCodeSource(Class<?> aClass) {
        return aClass.getProtectionDomain().getCodeSource();
    }

    /** Clear the list of resources. */
    public void clear() {
        lock.lock();
        try {
            resources = new ArrayList<>();
        } finally {
            lock.unlock();
        }
    }

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
    @Override
    public ObjectFactory<ResourceDescriptor> getMatchedResource(String requestPath, List<String> parameterValues) {
        ObjectFactory<ResourceDescriptor> resourceFactory = null;
        List<ObjectFactory<ResourceDescriptor>> myResources = resources;

        for (ObjectFactory<ResourceDescriptor> resource : myResources) {
            if (resource.getObjectModel().getUriPattern().match(requestPath, parameterValues)) {
                // all times will at least 1
                String lastParameterValue = Iterables.getLast(parameterValues);
                // If capturing group contains last element and this element is
                // neither null nor '/' then ResourceClass must contains at least one
                // sub-resource method or sub-resource locator.
                if (lastParameterValue == null || lastParameterValue.equals("/") || hasSubResourceMethodsOrSubResourceLocators(resource)) {
                    resourceFactory = resource;
                    break;
                }
            }
        }
        return resourceFactory;
    }

    private boolean hasSubResourceMethodsOrSubResourceLocators(ObjectFactory<ResourceDescriptor> resource) {
        return resource.getObjectModel().getSubResourceMethods().size()
                 + resource.getObjectModel().getSubResourceLocators().size() > 0;
    }

    @Override
    public List<ObjectFactory<ResourceDescriptor>> getResources() {
        List<ObjectFactory<ResourceDescriptor>> myResources = resources;
        return new ArrayList<>(myResources);
    }


    @Override
    public int getSize() {
        List<ObjectFactory<ResourceDescriptor>> myResources = resources;
        return myResources.size();
    }

    @Override
    public ObjectFactory<ResourceDescriptor> removeResource(Class<?> clazz) {
        lock.lock();
        try {
            ObjectFactory<ResourceDescriptor> resource = null;
            List<ObjectFactory<ResourceDescriptor>> snapshot = new ArrayList<>(resources);

            for (Iterator<ObjectFactory<ResourceDescriptor>> iterator = snapshot.iterator(); iterator.hasNext() && resource == null; ) {
                ObjectFactory<ResourceDescriptor> next = iterator.next();
                Class<?> resourceClass = next.getObjectModel().getObjectClass();
                if (clazz.equals(resourceClass)) {
                    resource = next;
                    iterator.remove();
                }
            }
            if (resource != null) {
                LOG.debug("Remove resource: {}", resource.getObjectModel());
                resources = snapshot;
            }
            return resource;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ObjectFactory<ResourceDescriptor> removeResource(String path) {
        lock.lock();
        try {
            ObjectFactory<ResourceDescriptor> resource = null;
            List<ObjectFactory<ResourceDescriptor>> snapshot = new ArrayList<>(resources);

            UriPattern pattern = new UriPattern(path);
            for (Iterator<ObjectFactory<ResourceDescriptor>> iterator = snapshot.iterator(); iterator.hasNext() && resource == null; ) {

                ObjectFactory<ResourceDescriptor> next = iterator.next();
                UriPattern resourcePattern = next.getObjectModel().getUriPattern();
                if (pattern.equals(resourcePattern)) {
                    resource = next;
                    iterator.remove();
                }
            }
            if (resource != null) {
                LOG.debug("Remove resource: {}", resource.getObjectModel());
                resources = snapshot;
            }
            return resource;
        } finally {
            lock.unlock();
        }
    }
}
