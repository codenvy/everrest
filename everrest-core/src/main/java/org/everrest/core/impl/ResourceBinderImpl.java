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
package org.everrest.core.impl;

import org.everrest.core.ApplicationContext;
import org.everrest.core.ObjectFactory;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResourcePublicationException;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
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
    private static final Logger LOG = Logger.getLogger(ResourceBinderImpl.class);

    /** Resource's comparator. */
    protected static final Comparator<ObjectFactory<AbstractResourceDescriptor>> RESOURCE_COMPARATOR =
            new Comparator<ObjectFactory<AbstractResourceDescriptor>>() {
                /**
                 * Compare two ResourceClass for order.
                 *
                 * @param o1 first ResourceClass to be compared
                 * @param o2 second ResourceClass to be compared
                 * @return positive , zero or negative dependent of {@link UriPattern}
                 *         comparison
                 * @see Comparator#compare(Object, Object)
                 * @see UriPattern
                 * @see UriPattern#URIPATTERN_COMPARATOR
                 */
                @Override
                public int compare(ObjectFactory<AbstractResourceDescriptor> o1, ObjectFactory<AbstractResourceDescriptor> o2) {
                    return UriPattern.URIPATTERN_COMPARATOR
                            .compare(o1.getObjectModel().getUriPattern(), o2.getObjectModel().getUriPattern());
                }
            };

    /** Root resource descriptors. */
    private volatile List<ObjectFactory<AbstractResourceDescriptor>> resources = new ArrayList<>();

    /** Validator. */
    private final ResourceDescriptorVisitor rdv = ResourceDescriptorValidator.getInstance();

    /** Update resources (add, remove, clear) lock. */
    private final ReentrantLock lock = new ReentrantLock();

    public ResourceBinderImpl() {
    }

    @Override
    public void addResource(Class<?> resourceClass, MultivaluedMap<String, String> properties) {
        Path path = resourceClass.getAnnotation(Path.class);
        if (path == null) {
            throw new ResourcePublicationException(String.format(
                    "Resource class %s it is not root resource. Path annotation javax.ws.rs.Path is not specified for this class.",
                    resourceClass.getName()));
        }
        try {
            addResource(new PerRequestObjectFactory<>(newResourceDescriptor(null, resourceClass, properties)));
        } catch (Exception e) {
            if (e instanceof ResourcePublicationException) {
                throw (ResourcePublicationException)e;
            }
            throw new ResourcePublicationException(e.getMessage(), e);
        }
    }

    @Override
    public void addResource(String uriPattern, Class<?> resourceClass, MultivaluedMap<String, String> properties) {
        addResource(new PerRequestObjectFactory<>(newResourceDescriptor(uriPattern, resourceClass, properties)));
    }

    private AbstractResourceDescriptor newResourceDescriptor(String path,
                                                             Class<?> resourceClass,
                                                             MultivaluedMap<String, String> properties) {
        AbstractResourceDescriptor descriptor =
                path == null ? new AbstractResourceDescriptorImpl(resourceClass) : new AbstractResourceDescriptorImpl(path, resourceClass);
        descriptor.accept(rdv);
        if (properties != null) {
            descriptor.getProperties().putAll(properties);
        }
        return descriptor;
    }

    @Override
    public void addResource(Object resource, MultivaluedMap<String, String> properties) {
        Path path = resource.getClass().getAnnotation(Path.class);
        if (path == null) {
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

    private AbstractResourceDescriptor newResourceDescriptor(String path,
                                                             Object resource,
                                                             MultivaluedMap<String, String> properties) {
        AbstractResourceDescriptor descriptor =
                path == null ? new AbstractResourceDescriptorImpl(resource) : new AbstractResourceDescriptorImpl(path, resource);
        descriptor.accept(rdv);
        if (properties != null) {
            descriptor.getProperties().putAll(properties);
        }
        return descriptor;
    }

    @Override
    public void addResource(ObjectFactory<AbstractResourceDescriptor> resourceFactory) {
        UriPattern pattern = resourceFactory.getObjectModel().getUriPattern();
        lock.lock();
        try {
            List<ObjectFactory<AbstractResourceDescriptor>> snapshot = new ArrayList<>(resources);
            for (ObjectFactory<AbstractResourceDescriptor> resource : snapshot) {
                if (resource.getObjectModel().getUriPattern().equals(resourceFactory.getObjectModel().getUriPattern())) {
                    if (resource.getObjectModel().getObjectClass() == resourceFactory.getObjectModel().getObjectClass()) {
                        LOG.debug(String.format("Resource %s already registered.",
                                                resourceFactory.getObjectModel().getObjectClass().getName()));
                        return;
                    }
                    throw new ResourcePublicationException(String.format(
                            "Resource class %s can't be registered. Resource class %s with the same pattern %s already registered.",
                            resourceFactory.getObjectModel().getObjectClass().getName(),
                            resource.getObjectModel().getObjectClass().getName(), pattern));
                }
            }
            snapshot.add(resourceFactory);
            Collections.sort(snapshot, RESOURCE_COMPARATOR);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Add resource: " + resourceFactory.getObjectModel());
            }
            resources = snapshot;
        } finally {
            lock.unlock();
        }
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
    public ObjectFactory<AbstractResourceDescriptor> getMatchedResource(String requestPath, List<String> parameterValues) {
        ObjectFactory<AbstractResourceDescriptor> resourceFactory = null;
        List<ObjectFactory<AbstractResourceDescriptor>> myResources = resources;

        for (ObjectFactory<AbstractResourceDescriptor> resource : myResources) {
            if (resource.getObjectModel().getUriPattern().match(requestPath, parameterValues)) {
                // all times will at least 1
                int len = parameterValues.size();
                // If capturing group contains last element and this element is
                // neither null nor '/' then ResourceClass must contains at least one
                // sub-resource method or sub-resource locator.
                if (parameterValues.get(len - 1) != null && !parameterValues.get(len - 1).equals("/")) {
                    if (0 == resource.getObjectModel().getSubResourceMethods().size()
                             + resource.getObjectModel().getSubResourceLocators().size()) {
                        continue;
                    }
                }
                resourceFactory = resource;
                break;
            }
        }
        return resourceFactory;
    }


    @Override
    public List<ObjectFactory<AbstractResourceDescriptor>> getResources() {
        List<ObjectFactory<AbstractResourceDescriptor>> myResources = resources;
        return new ArrayList<>(myResources);
    }


    @Override
    public int getSize() {
        List<ObjectFactory<AbstractResourceDescriptor>> myResources = resources;
        return myResources.size();
    }

    @Override
    public ObjectFactory<AbstractResourceDescriptor> removeResource(Class<?> clazz) {
        lock.lock();
        try {
            ObjectFactory<AbstractResourceDescriptor> resource = null;
            List<ObjectFactory<AbstractResourceDescriptor>> snapshot = new ArrayList<>(resources);

            for (Iterator<ObjectFactory<AbstractResourceDescriptor>> iterator = snapshot.iterator();
                 iterator.hasNext() && resource == null; ) {

                ObjectFactory<AbstractResourceDescriptor> next = iterator.next();
                Class<?> resourceClass = next.getObjectModel().getObjectClass();
                if (clazz.equals(resourceClass)) {
                    resource = next;
                    iterator.remove();
                }
            }
            if (resource != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove resource: " + resource.getObjectModel());
                }

                resources = snapshot;
            }
            return resource;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ObjectFactory<AbstractResourceDescriptor> removeResource(String path) {
        lock.lock();
        try {
            ObjectFactory<AbstractResourceDescriptor> resource = null;
            List<ObjectFactory<AbstractResourceDescriptor>> snapshot = new ArrayList<>(resources);

            UriPattern pattern = new UriPattern(path);
            for (Iterator<ObjectFactory<AbstractResourceDescriptor>> iterator = snapshot.iterator();
                 iterator.hasNext() && resource == null; ) {

                ObjectFactory<AbstractResourceDescriptor> next = iterator.next();
                UriPattern resourcePattern = next.getObjectModel().getUriPattern();
                if (pattern.equals(resourcePattern)) {
                    resource = next;
                    iterator.remove();
                }
            }
            if (resource != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove resource: " + resource.getObjectModel());
                }

                resources = snapshot;
            }
            return resource;
        } finally {
            lock.unlock();
        }
    }
}
