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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;

import com.google.common.collect.Iterables;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.everrest.core.ApplicationContext;
import org.everrest.core.Filter;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.ObjectFactory;
import org.everrest.core.ObjectModel;
import org.everrest.core.PerRequestObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResponseFilter;
import org.everrest.core.SingletonObjectFactory;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.provider.ByteEntityProvider;
import org.everrest.core.impl.provider.DOMSourceEntityProvider;
import org.everrest.core.impl.provider.DataSourceEntityProvider;
import org.everrest.core.impl.provider.DefaultExceptionMapper;
import org.everrest.core.impl.provider.DuplicateProviderException;
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
import org.everrest.core.impl.provider.multipart.CollectionMultipartFormDataMessageBodyWriter;
import org.everrest.core.impl.provider.multipart.ListMultipartFormDataMessageBodyReader;
import org.everrest.core.impl.provider.multipart.MapMultipartFormDataMessageBodyReader;
import org.everrest.core.method.MethodInvokerFilter;
import org.everrest.core.provider.ProviderDescriptor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.MediaTypeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives access to common predefined provider. Users of EverRest are not expected to use this class
 * or any of its subclasses.
 *
 * @author andrew00x
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
  private static final Logger LOG = LoggerFactory.getLogger(ProviderBinder.class);

  /** Need have possibility to disable replacing default providers. */
  private static final RuntimePermission PROVIDERS_PERMISSIONS =
      new RuntimePermission("providersManagePermission");

  /** Providers binder instance. */
  private static final AtomicReference<ProviderBinder> INSTANCE = new AtomicReference<>();

  /** @return instance of {@link ProviderBinder} */
  public static ProviderBinder getInstance() {
    ProviderBinder providerBinder = INSTANCE.get();
    if (providerBinder == null) {
      ProviderBinder newProviderBinder = new ProviderBinder();
      if (INSTANCE.compareAndSet(null, newProviderBinder)) {
        providerBinder = newProviderBinder;
        providerBinder.init();
      } else {
        providerBinder = INSTANCE.get();
      }
    }
    return providerBinder;
  }

  /**
   * Replace default set of providers by new one. This must not be used by regular users of EverRest
   * framework.
   *
   * @param providerBinder instance of ProviderBinder
   * @throws SecurityException if caller is not permitted to call this method because to current
   *     security policy
   */
  public static void setInstance(ProviderBinder providerBinder) {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      security.checkPermission(PROVIDERS_PERMISSIONS);
    }
    INSTANCE.set(providerBinder);
  }

  protected final MediaTypeComparator mediaTypeComparator = new MediaTypeComparator();

  /** Read message body providers. */
  protected final ConcurrentNavigableMap<MediaType, List<ObjectFactory<ProviderDescriptor>>>
      writeProviders = new ConcurrentSkipListMap<>(mediaTypeComparator);

  /** Read message body providers. */
  protected final ConcurrentNavigableMap<MediaType, List<ObjectFactory<ProviderDescriptor>>>
      readProviders = new ConcurrentSkipListMap<>(mediaTypeComparator);

  /** Exception mappers, see {@link ExceptionMapper}. */
  protected final ConcurrentMap<Class<? extends Throwable>, ObjectFactory<ProviderDescriptor>>
      exceptionMappers = new ConcurrentHashMap<>();

  /** Context resolvers. */
  protected final ConcurrentMap<
          Class<?>, NavigableMap<MediaType, ObjectFactory<ProviderDescriptor>>>
      contextResolvers = new ConcurrentHashMap<>();

  /** Request filters, see {@link RequestFilter}. */
  protected final ConcurrentMap<UriPattern, List<ObjectFactory<FilterDescriptor>>> requestFilters =
      new ConcurrentHashMap<>();

  /** Response filters, see {@link ResponseFilter}. */
  protected final ConcurrentMap<UriPattern, List<ObjectFactory<FilterDescriptor>>> responseFilters =
      new ConcurrentHashMap<>();

  /** Method invoking filters. */
  protected final ConcurrentMap<UriPattern, List<ObjectFactory<FilterDescriptor>>> invokerFilters =
      new ConcurrentHashMap<>();

  protected ProviderBinder() {}

  /**
   * Add per-request ContextResolver.
   *
   * @param contextResolverClass class of implementation ContextResolver
   */
  public void addContextResolver(Class<? extends ContextResolver> contextResolverClass) {
    try {
      ProviderDescriptor descriptor = new ProviderDescriptorImpl(contextResolverClass);
      addContextResolver(new PerRequestObjectFactory<>(descriptor));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add ContextResolver %s. %s", contextResolverClass.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton ContextResolver.
   *
   * @param contextResolver ContextResolver instance
   */
  public void addContextResolver(ContextResolver contextResolver) {
    try {
      ProviderDescriptor descriptor = new ProviderDescriptorImpl(contextResolver);
      addContextResolver(new SingletonObjectFactory<>(descriptor, contextResolver));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add ContextResolver %s. %s",
              contextResolver.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add per-request ExceptionMapper.
   *
   * @param exceptionMapperClass class of implementation ExceptionMapper
   */
  @SuppressWarnings({"unchecked"})
  public void addExceptionMapper(Class<? extends ExceptionMapper> exceptionMapperClass) {
    try {
      addExceptionMapper(
          new PerRequestObjectFactory(new ProviderDescriptorImpl(exceptionMapperClass)));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add ExceptionMapper %s. %s", exceptionMapperClass.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton ExceptionMapper.
   *
   * @param exceptionMapper ExceptionMapper instance
   */
  @SuppressWarnings({"unchecked"})
  public void addExceptionMapper(ExceptionMapper exceptionMapper) {
    try {
      addExceptionMapper(
          new SingletonObjectFactory(new ProviderDescriptorImpl(exceptionMapper), exceptionMapper));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add ExceptionMapper %s. %s",
              exceptionMapper.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add per-request MessageBodyReader.
   *
   * @param messageBodyReaderClass class of implementation MessageBodyReader
   */
  public void addMessageBodyReader(Class<? extends MessageBodyReader> messageBodyReaderClass) {
    try {
      ProviderDescriptor descriptor = new ProviderDescriptorImpl(messageBodyReaderClass);
      addMessageBodyReader(new PerRequestObjectFactory<>(descriptor));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add MessageBodyReader %s. %s",
              messageBodyReaderClass.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton MessageBodyReader.
   *
   * @param messageBodyReader MessageBodyReader instance
   */
  @SuppressWarnings({"unchecked"})
  public void addMessageBodyReader(MessageBodyReader messageBodyReader) {
    try {
      ProviderDescriptor descriptor = new ProviderDescriptorImpl(messageBodyReader);
      addMessageBodyReader(new SingletonObjectFactory(descriptor, messageBodyReader));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add MessageBodyReader %s. %s",
              messageBodyReader.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add per-request MessageBodyWriter.
   *
   * @param messageBodyWriter class of implementation MessageBodyWriter
   */
  public void addMessageBodyWriter(Class<? extends MessageBodyWriter> messageBodyWriter) {
    try {
      ProviderDescriptor descriptor = new ProviderDescriptorImpl(messageBodyWriter);
      addMessageBodyWriter(new PerRequestObjectFactory<>(descriptor));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add MessageBodyWriter %s. %s", messageBodyWriter.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton MessageBodyWriter.
   *
   * @param messageBodyWriter MessageBodyWriter instance
   */
  public void addMessageBodyWriter(MessageBodyWriter messageBodyWriter) {
    try {
      ProviderDescriptor descriptor = new ProviderDescriptorImpl(messageBodyWriter);
      addMessageBodyWriter(new SingletonObjectFactory<>(descriptor, messageBodyWriter));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add MessageBodyWriter %s. %s",
              messageBodyWriter.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add per-request MethodInvokerFilter.
   *
   * @param methodInvokerFilterClass class of implementation MethodInvokerFilter
   */
  public void addMethodInvokerFilter(
      Class<? extends MethodInvokerFilter> methodInvokerFilterClass) {
    try {
      FilterDescriptor descriptor = new FilterDescriptorImpl(methodInvokerFilterClass);
      addMethodInvokerFilter(new PerRequestObjectFactory<>(descriptor));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add MethodInvokerFilter %s. %s",
              methodInvokerFilterClass.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton MethodInvokerFilter.
   *
   * @param methodInvokerFilter MethodInvokerFilter instance
   */
  public void addMethodInvokerFilter(MethodInvokerFilter methodInvokerFilter) {
    try {
      FilterDescriptor descriptor = new FilterDescriptorImpl(methodInvokerFilter);
      addMethodInvokerFilter(new SingletonObjectFactory<>(descriptor, methodInvokerFilter));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add MethodInvokerFilter %s. %s",
              methodInvokerFilter.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add per-request RequestFilter.
   *
   * @param requestFilterClass class of implementation RequestFilter
   */
  public void addRequestFilter(Class<? extends RequestFilter> requestFilterClass) {
    try {
      FilterDescriptor descriptor = new FilterDescriptorImpl(requestFilterClass);
      addRequestFilter(new PerRequestObjectFactory<>(descriptor));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add RequestFilter %s. %s", requestFilterClass.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton RequestFilter.
   *
   * @param requestFilter RequestFilter instance
   */
  public void addRequestFilter(RequestFilter requestFilter) {
    try {
      FilterDescriptor descriptor = new FilterDescriptorImpl(requestFilter);
      addRequestFilter(new SingletonObjectFactory<>(descriptor, requestFilter));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add RequestFilter %s. %s",
              requestFilter.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add per-request ResponseFilter.
   *
   * @param responseFilterClass class of implementation ResponseFilter
   */
  public void addResponseFilter(Class<? extends ResponseFilter> responseFilterClass) {
    try {
      FilterDescriptor descriptor = new FilterDescriptorImpl(responseFilterClass);
      addResponseFilter(new PerRequestObjectFactory<>(descriptor));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add ResponseFilter %s. %s", responseFilterClass.getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Add singleton ResponseFilter.
   *
   * @param responseFilter ResponseFilter instance
   */
  public void addResponseFilter(ResponseFilter responseFilter) {
    try {
      FilterDescriptor descriptor = new FilterDescriptorImpl(responseFilter);
      addResponseFilter(new SingletonObjectFactory<>(descriptor, responseFilter));
    } catch (Exception e) {
      LOG.error(
          String.format(
              "Failed add ResponseFilter %s. %s",
              responseFilter.getClass().getName(), e.getMessage()),
          e);
    }
  }

  /**
   * Get list of most acceptable writer's media type for specified type.
   *
   * @param type type
   * @param genericType generic type
   * @param annotations annotations
   * @return sorted acceptable media type collection
   */
  public List<MediaType> getAcceptableWriterMediaTypes(
      Class<?> type, Type genericType, Annotation[] annotations) {
    return doGetAcceptableWriterMediaTypes(type, genericType, annotations);
  }

  @Override
  public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
    return doGetContextResolver(contextType, mediaType);
  }

  @Override
  public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> errorType) {
    return doGetExceptionMapper(errorType);
  }

  @Override
  public <T> MessageBodyReader<T> getMessageBodyReader(
      Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return doGetMessageBodyReader(type, genericType, annotations, mediaType);
  }

  @Override
  public <T> MessageBodyWriter<T> getMessageBodyWriter(
      Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return doGetMessageBodyWriter(type, genericType, annotations, mediaType);
  }

  /**
   * @param path request path
   * @return acceptable method invocation filters
   */
  public List<MethodInvokerFilter> getMethodInvokerFilters(String path) {
    ApplicationContext context = ApplicationContext.getCurrent();
    return doGetMatchedFilters(path, invokerFilters)
        .stream()
        .map(factory -> (MethodInvokerFilter) factory.getInstance(context))
        .collect(toList());
  }

  /**
   * @param path request path
   * @return acceptable request filters
   */
  public List<RequestFilter> getRequestFilters(String path) {
    ApplicationContext context = ApplicationContext.getCurrent();
    return doGetMatchedFilters(path, requestFilters)
        .stream()
        .map(factory -> (RequestFilter) factory.getInstance(context))
        .collect(toList());
  }

  /**
   * @param path request path
   * @return acceptable response filters
   */
  public List<ResponseFilter> getResponseFilters(String path) {
    ApplicationContext context = ApplicationContext.getCurrent();
    return doGetMatchedFilters(path, responseFilters)
        .stream()
        .map(factory -> (ResponseFilter) factory.getInstance(context))
        .collect(toList());
  }

  public void addContextResolver(ObjectFactory<ProviderDescriptor> contextResolverFactory) {
    for (Type type :
        contextResolverFactory.getObjectModel().getObjectClass().getGenericInterfaces()) {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (ContextResolver.class == parameterizedType.getRawType()) {
          Type[] typeArguments = parameterizedType.getActualTypeArguments();
          if (typeArguments.length != 1) {
            throw new IllegalArgumentException("Unable strong determine actual type argument");
          }

          Class<?> aClass = (Class<?>) typeArguments[0];
          NavigableMap<MediaType, ObjectFactory<ProviderDescriptor>> contextResolversForType =
              contextResolvers.get(aClass);

          if (contextResolversForType == null) {
            NavigableMap<MediaType, ObjectFactory<ProviderDescriptor>> newContextResolversForType =
                new ConcurrentSkipListMap<>(mediaTypeComparator);
            contextResolversForType =
                contextResolvers.putIfAbsent(aClass, newContextResolversForType);
            if (contextResolversForType == null) {
              contextResolversForType = newContextResolversForType;
            }
          }

          for (MediaType mediaType : contextResolverFactory.getObjectModel().produces()) {
            if (contextResolversForType.putIfAbsent(mediaType, contextResolverFactory) != null) {
              throw new DuplicateProviderException(
                  String.format(
                      "ContextResolver for %s and media type %s already registered",
                      aClass.getName(), mediaType));
            }
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void addExceptionMapper(ObjectFactory<ProviderDescriptor> exceptionMapperFactory) {
    for (Type type :
        exceptionMapperFactory.getObjectModel().getObjectClass().getGenericInterfaces()) {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (ExceptionMapper.class == parameterizedType.getRawType()) {
          Type[] typeArguments = parameterizedType.getActualTypeArguments();
          if (typeArguments.length != 1) {
            throw new RuntimeException("Unable strong determine actual type argument");
          }
          Class<? extends Throwable> errorType = (Class<? extends Throwable>) typeArguments[0];
          if (exceptionMappers.putIfAbsent(errorType, exceptionMapperFactory) != null) {
            throw new DuplicateProviderException(
                String.format("ExceptionMapper for exception %s already registered", errorType));
          }
        }
      }
    }
  }

  public void addMessageBodyReader(ObjectFactory<ProviderDescriptor> readerFactory) {
    for (MediaType mediaType : readerFactory.getObjectModel().consumes()) {
      addProviderFactory(readProviders, mediaType, readerFactory);
    }
  }

  public void addMessageBodyWriter(ObjectFactory<ProviderDescriptor> writerFactory) {
    for (MediaType mediaType : writerFactory.getObjectModel().produces()) {
      addProviderFactory(writeProviders, mediaType, writerFactory);
    }
  }

  public void addMethodInvokerFilter(ObjectFactory<FilterDescriptor> filterFactory) {
    addProviderFactory(
        invokerFilters, filterFactory.getObjectModel().getUriPattern(), filterFactory);
  }

  public void addRequestFilter(ObjectFactory<FilterDescriptor> filterFactory) {
    addProviderFactory(
        requestFilters, filterFactory.getObjectModel().getUriPattern(), filterFactory);
  }

  public void addResponseFilter(ObjectFactory<FilterDescriptor> filterFactory) {
    addProviderFactory(
        responseFilters, filterFactory.getObjectModel().getUriPattern(), filterFactory);
  }

  private <K, PF extends ObjectModel> void addProviderFactory(
      ConcurrentMap<K, List<ObjectFactory<PF>>> providersFactoryMap,
      K key,
      ObjectFactory<PF> providerFactory) {
    List<ObjectFactory<PF>> providersFactoryList = providersFactoryMap.get(key);
    if (providersFactoryList == null) {
      List<ObjectFactory<PF>> newList = new CopyOnWriteArrayList<>();
      providersFactoryList = providersFactoryMap.putIfAbsent(key, newList);
      if (providersFactoryList == null) {
        providersFactoryList = newList;
      }
    }
    providersFactoryList.add(providerFactory);
  }

  @SuppressWarnings({"unchecked"})
  protected List<MediaType> doGetAcceptableWriterMediaTypes(
      Class<?> type, Type genericType, Annotation[] annotations) {
    List<MediaType> result = new ArrayList<>();
    Map<Class, MessageBodyWriter> instanceCache = new HashMap<>();
    for (Map.Entry<MediaType, List<ObjectFactory<ProviderDescriptor>>> e :
        writeProviders.entrySet()) {
      MediaType mediaType = e.getKey();
      for (ObjectFactory messageBodyWriterFactory : e.getValue()) {
        Class messageBodyWriterClass = messageBodyWriterFactory.getObjectModel().getObjectClass();
        MessageBodyWriter messageBodyWriter = instanceCache.get(messageBodyWriterClass);
        if (messageBodyWriter == null) {
          messageBodyWriter =
              (MessageBodyWriter)
                  messageBodyWriterFactory.getInstance(ApplicationContext.getCurrent());
          instanceCache.put(messageBodyWriterClass, messageBodyWriter);
        }
        if (messageBodyWriter.isWriteable(type, genericType, annotations, WILDCARD_TYPE)) {
          result.add(mediaType);
        }
      }
    }
    if (result.size() > 1) {
      Collections.sort(result, mediaTypeComparator);
    }
    return result;
  }

  protected <T> ContextResolver<T> doGetContextResolver(Class<T> contextType, MediaType mediaType) {
    NavigableMap<MediaType, ObjectFactory<ProviderDescriptor>> mediaTypeToContextResolverMap =
        contextResolvers.get(contextType);
    ContextResolver<T> contextResolver = null;
    if (mediaTypeToContextResolverMap != null) {
      Iterator<MediaType> mediaTypeRange =
          MediaTypeHelper.createDescendingMediaTypeIterator(mediaType);
      while (mediaTypeRange.hasNext() && contextResolver == null) {
        MediaType actual = mediaTypeRange.next();
        contextResolver = doGetContextResolver(mediaTypeToContextResolverMap, actual);
      }
    }
    return contextResolver;
  }

  /**
   * @param mediaTypeToContextResolverMap map that contains ProviderFactories that may produce
   *     objects that are instance of T
   * @param mediaType media type that can be used to restrict context resolver choose
   * @return ContextResolver or null if nothing was found
   */
  @SuppressWarnings("unchecked")
  private <T> ContextResolver<T> doGetContextResolver(
      NavigableMap<MediaType, ObjectFactory<ProviderDescriptor>> mediaTypeToContextResolverMap,
      MediaType mediaType) {
    for (Map.Entry<MediaType, ObjectFactory<ProviderDescriptor>> e :
        mediaTypeToContextResolverMap.entrySet()) {
      if (mediaType.isCompatible(e.getKey())) {
        return (ContextResolver<T>) e.getValue().getInstance(ApplicationContext.getCurrent());
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Throwable> ExceptionMapper<T> doGetExceptionMapper(Class<T> errorType) {
    ObjectFactory objectFactory = exceptionMappers.get(errorType);
    if (objectFactory == null) {
      Class superclassOfErrorType = errorType.getSuperclass();
      while (objectFactory == null && superclassOfErrorType != Object.class) {
        objectFactory = exceptionMappers.get(superclassOfErrorType);
        superclassOfErrorType = superclassOfErrorType.getSuperclass();
      }
    }
    if (objectFactory == null) {
      return null;
    }
    return (ExceptionMapper<T>) objectFactory.getInstance(ApplicationContext.getCurrent());
  }

  /**
   * Looking for message body reader according to supplied entity class, entity generic type,
   * annotations and content type.
   *
   * @param <T> message body reader actual type argument
   * @param type entity type
   * @param genericType entity generic type
   * @param annotations annotations
   * @param mediaType entity content type
   * @return message body reader or null if no one was found.
   */
  @SuppressWarnings({"unchecked"})
  protected <T> MessageBodyReader<T> doGetMessageBodyReader(
      Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    Iterator<MediaType> mediaTypeRange =
        MediaTypeHelper.createDescendingMediaTypeIterator(mediaType);
    Map<Class, MessageBodyReader> instanceCache = new HashMap<>();
    List<MessageBodyReader> matchedReaders = newArrayList();
    while (mediaTypeRange.hasNext()) {
      MediaType actual = mediaTypeRange.next();
      List<ObjectFactory<ProviderDescriptor>> messageBodyReaderFactories =
          readProviders.get(actual);
      if (messageBodyReaderFactories != null) {
        for (ObjectFactory messageBodyReaderFactory : messageBodyReaderFactories) {
          Class<?> messageBodyReaderClass =
              messageBodyReaderFactory.getObjectModel().getObjectClass();
          MessageBodyReader messageBodyReader = instanceCache.get(messageBodyReaderClass);
          if (messageBodyReader == null) {
            messageBodyReader =
                (MessageBodyReader)
                    messageBodyReaderFactory.getInstance(ApplicationContext.getCurrent());
            instanceCache.put(messageBodyReaderClass, messageBodyReader);
          }
          if (messageBodyReader.isReadable(type, genericType, annotations, actual)) {
            matchedReaders.add(messageBodyReader);
          }
        }
      }
    }
    if (matchedReaders.isEmpty()) {
      return null;
    }
    if (matchedReaders.size() > 1) {
      Collections.sort(
          matchedReaders,
          (readerOne, readerTwo) -> {
            Type typeOne = getTypeSupportedByReader(readerOne);
            Type typeTwo = getTypeSupportedByReader(readerTwo);
            if (!(typeOne instanceof Class) || !(typeTwo instanceof Class)) {
              return 0;
            }
            int inheritanceDepthOne = calculateInheritanceDepth((Class<?>) typeOne, type);
            int inheritanceDepthTwo = calculateInheritanceDepth((Class<?>) typeTwo, type);
            if (inheritanceDepthOne < 0 && inheritanceDepthTwo >= 0) {
              return 1;
            } else if (inheritanceDepthOne >= 0 && inheritanceDepthTwo < 0) {
              return -1;
            } else if (inheritanceDepthOne > inheritanceDepthTwo) {
              return 1;
            } else if (inheritanceDepthOne < inheritanceDepthTwo) {
              return -1;
            }
            return 0;
          });
    }
    return matchedReaders.get(0);
  }

  private static Type getTypeSupportedByReader(MessageBodyReader<?> reader) {
    Class readerSuperClass = reader.getClass();
    while (readerSuperClass != null) {
      for (Type anInterface : readerSuperClass.getGenericInterfaces()) {
        if (anInterface instanceof ParameterizedType) {
          ParameterizedType parameterizedType = (ParameterizedType) anInterface;
          if (parameterizedType.getRawType() == MessageBodyReader.class) {
            return parameterizedType.getActualTypeArguments()[0];
          }
        }
      }
      readerSuperClass = readerSuperClass.getSuperclass();
    }
    return null;
  }

  /**
   * Looking for message body writer according to supplied entity class, entity generic type,
   * annotations and content type.
   *
   * @param <T> message body writer actual type argument
   * @param type entity type
   * @param genericType entity generic type
   * @param annotations annotations
   * @param mediaType content type in which entity should be represented
   * @return message body writer or null if no one was found.
   */
  @SuppressWarnings({"unchecked"})
  protected <T> MessageBodyWriter<T> doGetMessageBodyWriter(
      Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    Iterator<MediaType> mediaTypeRange =
        MediaTypeHelper.createDescendingMediaTypeIterator(mediaType);
    Map<Class, MessageBodyWriter> instanceCache = new HashMap<>();
    List<MessageBodyWriter> matchedWriters = newArrayList();
    while (mediaTypeRange.hasNext()) {
      MediaType actual = mediaTypeRange.next();
      List<ObjectFactory<ProviderDescriptor>> messageBodyWriterFactories =
          writeProviders.get(actual);
      if (messageBodyWriterFactories != null) {
        for (ObjectFactory messageBodyWriterFactory : messageBodyWriterFactories) {
          Class<?> messageBodyWriterClass =
              messageBodyWriterFactory.getObjectModel().getObjectClass();
          MessageBodyWriter writer = instanceCache.get(messageBodyWriterClass);
          if (writer == null) {
            writer =
                (MessageBodyWriter)
                    messageBodyWriterFactory.getInstance(ApplicationContext.getCurrent());
            instanceCache.put(messageBodyWriterClass, writer);
          }
          if (writer.isWriteable(type, genericType, annotations, actual)) {
            matchedWriters.add(writer);
          }
        }
      }
    }
    if (matchedWriters.isEmpty()) {
      return null;
    }
    if (matchedWriters.size() > 1) {
      Collections.sort(
          matchedWriters,
          (writerOne, writerTwo) -> {
            Type typeOne = getTypeSupportedByWriter(writerOne);
            Type typeTwo = getTypeSupportedByWriter(writerTwo);
            if (!(typeOne instanceof Class) || !(typeTwo instanceof Class)) {
              return 0;
            }
            int inheritanceDepthOne = calculateInheritanceDepth((Class<?>) typeOne, type);
            int inheritanceDepthTwo = calculateInheritanceDepth((Class<?>) typeTwo, type);
            if (inheritanceDepthOne < 0 && inheritanceDepthTwo >= 0) {
              return 1;
            } else if (inheritanceDepthOne >= 0 && inheritanceDepthTwo < 0) {
              return -1;
            } else if (inheritanceDepthOne > inheritanceDepthTwo) {
              return 1;
            } else if (inheritanceDepthOne < inheritanceDepthTwo) {
              return -1;
            }
            return 0;
          });
    }
    return matchedWriters.get(0);
  }

  private static Type getTypeSupportedByWriter(MessageBodyWriter writer) {
    Class writerSuperClass = writer.getClass();
    while (writerSuperClass != null) {
      for (Type anInterface : writerSuperClass.getGenericInterfaces()) {
        if (anInterface instanceof ParameterizedType) {
          ParameterizedType parameterizedType = (ParameterizedType) anInterface;
          if (parameterizedType.getRawType() == MessageBodyWriter.class) {
            return parameterizedType.getActualTypeArguments()[0];
          }
        }
      }
      writerSuperClass = writerSuperClass.getSuperclass();
    }
    return null;
  }

  private static int calculateInheritanceDepth(Class<?> inherited, Class<?> inheritor) {
    if (!inherited.isAssignableFrom(inheritor)) {
      return -1;
    }
    Class superClass = inheritor;
    int depth = 0;
    while (superClass != null && superClass != inherited) {
      superClass = superClass.getSuperclass();
      depth++;
    }
    return depth;
  }

  /**
   * @param path request path
   * @param filtersMap filter map
   * @return acceptable filter
   * @see #getMethodInvokerFilters(String)
   * @see #getRequestFilters(String)
   * @see #getResponseFilters(String)
   */
  protected List<ObjectFactory<FilterDescriptor>> doGetMatchedFilters(
      String path, Map<UriPattern, List<ObjectFactory<FilterDescriptor>>> filtersMap) {
    if (path == null) {
      path = FilterDescriptorImpl.DEFAULT_PATH;
    }
    List<ObjectFactory<FilterDescriptor>> result = new ArrayList<>();

    List<String> capturingValues = new ArrayList<>();
    for (Map.Entry<UriPattern, List<ObjectFactory<FilterDescriptor>>> e : filtersMap.entrySet()) {
      UriPattern uriPattern = e.getKey();

      if (uriPattern != null) {
        if (uriPattern.match(path, capturingValues)) {
          String last = Iterables.getLast(capturingValues);
          if (last != null && !"/".equals(last)) {
            continue;
          }
        } else {
          continue;
        }
      }
      result.addAll(e.getValue());
    }
    return result;
  }

  /** Add prepared providers. */
  protected void init() {
    ByteEntityProvider byteArrayEntityProvider = new ByteEntityProvider();
    addMessageBodyReader(byteArrayEntityProvider);
    addMessageBodyWriter(byteArrayEntityProvider);

    DataSourceEntityProvider dataSourceEntityProvider = new DataSourceEntityProvider();
    addMessageBodyReader(dataSourceEntityProvider);
    addMessageBodyWriter(dataSourceEntityProvider);

    DOMSourceEntityProvider domSourceEntityProvider = new DOMSourceEntityProvider();
    addMessageBodyReader(domSourceEntityProvider);
    addMessageBodyWriter(domSourceEntityProvider);

    FileEntityProvider fileEntityProvider = new FileEntityProvider();
    addMessageBodyReader(fileEntityProvider);
    addMessageBodyWriter(fileEntityProvider);

    addMessageBodyReader(MultivaluedMapEntityProvider.class);
    addMessageBodyWriter(MultivaluedMapEntityProvider.class);

    InputStreamEntityProvider inputStreamEntityProvider = new InputStreamEntityProvider();
    addMessageBodyReader(inputStreamEntityProvider);
    addMessageBodyWriter(inputStreamEntityProvider);

    ReaderEntityProvider readerEntityProvider = new ReaderEntityProvider();
    addMessageBodyReader(readerEntityProvider);
    addMessageBodyWriter(readerEntityProvider);

    SAXSourceEntityProvider saxSourceEntityProvider = new SAXSourceEntityProvider();
    addMessageBodyReader(saxSourceEntityProvider);
    addMessageBodyWriter(saxSourceEntityProvider);

    StreamSourceEntityProvider streamSourceEntityProvider = new StreamSourceEntityProvider();
    addMessageBodyReader(streamSourceEntityProvider);
    addMessageBodyWriter(streamSourceEntityProvider);

    StringEntityProvider stringEntityProvider = new StringEntityProvider();
    addMessageBodyReader(stringEntityProvider);
    addMessageBodyWriter(stringEntityProvider);

    StreamOutputEntityProvider streamOutputEntityProvider = new StreamOutputEntityProvider();
    addMessageBodyReader(streamOutputEntityProvider);
    addMessageBodyWriter(streamOutputEntityProvider);

    JsonEntityProvider<Object> jsonEntityProvider = new JsonEntityProvider<>();
    addMessageBodyReader(jsonEntityProvider);
    addMessageBodyWriter(jsonEntityProvider);

    addMessageBodyReader(JAXBElementEntityProvider.class);
    addMessageBodyWriter(JAXBElementEntityProvider.class);

    addMessageBodyReader(JAXBObjectEntityProvider.class);
    addMessageBodyWriter(JAXBObjectEntityProvider.class);

    addMessageBodyReader(MultipartFormDataEntityProvider.class);

    addMessageBodyReader(ListMultipartFormDataMessageBodyReader.class);
    addMessageBodyReader(MapMultipartFormDataMessageBodyReader.class);
    addMessageBodyWriter(CollectionMultipartFormDataMessageBodyWriter.class);

    addContextResolver(new JAXBContextResolver());

    addExceptionMapper(new DefaultExceptionMapper());
  }
}
