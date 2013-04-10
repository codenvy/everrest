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
package org.everrest.core.impl;

import org.everrest.core.ComponentLifecycleScope;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.provider.ByteEntityProvider;
import org.everrest.core.impl.provider.DOMSourceEntityProvider;
import org.everrest.core.impl.provider.DataSourceEntityProvider;
import org.everrest.core.impl.provider.DefaultExceptionMapper;
import org.everrest.core.impl.provider.FileEntityProvider;
import org.everrest.core.impl.provider.InputStreamEntityProvider;
import org.everrest.core.impl.provider.JAXBContextResolver;
import org.everrest.core.impl.provider.JAXBElementEntityProvider;
import org.everrest.core.impl.provider.JAXBObjectEntityProvider;
import org.everrest.core.impl.provider.JsonEntityProvider;
import org.everrest.core.impl.provider.MultipartFormDataEntityProvider;
import org.everrest.core.impl.provider.MultivaluedMapEntityProvider;
import org.everrest.core.impl.provider.ProviderDescriptorImpl;
import org.everrest.core.impl.provider.ReaderEntityProvider;
import org.everrest.core.impl.provider.SAXSourceEntityProvider;
import org.everrest.core.impl.provider.StreamOutputEntityProvider;
import org.everrest.core.impl.provider.StreamSourceEntityProvider;
import org.everrest.core.impl.provider.StringEntityProvider;
import org.everrest.core.impl.resource.ResourceDescriptorValidator;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.Logger;
import org.everrest.core.util.MediaTypeMap;
import org.everrest.core.util.MediaTypeMultivaluedMap;
import org.everrest.core.util.UriPatternMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gives access to common predefined provider. Users of EverRest are not expected to use this class or any of its
 * subclasses.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 * @see Providers
 * @see Provider
 * @see MessageBodyReader
 * @see MessageBodyWriter
 * @see ContextResolver
 * @see ExceptionMapper
 * @see Filter
 * @see RequestFilter
 * @see ResponseFilter
 * @see MethodInvokerFilter
 */
public class ProviderBinder implements Providers {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ProviderBinder.class);

    /** Need have possibility to disable replacing default providers. */
    private static final RuntimePermission PROVIDERS_PERMISSIONS = new RuntimePermission("providersManagePermission");

    /** Providers binder instance. */
    private static final AtomicReference<ProviderBinder> INSTANCE = new AtomicReference<ProviderBinder>();

    /** @return instance of {@link ProviderBinder} */
    public static ProviderBinder getInstance() {
        ProviderBinder t = INSTANCE.get();
        if (t != null) {
            return t;
        }
        synchronized (INSTANCE) {
            t = INSTANCE.get();
            if (t != null) {
                return t;
            }
            t = new ProviderBinder();
            INSTANCE.compareAndSet(null, t);
        }
        return INSTANCE.get();
    }

    /**
     * Replace default set of providers by new one. This must not be used by regular users of EverRest framework.
     *
     * @param inst
     *         instance of ProviderBinder
     * @throws SecurityException
     *         if caller is not permitted to call this method because to current security policy
     */
    public static void setInstance(ProviderBinder inst) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(PROVIDERS_PERMISSIONS);
        }
        INSTANCE.set(inst);
    }

    /** Read message body providers. Also see {@link MediaTypeMultivaluedMap}. */
    protected final MediaTypeMultivaluedMap<ObjectFactory<ProviderDescriptor>> writeProviders =
            new MediaTypeMultivaluedMap<ObjectFactory<ProviderDescriptor>>();

    /** Read message body providers. Also see {@link MediaTypeMultivaluedMap}. */
    protected final MediaTypeMultivaluedMap<ObjectFactory<ProviderDescriptor>> readProviders =
            new MediaTypeMultivaluedMap<ObjectFactory<ProviderDescriptor>>();

    /** Exception mappers, see {@link ExceptionMapper}. */
    protected final Map<Class<? extends Throwable>, ObjectFactory<ProviderDescriptor>> exceptionMappers =
            new HashMap<Class<? extends Throwable>, ObjectFactory<ProviderDescriptor>>();

    /** Context resolvers. */
    protected final Map<Class<?>, MediaTypeMap<ObjectFactory<ProviderDescriptor>>> contextResolvers =
            new HashMap<Class<?>, MediaTypeMap<ObjectFactory<ProviderDescriptor>>>();

    /** Request filters, see {@link RequestFilter}. */
    protected final UriPatternMap<ObjectFactory<FilterDescriptor>> requestFilters =
            new UriPatternMap<ObjectFactory<FilterDescriptor>>();

    /** Response filters, see {@link ResponseFilter}. */
    protected final UriPatternMap<ObjectFactory<FilterDescriptor>> responseFilters =
            new UriPatternMap<ObjectFactory<FilterDescriptor>>();

    /** Method invoking filters. */
    protected final UriPatternMap<ObjectFactory<FilterDescriptor>> invokerFilters =
            new UriPatternMap<ObjectFactory<FilterDescriptor>>();

    /** Validator. */
    protected final ResourceDescriptorVisitor rdv = ResourceDescriptorValidator.getInstance();

    protected ProviderBinder() {
        init();
    }

    //

    /**
     * Add per-request ContextResolver.
     *
     * @param clazz
     *         class of implementation ContextResolver
     */
    public void addContextResolver(@SuppressWarnings("rawtypes") Class<? extends ContextResolver> clazz) {
        try {
            ProviderDescriptor descriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.PER_REQUEST);
            descriptor.accept(rdv);
            addContextResolver(new PerRequestObjectFactory<ProviderDescriptor>(descriptor));
        } catch (Exception e) {
            LOG.error("Failed add ContextResolver " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton ContextResolver.
     *
     * @param instance
     *         ContextResolver instance
     */
    @SuppressWarnings("rawtypes")
    public void addContextResolver(ContextResolver instance) {
        Class<? extends ContextResolver> clazz = instance.getClass();
        try {
            ProviderDescriptor descriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.SINGLETON);
            descriptor.accept(rdv);
            addContextResolver(new SingletonObjectFactory<ProviderDescriptor>(descriptor, instance));
        } catch (Exception e) {
            LOG.error("Failed add ContextResolver " + clazz.getName(), e);
        }
    }

    /**
     * Add per-request ExceptionMapper.
     *
     * @param clazz
     *         class of implementation ExceptionMapper
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addExceptionMapper(Class<? extends ExceptionMapper> clazz) {
        try {
            addExceptionMapper(new PerRequestObjectFactory(new ProviderDescriptorImpl(clazz,
                                                                                      ComponentLifecycleScope.PER_REQUEST)));
        } catch (Exception e) {
            LOG.error("Failed add ExceptionMapper " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton ExceptionMapper.
     *
     * @param instance
     *         ExceptionMapper instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addExceptionMapper(ExceptionMapper instance) {
        Class<? extends ExceptionMapper> clazz = instance.getClass();
        try {
            addExceptionMapper(new SingletonObjectFactory(new ProviderDescriptorImpl(clazz,
                                                                                     ComponentLifecycleScope.SINGLETON), instance));
        } catch (Exception e) {
            LOG.error("Failed add ExceptionMapper " + clazz.getName(), e);
        }
    }

    /**
     * Add per-request MessageBodyReader.
     *
     * @param clazz
     *         class of implementation MessageBodyReader
     */
    public void addMessageBodyReader(@SuppressWarnings("rawtypes") Class<? extends MessageBodyReader> clazz) {
        try {
            ProviderDescriptor descriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.PER_REQUEST);
            descriptor.accept(rdv);
            addMessageBodyReader(new PerRequestObjectFactory<ProviderDescriptor>(descriptor));
        } catch (Exception e) {
            LOG.error("Failed add MessageBodyReader " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton MessageBodyReader.
     *
     * @param instance
     *         MessageBodyReader instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addMessageBodyReader(MessageBodyReader instance) {
        Class<? extends MessageBodyReader> clazz = instance.getClass();
        try {
            ProviderDescriptor descriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.SINGLETON);
            descriptor.accept(rdv);
            addMessageBodyReader(new SingletonObjectFactory(descriptor, instance));
        } catch (Exception e) {
            LOG.error("Failed add MessageBodyReader " + clazz.getName(), e);
        }
    }

    /**
     * Add per-request MessageBodyWriter.
     *
     * @param clazz
     *         class of implementation MessageBodyWriter
     */
    public void addMessageBodyWriter(@SuppressWarnings("rawtypes") Class<? extends MessageBodyWriter> clazz) {
        try {
            ProviderDescriptor descriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.PER_REQUEST);
            descriptor.accept(rdv);
            addMessageBodyWriter(new PerRequestObjectFactory<ProviderDescriptor>(descriptor));
        } catch (Exception e) {
            LOG.error("Failed add MessageBodyWriter " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton MessageBodyWriter.
     *
     * @param instance
     *         MessageBodyWriter instance
     */
    @SuppressWarnings("rawtypes")
    public void addMessageBodyWriter(MessageBodyWriter instance) {
        Class<? extends MessageBodyWriter> clazz = instance.getClass();
        try {
            ProviderDescriptor descriptor = new ProviderDescriptorImpl(clazz, ComponentLifecycleScope.SINGLETON);
            descriptor.accept(rdv);
            addMessageBodyWriter(new SingletonObjectFactory<ProviderDescriptor>(descriptor, instance));
        } catch (Exception e) {
            LOG.error("Failed add MessageBodyWriter " + clazz.getName(), e);
        }
    }

    /**
     * Add per-request MethodInvokerFilter.
     *
     * @param clazz
     *         class of implementation MethodInvokerFilter
     */
    public void addMethodInvokerFilter(Class<? extends MethodInvokerFilter> clazz) {
        try {
            FilterDescriptor descriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.PER_REQUEST);
            descriptor.accept(rdv);
            addMethodInvokerFilter(new PerRequestObjectFactory<FilterDescriptor>(descriptor));
        } catch (Exception e) {
            LOG.error("Failed add MethodInvokerFilter " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton MethodInvokerFilter.
     *
     * @param instance
     *         MethodInvokerFilter instance
     */
    public void addMethodInvokerFilter(MethodInvokerFilter instance) {
        Class<? extends MethodInvokerFilter> clazz = instance.getClass();
        try {
            FilterDescriptor descriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.SINGLETON);
            descriptor.accept(rdv);
            addMethodInvokerFilter(new SingletonObjectFactory<FilterDescriptor>(descriptor, instance));
        } catch (Exception e) {
            LOG.error("Failed add RequestFilter " + clazz.getName(), e);
        }
    }

    /**
     * Add per-request RequestFilter.
     *
     * @param clazz
     *         class of implementation RequestFilter
     */
    public void addRequestFilter(Class<? extends RequestFilter> clazz) {
        try {
            FilterDescriptor descriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.PER_REQUEST);
            descriptor.accept(rdv);
            addRequestFilter(new PerRequestObjectFactory<FilterDescriptor>(descriptor));
        } catch (Exception e) {
            LOG.error("Failed add MethodInvokerFilter " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton RequestFilter.
     *
     * @param instance
     *         RequestFilter instance
     */
    public void addRequestFilter(RequestFilter instance) {
        Class<? extends RequestFilter> clazz = instance.getClass();
        try {
            FilterDescriptor descriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.SINGLETON);
            descriptor.accept(rdv);
            addRequestFilter(new SingletonObjectFactory<FilterDescriptor>(descriptor, instance));
        } catch (Exception e) {
            LOG.error("Failed add RequestFilter " + clazz.getName(), e);
        }
    }

    /**
     * Add per-request ResponseFilter.
     *
     * @param clazz
     *         class of implementation ResponseFilter
     */
    public void addResponseFilter(Class<? extends ResponseFilter> clazz) {
        try {
            FilterDescriptor descriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.PER_REQUEST);
            descriptor.accept(rdv);
            addResponseFilter(new PerRequestObjectFactory<FilterDescriptor>(descriptor));
        } catch (Exception e) {
            LOG.error("Failed add ResponseFilter " + clazz.getName(), e);
        }
    }

    /**
     * Add singleton ResponseFilter.
     *
     * @param instance
     *         ResponseFilter instance
     */
    public void addResponseFilter(ResponseFilter instance) {
        Class<? extends ResponseFilter> clazz = instance.getClass();
        try {
            FilterDescriptor descriptor = new FilterDescriptorImpl(clazz, ComponentLifecycleScope.SINGLETON);
            descriptor.accept(rdv);
            addResponseFilter(new SingletonObjectFactory<FilterDescriptor>(descriptor, instance));
        } catch (Exception e) {
            LOG.error("Failed add ResponseFilter " + clazz.getName(), e);
        }
    }

    /**
     * Get list of most acceptable writer's media type for specified type.
     *
     * @param type
     *         type
     * @param genericType
     *         generic type
     * @param annotations
     *         annotations
     * @return sorted acceptable media type collection
     * @see MediaTypeHelper#MEDIA_TYPE_COMPARATOR
     */
    public List<MediaType> getAcceptableWriterMediaTypes(Class<?> type, Type genericType, Annotation[] annotations) {
        return doGetAcceptableWriterMediaTypes(type, genericType, annotations);
    }

    /** {@inheritDoc} */
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
        return doGetContextResolver(contextType, mediaType);
    }

    /** {@inheritDoc} */
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        return doGetExceptionMapper(type);
    }

    /** {@inheritDoc} */
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations,
                                                         MediaType mediaType) {
        return doGetMessageBodyReader(type, genericType, annotations, mediaType);
    }

    /** {@inheritDoc} */
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations,
                                                         MediaType mediaType) {
        return doGetMessageBodyWriter(type, genericType, annotations, mediaType);
    }

    /**
     * @param path
     *         request path
     * @return acceptable method invocation filters
     */
    public List<ObjectFactory<FilterDescriptor>> getMethodInvokerFilters(String path) {
        return doGetMatchedFilters(path, invokerFilters);
    }

    /**
     * @param path
     *         request path
     * @return acceptable request filters
     */
    public List<ObjectFactory<FilterDescriptor>> getRequestFilters(String path) {
        return doGetMatchedFilters(path, requestFilters);
    }

    /**
     * @param path
     *         request path
     * @return acceptable response filters
     */
    public List<ObjectFactory<FilterDescriptor>> getResponseFilters(String path) {
        return doGetMatchedFilters(path, responseFilters);
    }

    public void addContextResolver(ObjectFactory<ProviderDescriptor> contextResolverFactory) {
        for (Type type : contextResolverFactory.getObjectModel().getObjectClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)type;
                if (ContextResolver.class == pt.getRawType()) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    if (typeArguments.length > 1) {
                        throw new RuntimeException("Unable strong determine actual type argument, more then one type found.");
                    }

                    Class<?> aclazz = (Class<?>)typeArguments[0];
                    MediaTypeMap<ObjectFactory<ProviderDescriptor>> pm = contextResolvers.get(aclazz);

                    if (pm == null) {
                        pm = new MediaTypeMap<ObjectFactory<ProviderDescriptor>>();
                        contextResolvers.put(aclazz, pm);
                    }

                    for (MediaType mime : contextResolverFactory.getObjectModel().produces()) {
                        if (pm.get(mime) != null) {
                            throw new RuntimeException("ContextResolver for " + aclazz.getName() + " and media type " + mime
                                                       + " already registered.");
                        } else {
                            pm.put(mime, contextResolverFactory);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addExceptionMapper(ObjectFactory<ProviderDescriptor> exceptionMapperFactory) {
        for (Type type : exceptionMapperFactory.getObjectModel().getObjectClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)type;
                if (ExceptionMapper.class == pt.getRawType()) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    if (typeArguments.length > 1) {
                        throw new RuntimeException("Unable strong determine actual type argument, more then one type found.");
                    }
                    Class<? extends Throwable> exc = (Class<? extends Throwable>)typeArguments[0];

                    if (exceptionMappers.get(exc) != null) {
                        throw new RuntimeException("ExceptionMapper for exception " + exc + " already registered.");
                    }
                    exceptionMappers.put(exc, exceptionMapperFactory);
                }
            }
        }
    }

    public void addMessageBodyReader(ObjectFactory<ProviderDescriptor> readerFactory) {
        // MessageBodyReader is smart component and can determine which type it
        // supports, see method MessageBodyReader.isReadable. So here does not
        // check is reader for the same Java and media type already exists.
        // Let it be under developer's control.
        for (MediaType mime : readerFactory.getObjectModel().consumes()) {
            readProviders.getList(mime).add(readerFactory);
        }
    }

    public void addMessageBodyWriter(ObjectFactory<ProviderDescriptor> writerFactory) {
        // MessageBodyWriter is smart component and can determine which type it
        // supports, see method MessageBodyWriter#isWriteable. So here does not
        // check is writer for the same Java and media type already exists.
        // Let it be under developer's control.
        for (MediaType mime : writerFactory.getObjectModel().produces()) {
            writeProviders.getList(mime).add(writerFactory);
        }
    }

    public void addMethodInvokerFilter(ObjectFactory<FilterDescriptor> filterFactory) {
        invokerFilters.getList(filterFactory.getObjectModel().getUriPattern()).add(filterFactory);
    }

    public void addRequestFilter(ObjectFactory<FilterDescriptor> filterFactory) {
        requestFilters.getList(filterFactory.getObjectModel().getUriPattern()).add(filterFactory);
    }

    public void addResponseFilter(ObjectFactory<FilterDescriptor> filterFactory) {
        responseFilters.getList(filterFactory.getObjectModel().getUriPattern()).add(filterFactory);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<MediaType> doGetAcceptableWriterMediaTypes(Class<?> type, Type genericType, Annotation[] annotations) {
        List<MediaType> l = new ArrayList<MediaType>();
        Map<Class, MessageBodyWriter> instanceCache = new HashMap<Class, MessageBodyWriter>();
        for (Map.Entry<MediaType, List<ObjectFactory<ProviderDescriptor>>> e : writeProviders.entrySet()) {
            MediaType mime = e.getKey();
            for (ObjectFactory pf : e.getValue()) {
                Class clazz = pf.getObjectModel().getObjectClass();
                MessageBodyWriter writer = instanceCache.get(clazz);
                if (writer == null) {
                    writer = (MessageBodyWriter)pf.getInstance(ApplicationContextImpl.getCurrent());
                    instanceCache.put(clazz, writer);
                }
                if (writer.isWriteable(type, genericType, annotations, MediaTypeHelper.DEFAULT_TYPE)) {
                    l.add(mime);
                }
            }
        }
        if (l.size() > 1) {
            Collections.sort(l, MediaTypeHelper.MEDIA_TYPE_COMPARATOR);
        }
        return l;
    }

    protected <T> ContextResolver<T> doGetContextResolver(Class<T> contextType, MediaType mediaType) {
        MediaTypeMap<ObjectFactory<ProviderDescriptor>> pm = contextResolvers.get(contextType);
        ContextResolver<T> resolver = null;
        if (pm != null) {
            MediaTypeHelper.MediaTypeRange mrange = new MediaTypeHelper.MediaTypeRange(mediaType);
            while (mrange.hasNext() && resolver == null) {
                MediaType actual = mrange.next();
                resolver = doGetContextResolver(pm, contextType, actual);
            }
        }
        return resolver;
    }

    /**
     * @param <T>
     *         context resolver actual type argument
     * @param pm
     *         MediaTypeMap that contains ProviderFactories that may produce objects that are instance of T
     * @param contextType
     *         context type
     * @param mediaType
     *         media type that can be used to restrict context resolver choose
     * @return ContextResolver or null if nothing was found
     */
    @SuppressWarnings("unchecked")
    private <T> ContextResolver<T> doGetContextResolver(MediaTypeMap<ObjectFactory<ProviderDescriptor>> pm,
                                                        Class<T> contextType, MediaType mediaType) {
        for (Map.Entry<MediaType, ObjectFactory<ProviderDescriptor>> e : pm.entrySet()) {
            if (mediaType.isCompatible(e.getKey())) {
                return (ContextResolver<T>)e.getValue().getInstance(ApplicationContextImpl.getCurrent());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Throwable> ExceptionMapper<T> doGetExceptionMapper(Class<T> type) {
        @SuppressWarnings("rawtypes")
        ObjectFactory pf = exceptionMappers.get(type);
        if (pf != null) {
            return (ExceptionMapper<T>)pf.getInstance(ApplicationContextImpl.getCurrent());
        }
        return null;
    }

    /**
     * Looking for message body reader according to supplied entity class, entity generic type, annotations and content
     * type.
     *
     * @param <T>
     *         message body reader actual type argument
     * @param type
     *         entity type
     * @param genericType
     *         entity generic type
     * @param annotations
     *         annotations
     * @param mediaType
     *         entity content type
     * @return message body reader or null if no one was found.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> MessageBodyReader<T> doGetMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations,
                                                              MediaType mediaType) {
        MediaTypeHelper.MediaTypeRange mrange = new MediaTypeHelper.MediaTypeRange(mediaType);
        Map<Class, MessageBodyReader> instanceCache = new HashMap<Class, MessageBodyReader>();
        while (mrange.hasNext()) {
            MediaType actual = mrange.next();
            for (ObjectFactory pf : readProviders.getList(actual)) {
                Class<?> clazz = pf.getObjectModel().getObjectClass();
                MessageBodyReader reader = instanceCache.get(clazz);
                if (reader == null) {
                    reader = (MessageBodyReader)pf.getInstance(ApplicationContextImpl.getCurrent());
                    instanceCache.put(clazz, reader);
                }
                if (reader.isReadable(type, genericType, annotations, actual)) {
                    return reader;
                }
            }
        }
        return null;
    }

    /**
     * Looking for message body writer according to supplied entity class, entity generic type, annotations and content
     * type.
     *
     * @param <T>
     *         message body writer actual type argument
     * @param type
     *         entity type
     * @param genericType
     *         entity generic type
     * @param annotations
     *         annotations
     * @param mediaType
     *         content type in which entity should be represented
     * @return message body writer or null if no one was found.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> MessageBodyWriter<T> doGetMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations,
                                                              MediaType mediaType) {
        MediaTypeHelper.MediaTypeRange mrange = new MediaTypeHelper.MediaTypeRange(mediaType);
        Map<Class, MessageBodyWriter> instanceCache = new HashMap<Class, MessageBodyWriter>();
        while (mrange.hasNext()) {
            MediaType actual = mrange.next();
            for (ObjectFactory pf : writeProviders.getList(actual)) {
                Class<?> clazz = pf.getObjectModel().getObjectClass();
                MessageBodyWriter writer = instanceCache.get(clazz);
                if (writer == null) {
                    writer = (MessageBodyWriter)pf.getInstance(ApplicationContextImpl.getCurrent());
                    instanceCache.put(clazz, writer);
                }
                if (writer.isWriteable(type, genericType, annotations, actual)) {
                    return writer;
                }
            }
        }
        return null;
    }

    /**
     * @param path
     *         request path
     * @param m
     *         filter map
     * @return acceptable filter
     * @see #getMethodInvokerFilters(String)
     * @see #getRequestFilters(String)
     * @see #getResponseFilters(String)
     */
    protected List<ObjectFactory<FilterDescriptor>> doGetMatchedFilters(String path,
                                                                        UriPatternMap<ObjectFactory<FilterDescriptor>> m) {
        List<ObjectFactory<FilterDescriptor>> l = new ArrayList<ObjectFactory<FilterDescriptor>>();

        List<String> capturingValues = new ArrayList<String>();
        for (Map.Entry<UriPattern, List<ObjectFactory<FilterDescriptor>>> e : m.entrySet()) {
            UriPattern uriPattern = e.getKey();

            if (uriPattern != null) {
                if (uriPattern.match(path, capturingValues)) {
                    int len = capturingValues.size();
                    if (capturingValues.get(len - 1) != null && !"/".equals(capturingValues.get(len - 1))) {
                        // not matched
                        continue;
                    }
                } else {
                    // not matched
                    continue;
                }
            }
            // if matched or UriPattern is null
            l.addAll(e.getValue());
        }
        return l;
    }

    /** Add prepared providers. */
    protected void init() {
        // Add known Providers, Filters, etc with predefined life cycle.
        ByteEntityProvider baep = new ByteEntityProvider();
        addMessageBodyReader(baep);
        addMessageBodyWriter(baep);

        DataSourceEntityProvider dsep = new DataSourceEntityProvider();
        addMessageBodyReader(dsep);
        addMessageBodyWriter(dsep);

        DOMSourceEntityProvider domsep = new DOMSourceEntityProvider();
        addMessageBodyReader(domsep);
        addMessageBodyWriter(domsep);

        FileEntityProvider fep = new FileEntityProvider();
        addMessageBodyReader(fep);
        addMessageBodyWriter(fep);

        MultivaluedMapEntityProvider mvep = new MultivaluedMapEntityProvider();
        addMessageBodyReader(mvep);
        addMessageBodyWriter(mvep);

        InputStreamEntityProvider isep = new InputStreamEntityProvider();
        addMessageBodyReader(isep);
        addMessageBodyWriter(isep);

        ReaderEntityProvider rep = new ReaderEntityProvider();
        addMessageBodyReader(rep);
        addMessageBodyWriter(rep);

        SAXSourceEntityProvider saxep = new SAXSourceEntityProvider();
        addMessageBodyReader(saxep);
        addMessageBodyWriter(saxep);

        StreamSourceEntityProvider ssep = new StreamSourceEntityProvider();
        addMessageBodyReader(ssep);
        addMessageBodyWriter(ssep);

        StringEntityProvider sep = new StringEntityProvider();
        addMessageBodyReader(sep);
        addMessageBodyWriter(sep);

        StreamOutputEntityProvider soep = new StreamOutputEntityProvider();
        addMessageBodyReader(soep);
        addMessageBodyWriter(soep);

        JsonEntityProvider<Object> jsep = new JsonEntityProvider<Object>();
        addMessageBodyReader(jsep);
        addMessageBodyWriter(jsep);

        // per-request mode, Providers should be injected
        addMessageBodyReader(JAXBElementEntityProvider.class);
        addMessageBodyWriter(JAXBElementEntityProvider.class);

        addMessageBodyReader(JAXBObjectEntityProvider.class);
        addMessageBodyWriter(JAXBObjectEntityProvider.class);

        // per-request mode, HttpServletRequest should be injected in provider
        addMessageBodyReader(MultipartFormDataEntityProvider.class);

        // JAXB context
        addContextResolver(new JAXBContextResolver());

        addExceptionMapper(new DefaultExceptionMapper());
    }

}
