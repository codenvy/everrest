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
package org.everrest.exoplatform.container;

import org.everrest.core.ApplicationContext;
import org.everrest.core.BaseDependencySupplier;
import org.everrest.core.DependencySupplier;
import org.everrest.core.Filter;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.FilterDescriptorImpl;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.resource.AbstractResourceDescriptor;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link ComponentAdapter} for providing instance of JAX-RS resource of provider.
 *
 * @author andrew00x
 */
public class RestfulComponentAdapter implements ComponentAdapter {
    public static boolean isRestfulComponent(Object classOrInstance) {
        Class<?> clazz = (classOrInstance instanceof Class) ? (Class<?>)classOrInstance : classOrInstance.getClass();
        return clazz.isAnnotationPresent(Path.class)
               || clazz.isAnnotationPresent(Provider.class)
               || clazz.isAnnotationPresent(Filter.class);
    }

    private final Class<?>                   clazz;
    private final Object                     componentKey;
    private final ObjectFactory<ObjectModel> factory;
    private final ParameterizedType[]        implementedInterfaces;

    /**
     * Create new RestfulComponentAdapter for specified componentKey and class or instance.
     *
     * @param componentKey
     *         the key of component
     * @param classOrInstance
     *         the class or instance of JAX-RS resource or provider
     * @throws NullPointerException
     *         if <code>componentKey</code> or <code>classOrInstance</code> is <code>null</code>
     * @throws IllegalArgumentException
     *         if <code>classOrInstance</code> has neither {@link Path}, {@link Provider} nor
     *         {@link Filter}.
     */
    public RestfulComponentAdapter(Object componentKey, Object classOrInstance) {
        if (componentKey == null || classOrInstance == null) {
            throw new NullPointerException();
        }

        this.componentKey = componentKey;

        Object instance = null;

        if (classOrInstance instanceof Class) {
            clazz = (Class<?>)classOrInstance;
        } else {
            clazz = classOrInstance.getClass();
            instance = classOrInstance;
        }

        implementedInterfaces = getImplementedInterfaces(clazz);

        ObjectModel objectModel;
        if (clazz.isAnnotationPresent(Path.class)) {
            objectModel = instance == null ? new AbstractResourceDescriptor(clazz) : new AbstractResourceDescriptor(instance);
        } else if (clazz.isAnnotationPresent(Provider.class)) {
            objectModel = instance == null ? new ProviderDescriptorImpl(clazz) : new ProviderDescriptorImpl(instance);
        } else if (clazz.isAnnotationPresent(Filter.class)) {
            objectModel = instance == null ? new FilterDescriptorImpl(clazz) : new FilterDescriptorImpl(instance);
        } else {
            throw new IllegalArgumentException("Incorrect type or instance " + clazz + ". ");
        }

        factory = instance == null ? new PerRequestObjectFactory<>(objectModel) : new SingletonObjectFactory<>(objectModel, instance);
    }

    public ObjectModel getObjectModel() {
        return factory.getObjectModel();
    }

    public ObjectFactory<ObjectModel> getFactory() {
        return factory;
    }

    ParameterizedType[] getImplementedInterfaces() {
        return implementedInterfaces;
    }

    @Override
    public Object getComponentInstance(final PicoContainer container) throws PicoInitializationException,
                                                                             PicoIntrospectionException {
        // ComponentAdapter always create instance of component by using ObjectFactory instance.
        // PicoContainer (version 1.x) can't provide all required dependencies.
        // ComponentAdapter in this case is just facade for ObjectFactory.
        final ApplicationContext context = ApplicationContext.getCurrent();
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not initialized. ");
        }
        DependencySupplier dependencies = context.getDependencySupplier();
        try {
            context.setDependencySupplier(new BaseDependencySupplier() {
                @Override
                public Object getInstance(Class<?> type) {
                    Object object = container.getComponentInstanceOfType(type);
                    if (object != null) {
                        return object;
                    }
                    DependencySupplier contextDependencySupplier = context.getDependencySupplier();
                    if (contextDependencySupplier != null) {
                        return contextDependencySupplier.getInstance(type);
                    }
                    return null;
                }
            });
            return factory.getInstance(context);
        } finally {
            context.setDependencySupplier(dependencies);
        }
    }

    @Override
    public void verify(PicoContainer container) throws PicoIntrospectionException {
    }

    @Override
    public void accept(PicoVisitor visitor) {
        visitor.visitComponentAdapter(this);
    }

    @Override
    public Object getComponentKey() {
        return componentKey;
    }

    @Override
    public Class getComponentImplementation() {
        return clazz;
    }

    @Override
    public String toString() {
        return String.format("RestfulComponentAdapter [%s]", getComponentKey());
    }

    //

    private static final Class[] KNOWN_INTERFACES =
            new Class[]{MessageBodyReader.class, MessageBodyWriter.class, ExceptionMapper.class, ContextResolver.class};

    private static ParameterizedType[] getImplementedInterfaces(Class<?> type) {
        if (type.isAnnotationPresent(Provider.class)) {
            List<ParameterizedType> implementedInterfaces = new ArrayList<>();
            for (int i = 0; i < KNOWN_INTERFACES.length; i++) {
                ParameterizedType impl = getGenericInterface(KNOWN_INTERFACES[i], type);
                if (impl != null) {
                    implementedInterfaces.add(impl);
                }
            }

            if (implementedInterfaces.size() == 0) {
                throw new IllegalArgumentException(
                        String.format("Type %s annotated with @javax.ws.rs.ext.Provider but does not implement any of the interfaces: %s", type, Arrays.toString(KNOWN_INTERFACES)));
            }

            return implementedInterfaces.toArray(new ParameterizedType[implementedInterfaces.size()]);
        }
        return new ParameterizedType[0];
    }

    private static ParameterizedType getGenericInterface(Class<?> interfaceType, Class<?> type) {
        for (Type t : type.getGenericInterfaces()) {
            if (t instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType)t;
                if (interfaceType == parameterized.getRawType()) {
                    return parameterized;
                }
            }
        }
        Class<?> sc = type.getSuperclass();
        if (sc != null && sc != Object.class) {
            return getGenericInterface(interfaceType, sc);
        }
        return null;
    }
}
