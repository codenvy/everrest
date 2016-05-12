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

import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.uri.UriPattern;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.AbstractPicoVisitor;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Base implementation of PicoVisitor for lookup JAX-RS components by annotation.
 *
 * @author andrew00x
 */
abstract class ComponentsFinder extends AbstractPicoVisitor {
    private static final Set<ComponentFilter> COMPONENT_FILTERS = new LinkedHashSet<>();

    static {
        for (ComponentFilter componentFilter : ServiceLoader.load(ComponentFilter.class)) {
            COMPONENT_FILTERS.add(componentFilter);
        }
    }

    static List<ComponentAdapter> withFilters(List<ComponentAdapter> source) {
        if (source.size() == 0 || COMPONENT_FILTERS.size() == 0) {
            return source;
        }
        List<ComponentAdapter> result = new ArrayList<>();
        for (ComponentAdapter component : source) {
            for (ComponentFilter componentFilter : COMPONENT_FILTERS) {
                if (componentFilter.accept(component)) {
                    result.add(component);
                }
            }
        }
        return result;
    }

    final Class<? extends Annotation> annotation;

    ComponentsFinder(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public final void visitComponentAdapter(ComponentAdapter componentAdapter) {
        checkTraversal();
    }

    @Override
    public final void visitParameter(Parameter parameter) {
        checkTraversal();
    }

    final static class ResourceFinder extends ComponentsFinder {
        private final String       requestPath;
        private final List<String> parameterValues;
        private final List<ComponentAdapter> components = new ArrayList<>();

        ResourceFinder(Class<? extends Annotation> annotation, String requestPath, List<String> parameterValues) {
            super(annotation);
            this.requestPath = requestPath;
            this.parameterValues = parameterValues;
        }

        @Override
        public Object traverse(Object container) {
            components.clear();
            try {
                super.traverse(container);
                Map<UriPattern, ComponentAdapter> matched = new HashMap<>();
                for (ComponentAdapter adapter : components) {
                    if (adapter instanceof RestfulComponentAdapter) {
                        ResourceDescriptor resource =
                                (ResourceDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                        if (resource.getUriPattern().match(requestPath, parameterValues)) {
                            String tail = parameterValues.get(parameterValues.size() - 1);
                            if (tail == null
                                || "/".equals(tail)
                                || (resource.getSubResourceMethods().size() + resource.getSubResourceLocators().size()) > 0) {
                                matched.put(resource.getUriPattern(), adapter);
                            }
                        }
                    }
                }
                if (matched.isEmpty()) {
                    return null;
                } else if (matched.size() == 1) {
                    ComponentAdapter componentAdapter = matched.values().iterator().next();
                    matched.clear();
                    return componentAdapter;
                } else {
                    UriPattern[] keys = matched.keySet().toArray(new UriPattern[matched.size()]);
                    Arrays.sort(keys, UriPattern.URIPATTERN_COMPARATOR);
                    ComponentAdapter componentAdapter = matched.get(keys[0]);
                    matched.clear();
                    return componentAdapter;
                }
            } finally {
                components.clear();
            }
        }

        @Override
        public void visitContainer(PicoContainer pico) {
            checkTraversal();
            components.addAll(withFilters(((RestfulContainer)pico).getComponentAdapters(annotation)));
        }
    }

    final static class WriterFinder<T> extends ComponentsFinder {
        private final Class<T>     entityType;
        private final Type         genericEntityType;
        private final MediaType    mediaType;
        private final Annotation[] annotations;
        private final List<ComponentAdapter> components = new ArrayList<>();

        WriterFinder(Class<? extends Annotation> annotation, Class<T> entityType, Type genericEntityType, Annotation[] annotations,
                     MediaType mediaType) {
            super(annotation);
            this.entityType = entityType;
            this.genericEntityType = genericEntityType;
            this.annotations = annotations;
            this.mediaType = mediaType;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Object traverse(Object container) {
            components.clear();
            try {
                super.traverse(container);
                Iterator<MediaType> mediaTypeRange = MediaTypeHelper.createDescendingMediaTypeIterator(mediaType);
                while (mediaTypeRange.hasNext()) {
                    MediaType next = mediaTypeRange.next();
                    for (ComponentAdapter adapter : components) {
                        if (adapter instanceof RestfulComponentAdapter) {
                            ProviderDescriptor provider = (ProviderDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                            if (provider.produces().contains(next)) {
                                MessageBodyWriter writer = (MessageBodyWriter)adapter.getComponentInstance((PicoContainer)container);
                                if (writer.isWriteable(entityType, genericEntityType, annotations, next)) {
                                    return writer;
                                }
                            }
                        }
                    }
                }
                return null;
            } finally {
                components.clear();
            }
        }

        @Override
        public void visitContainer(PicoContainer pico) {
            checkTraversal();
            components.addAll(withFilters(((RestfulContainer)pico).getComponentAdaptersOfType(MessageBodyWriter.class, annotation)));
        }
    }

    final static class ReaderFinder<T> extends ComponentsFinder {
        private final Class<T>     entityType;
        private final Type         genericEntityType;
        private final MediaType    mediaType;
        private final Annotation[] annotations;
        private final List<ComponentAdapter> components = new ArrayList<>();

        ReaderFinder(Class<? extends Annotation> annotation, Class<T> entityType, Type genericEntityType, Annotation[] annotations,
                     MediaType mediaType) {
            super(annotation);
            this.entityType = entityType;
            this.genericEntityType = genericEntityType;
            this.annotations = annotations;
            this.mediaType = mediaType;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Object traverse(Object container) {
            components.clear();
            try {
                super.traverse(container);
                Iterator<MediaType> mediaTypeRange = MediaTypeHelper.createDescendingMediaTypeIterator(mediaType);
                while (mediaTypeRange.hasNext()) {
                    MediaType next = mediaTypeRange.next();
                    for (ComponentAdapter adapter : components) {
                        if (adapter instanceof RestfulComponentAdapter) {
                            ProviderDescriptor provider =
                                    (ProviderDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                            if (provider.consumes().contains(next)) {
                                MessageBodyReader reader = (MessageBodyReader)adapter.getComponentInstance((PicoContainer)container);
                                if (reader.isReadable(entityType, genericEntityType, annotations, next)) {
                                    return reader;
                                }
                            }
                        }
                    }
                }
                return null;
            } finally {
                components.clear();
            }
        }

        @Override
        public void visitContainer(PicoContainer pico) {
            checkTraversal();
            components.addAll(withFilters(((RestfulContainer)pico).getComponentAdaptersOfType(MessageBodyReader.class, annotation)));
        }
    }

    final static class ExceptionMapperFinder<T> extends ComponentsFinder {
        private final Class<T> exceptionType;
        private final List<ComponentAdapter> components = new ArrayList<>();

        ExceptionMapperFinder(Class<? extends Annotation> annotation, Class<T> exceptionType) {
            super(annotation);
            this.exceptionType = exceptionType;
        }

        @Override
        public Object traverse(Object container) {
            components.clear();
            try {
                super.traverse(container);
                for (ComponentAdapter adapter : components) {
                    if (adapter instanceof RestfulComponentAdapter) {
                        ParameterizedType[] implementedInterfaces = ((RestfulComponentAdapter)adapter).getImplementedInterfaces();
                        for (ParameterizedType genericInterface : implementedInterfaces) {
                            if (ExceptionMapper.class == genericInterface.getRawType()
                                && exceptionType == genericInterface.getActualTypeArguments()[0]) {
                                return adapter.getComponentInstance((PicoContainer)container);
                            }
                        }
                    }
                }
                return null;
            } finally {
                components.clear();
            }
        }

        @Override
        public void visitContainer(PicoContainer pico) {
            checkTraversal();
            components.addAll(withFilters(((RestfulContainer)pico).getComponentAdaptersOfType(ExceptionMapper.class, annotation)));
        }
    }

    final static class ContextResolverFinder<T> extends ComponentsFinder {
        private final Class<T>  contextType;
        private final MediaType mediaType;
        private final List<ComponentAdapter> components = new ArrayList<>();

        ContextResolverFinder(Class<? extends Annotation> annotation, Class<T> contextType, MediaType mediaType) {
            super(annotation);
            this.contextType = contextType;
            this.mediaType = mediaType;
        }

        @Override
        public Object traverse(Object container) {
            components.clear();
            try {
                super.traverse(container);
                Iterator<MediaType> mediaTypeRange = MediaTypeHelper.createDescendingMediaTypeIterator(mediaType);
                while (mediaTypeRange.hasNext()) {
                    MediaType next = mediaTypeRange.next();
                    for (ComponentAdapter adapter : components) {
                        if (adapter instanceof RestfulComponentAdapter) {
                            ProviderDescriptor provider =
                                    (ProviderDescriptor)((RestfulComponentAdapter)adapter).getObjectModel();
                            if (provider.produces().contains(next)) {
                                ParameterizedType[] implementedInterfaces = ((RestfulComponentAdapter)adapter).getImplementedInterfaces();
                                for (ParameterizedType genericInterface : implementedInterfaces) {
                                    Type rawType = genericInterface.getRawType();
                                    Class<?> actualType;
                                    try {
                                        actualType = (Class<?>)genericInterface.getActualTypeArguments()[0];
                                    } catch (ClassCastException e) {
                                        continue;
                                    }
                                    if (ContextResolver.class == rawType && contextType.isAssignableFrom(actualType)) {
                                        return adapter.getComponentInstance((PicoContainer)container);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            } finally {
                components.clear();
            }
        }

        @Override
        public void visitContainer(PicoContainer pico) {
            checkTraversal();
            components.addAll(withFilters(((RestfulContainer)pico).getComponentAdaptersOfType(ContextResolver.class, annotation)));
        }
    }

    static ComponentAdapter findResource(PicoContainer pico, String requestPath, List<String> parameterValues) {
        return (ComponentAdapter)new ResourceFinder(Path.class, requestPath, parameterValues).traverse(pico);
    }

    @SuppressWarnings("unchecked")
    static <T> MessageBodyWriter<T> findWriter(PicoContainer pico, Class<T> entityType, Type genericEntityType,
                                               Annotation[] annotations, MediaType mediaType) {
        return (MessageBodyWriter<T>)new WriterFinder<>(Provider.class, entityType, genericEntityType, annotations, mediaType)
                .traverse(pico);
    }

    @SuppressWarnings("unchecked")
    static <T> MessageBodyReader<T> findReader(PicoContainer pico, Class<T> entityType, Type genericEntityType,
                                               Annotation[] annotations, MediaType mediaType) {
        return (MessageBodyReader<T>)new ReaderFinder<>(Provider.class, entityType, genericEntityType, annotations, mediaType)
                .traverse(pico);
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> ExceptionMapper<T> findExceptionMapper(PicoContainer pico, Class<T> exceptionType) {
        return (ExceptionMapper<T>)new ExceptionMapperFinder<>(Provider.class, exceptionType).traverse(pico);
    }

    @SuppressWarnings("unchecked")
    static <T> ContextResolver<T> findContextResolver(PicoContainer pico, Class<T> contextType, MediaType mediaType) {
        return (ContextResolver<T>)new ContextResolverFinder<>(Provider.class, contextType, mediaType).traverse(pico);
    }
}
