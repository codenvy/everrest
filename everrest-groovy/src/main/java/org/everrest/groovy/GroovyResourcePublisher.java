/*
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.groovy;

import groovy.lang.GroovyCodeSource;

import org.everrest.core.DependencySupplier;
import org.everrest.core.ObjectFactory;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.ResourcePublicationException;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.uri.UriPattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Manage via {@link ResourceBinder} Groovy based RESTful services.
 *
 * @author andrew00x
 */
public class GroovyResourcePublisher {
    /** Default character set name. */
    protected static final String DEFAULT_CHARSET_NAME = "UTF-8";

    /** Default character set. */
    protected static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

    protected final ResourceBinder binder;

    protected final Map<ResourceId, String> resources = Collections.synchronizedMap(new HashMap<>());

    protected final GroovyClassLoaderProvider classLoaderProvider;

    protected final DependencySupplier dependencies;

    protected final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>() {
        @Override
        public int compare(Constructor<?> o1, Constructor<?> o2) {
            return o2.getParameterTypes().length - o1.getParameterTypes().length;
        }
    };

    /**
     * Create GroovyJaxrsPublisher which is able publish per-request and singleton resources. Any required dependencies for per-request
     * resource injected by {@link PerRequestObjectFactory}.
     *
     * @param binder
     *         resource binder
     * @param classLoaderProvider
     *         GroovyClassLoaderProvider
     * @param dependencies
     *         dependencies resolver
     * @see DependencySupplier
     */
    protected GroovyResourcePublisher(ResourceBinder binder, GroovyClassLoaderProvider classLoaderProvider,
                                      DependencySupplier dependencies) {
        this.binder = binder;
        this.classLoaderProvider = classLoaderProvider;
        this.dependencies = dependencies;
    }

    /**
     * Create GroovyJaxrsPublisher which is able publish per-request and singleton resources. Any required dependencies for per-request
     * resource injected by {@link PerRequestObjectFactory}.
     *
     * @param binder
     *         resource binder
     * @param dependencies
     *         dependencies resolver
     * @see DependencySupplier
     */
    public GroovyResourcePublisher(ResourceBinder binder, DependencySupplier dependencies) {
        this(binder, new GroovyClassLoaderProvider(), dependencies);
    }

    /**
     * Get resource corresponded to specified id <code>resourceId</code> .
     *
     * @param resourceId
     *         resource id
     * @return resource or <code>null</code>
     */
    public ObjectFactory<ResourceDescriptor> getResource(ResourceId resourceId) {
        String path = resources.get(resourceId);
        if (path == null) {
            return null;
        }

        UriPattern pattern = new UriPattern(path);
        for (ObjectFactory<ResourceDescriptor> res : binder.getResources()) {
            if (res.getObjectModel().getUriPattern().equals(pattern)) {
                return res;
            }
        }
        // If resource not exists any more but still in mapping.
        resources.remove(resourceId);
        return null;
    }

    /**
     * Check is groovy resource with specified id is published or not
     *
     * @param resourceId
     *         id of resource to be checked
     * @return <code>true</code> if resource is published and <code>false</code>* otherwise
     */
    public boolean isPublished(ResourceId resourceId) {
        return null != getResource(resourceId);
    }

    /**
     * Parse given stream and publish result as per-request RESTful service.
     *
     * @param in
     *         stream which contains groovy source code of RESTful service
     * @param resourceId
     *         id to be assigned to resource
     * @param properties
     *         optional resource properties. This parameter may be <code>null</code>
     * @param src
     *         additional path to Groovy sources
     * @param files
     *         Groovy source files to be added in build path directly
     * @throws ResourcePublicationException
     *         see {@link ResourceBinder#addResource(Class, MultivaluedMap)}
     */
    public void publishPerRequest(InputStream in, ResourceId resourceId, MultivaluedMap<String, String> properties,
                                  SourceFolder[] src, SourceFile[] files) {
        Class<?> rc;
        try {
            rc = classLoaderProvider.getGroovyClassLoader(src).parseClass(in, resourceId.getId(), files);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        binder.addResource(rc, properties);
        resources.put(resourceId, rc.getAnnotation(Path.class).value());
    }

    /**
     * Parse given <code>source</code> and publish result as per-request RESTful service.
     *
     * @param source
     *         groovy source code of RESTful service
     * @param resourceId
     *         id to be assigned to resource
     * @param properties
     *         optional resource properties. This parameter may be <code>null</code>
     * @param src
     *         additional path to Groovy sources
     * @param files
     *         Groovy source files to be added in build path directly
     * @throws ResourcePublicationException
     *         see {@link ResourceBinder#addResource(Class, MultivaluedMap)}
     */
    public final void publishPerRequest(String source, ResourceId resourceId, MultivaluedMap<String, String> properties,
                                        SourceFolder[] src, SourceFile[] files) {
        publishPerRequest(source, DEFAULT_CHARSET, resourceId, properties, src, files);
    }

    /**
     * Parse given <code>source</code> and publish result as per-request RESTful
     * service.
     *
     * @param source
     *         groovy source code of RESTful service
     * @param charset
     *         source string charset. May be <code>null</code> than default charset will be in use
     * @param resourceId
     *         id to be assigned to resource
     * @param properties
     *         optional resource properties. This parameter may be <code>null</code>.
     * @param src
     *         additional path to Groovy sources
     * @param files
     *         Groovy source files to be added in build path directly
     * @throws UnsupportedCharsetException
     *         if <code>charset</code> is unsupported
     * @throws ResourcePublicationException
     *         see {@link ResourceBinder#addResource(Class, MultivaluedMap)}
     */
    public final void publishPerRequest(String source, String charset, ResourceId resourceId,
                                        MultivaluedMap<String, String> properties, SourceFolder[] src, SourceFile[] files) {
        publishPerRequest(source, charset == null ? DEFAULT_CHARSET : Charset.forName(charset), resourceId, properties, src, files);
    }

    /**
     * Parse given stream and publish result as singleton RESTful service.
     *
     * @param in
     *         stream which contains groovy source code of RESTful service
     * @param resourceId
     *         id to be assigned to resource
     * @param properties
     *         optional resource properties. This parameter may be <code>null</code>
     * @param src
     *         additional path to Groovy sources
     * @param files
     *         Groovy source files to be added in build path directly
     * @throws ResourcePublicationException
     *         see {@link ResourceBinder#addResource(Object, MultivaluedMap)}
     */
    public void publishSingleton(InputStream in, ResourceId resourceId, MultivaluedMap<String, String> properties,
                                 SourceFolder[] src, SourceFile[] files) {
        Class<?> rc;
        try {
            rc = classLoaderProvider.getGroovyClassLoader(src).parseClass(in, resourceId.getId(), files);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        Object r;
        try {
            r = createInstance(rc);
        } catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ResourcePublicationException(e.getMessage());
        }

        binder.addResource(r, properties);
        resources.put(resourceId, r.getClass().getAnnotation(Path.class).value());
    }

    /**
     * Parse given <code>source</code> and publish result as singleton RESTful service.
     *
     * @param source
     *         groovy source code of RESTful service
     * @param resourceId
     *         name of resource
     * @param properties
     *         optional resource properties. This parameter may be
     *         <code>null</code>.
     * @param src
     *         additional path to Groovy sources
     * @param files
     *         Groovy source files to be added in build path directly
     * @throws ResourcePublicationException
     *         see {@link ResourceBinder#addResource(Object, MultivaluedMap)}
     */
    public final void publishSingleton(String source, ResourceId resourceId, MultivaluedMap<String, String> properties,
                                       SourceFolder[] src, SourceFile[] files) {
        publishSingleton(source, DEFAULT_CHARSET, resourceId, properties, src, files);
    }

    /**
     * Parse given <code>source</code> and publish result as singleton RESTful service.
     *
     * @param source
     *         groovy source code of RESTful service
     * @param charset
     *         source string charset. May be <code>null</code> than default charset will be in use
     * @param resourceId
     *         name of resource
     * @param properties
     *         optional resource properties. This parameter may be <code>null</code>.
     * @param src
     *         additional path to Groovy sources
     * @param files
     *         Groovy source files to be added in build path directly
     * @throws UnsupportedCharsetException
     *         if <code>charset</code> is unsupported
     * @throws ResourcePublicationException
     *         see {@link ResourceBinder#addResource(Object, MultivaluedMap)}
     */
    public final void publishSingleton(String source, String charset, ResourceId resourceId,
                                       MultivaluedMap<String, String> properties, SourceFolder[] src, SourceFile[] files) {
        publishSingleton(source, charset == null ? DEFAULT_CHARSET : Charset.forName(charset), resourceId, properties, src, files);
    }

    /**
     * Unpublish resource with specified id.
     *
     * @param resourceId
     *         id of resource to be unpublished
     * @return <code>true</code> if resource was published and <code>false</code> otherwise, e.g. because there is not resource corresponded
     * to supplied <code>resourceId</code>
     */
    public ObjectFactory<ResourceDescriptor> unpublishResource(ResourceId resourceId) {
        String path = resources.get(resourceId);
        if (path == null) {
            return null;
        }
        ObjectFactory<ResourceDescriptor> resource = binder.removeResource(path);
        if (resource != null) {
            resources.remove(resourceId);
        }
        return resource;
    }

    private void publishPerRequest(String source, Charset charset, ResourceId resourceId,
                                   MultivaluedMap<String, String> properties, SourceFolder[] src, SourceFile[] files) {
        byte[] bytes = source.getBytes(charset);
        publishPerRequest(new ByteArrayInputStream(bytes), resourceId, properties, src, files);
    }

    private void publishSingleton(String source, Charset charset, ResourceId resourceId,
                                  MultivaluedMap<String, String> properties, SourceFolder[] src, SourceFile[] files) {
        byte[] bytes = source.getBytes(charset);
        publishSingleton(new ByteArrayInputStream(bytes), resourceId, properties, src, files);
    }

    protected Object createInstance(Class<?> clazz)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = clazz.getConstructors();
        //Sort constructors by number of parameters. With more parameters must be first.
        Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        l:
        for (Constructor<?> c : constructors) {
            Class<?>[] parameterTypes = c.getParameterTypes();
            if (parameterTypes.length == 0) {
                return c.newInstance();
            }
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Object param = dependencies.getInstance(parameterTypes[i]);
                if (param == null) {
                    continue l;
                }
                parameters[i] = param;
            }
            return c.newInstance(parameters);
        }
        throw new ResourcePublicationException(String.format("Unable create instance of class %s. Required constructor's dependencies can't be resolved. ", clazz.getName()));
    }

    /**
     * Create {@link GroovyCodeSource} from given stream and name. Code base 'file:/groovy/script/jaxrs' will be used.
     *
     * @param in
     *         groovy source code stream
     * @param name
     *         code source name
     * @return GroovyCodeSource
     */
    protected GroovyCodeSource createCodeSource(InputStream in, String name) {
        GroovyCodeSource gcs = new GroovyCodeSource(new BufferedReader(new InputStreamReader(in)), name, "/groovy/script/jaxrs");
        gcs.setCachable(false);
        return gcs;
    }
}
