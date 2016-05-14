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
package org.everrest.core.impl.resource;

import com.google.common.base.MoreObjects;

import org.everrest.core.BaseObjectModel;
import org.everrest.core.Parameter;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.method.MethodParameter;
import org.everrest.core.resource.ResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.util.ResourceMethodComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.everrest.core.impl.header.MediaTypeHelper.WADL_TYPE;
import static org.everrest.core.impl.method.ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS;

/**
 * @author andrew00x
 */
public class AbstractResourceDescriptor extends BaseObjectModel implements ResourceDescriptor {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractResourceDescriptor.class);

    /** PathValue. */
    private final PathValue path;
    /** UriPattern. */
    private final UriPattern uriPattern;
    /** Sub-resource methods. Sub-resource method has path annotation. */
    private final TreeMap<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> subResourceMethods;
    /** Sub-resource locators. Sub-resource locator has path annotation. */
    private final TreeMap<UriPattern, SubResourceLocatorDescriptor> subResourceLocators;
    /** Resource methods. Resource method has not own path annotation. */
    private final MultivaluedMap<String, ResourceMethodDescriptor> resourceMethods;
    private final ResourceMethodComparator resourceMethodComparator = new ResourceMethodComparator();

    /**
     * Constructs new instance of AbstractResourceDescriptor.
     *
     * @param resourceClass
     *         resource class
     */
    public AbstractResourceDescriptor(Class<?> resourceClass) {
        this(PathValue.getPath(resourceClass.getAnnotation(Path.class)), resourceClass);
    }

    public AbstractResourceDescriptor(String path, Class<?> resourceClass) {
        super(resourceClass);
        if (path == null) {
            this.path = null;
            this.uriPattern = null;
        } else {
            this.path = new PathValue(path);
            this.uriPattern = new UriPattern(path);
        }
        this.resourceMethods = new MultivaluedHashMap<>();
        this.subResourceMethods = new TreeMap<>(UriPattern.URIPATTERN_COMPARATOR);
        this.subResourceLocators = new TreeMap<>(UriPattern.URIPATTERN_COMPARATOR);
        processMethods();
    }

    /**
     * Constructs new instance of AbstractResourceDescriptor.
     *
     * @param resource
     *         resource
     */
    public AbstractResourceDescriptor(Object resource) {
        this(resource.getClass());
    }

    public AbstractResourceDescriptor(String path, Object resource) {
        this(path, resource.getClass());
    }


    @Override
    public PathValue getPathValue() {
        return path;
    }


    @Override
    public Map<String, List<ResourceMethodDescriptor>> getResourceMethods() {
        return resourceMethods;
    }


    @Override
    public Map<UriPattern, SubResourceLocatorDescriptor> getSubResourceLocators() {
        return subResourceLocators;
    }


    @Override
    public Map<UriPattern, Map<String, List<SubResourceMethodDescriptor>>> getSubResourceMethods() {
        return subResourceMethods;
    }


    @Override
    public UriPattern getUriPattern() {
        return uriPattern;
    }


    @Override
    public boolean isRootResource() {
        return path != null;
    }

    /**
     * Process method of resource and separate them to three types Resource Methods, Sub-Resource Methods and
     * Sub-Resource Locators.
     */
    private void processMethods() {
        Class<?> resourceClass = getObjectClass();

        for (Method method : getAllMethods(resourceClass)) {
            Path subPath = getMethodAnnotation(method, resourceClass, Path.class, false);
            HttpMethod httpMethod = getMethodAnnotation(method, resourceClass, HttpMethod.class, true);

            if (subPath != null || httpMethod != null) {
                if (Modifier.isPublic(method.getModifiers())) {
                    List<Parameter> methodParameters = createMethodParameters(resourceClass, method);

                    Annotation securityAnnotation = getSecurityAnnotation(method, resourceClass);
                    Annotation[] additionalAnnotations = securityAnnotation != null ? new Annotation[]{securityAnnotation} : new Annotation[0];

                    if (httpMethod != null) {
                        Produces producesAnnotation = getMethodAnnotation(method, resourceClass, Produces.class, false);
                        if (producesAnnotation == null) {
                            producesAnnotation = getClassAnnotation(resourceClass, Produces.class);
                        }
                        List<MediaType> produces = MediaTypeHelper.createProducesList(producesAnnotation);

                        Consumes consumesAnnotation = getMethodAnnotation(method, resourceClass, Consumes.class, false);
                        if (consumesAnnotation == null) {
                            consumesAnnotation = getClassAnnotation(resourceClass, Consumes.class);
                        }
                        List<MediaType> consumes = MediaTypeHelper.createConsumesList(consumesAnnotation);

                        if (subPath == null) {
                            addResourceMethod(method, httpMethod, methodParameters, additionalAnnotations, produces, consumes);
                        } else {
                            addSubResourceMethod(method, subPath, httpMethod, methodParameters, additionalAnnotations, produces, consumes);
                        }
                    } else {
                        addSubResourceLocator(method, subPath, methodParameters, additionalAnnotations);
                    }
                } else {
                    LOG.warn("Non-public method {} in {} annotated with @Path of HTTP method annotation, it's ignored", method.getName(), clazz.getName());
                }
            }
        }
        if (resourceMethods.size() + subResourceMethods.size() + subResourceLocators.size() == 0) {
            LOG.warn("Not found any resource methods, sub-resource methods or sub-resource locators in {}", resourceClass.getName());
        }

        // End method processing. Start HEAD and OPTIONS resolving, see JAX-RS (JSR-311) specification section 3.3.5
        resolveHeadRequest();
        resolveOptionsRequest();

        sortResourceMethods();
        sortSubResourceMethods();
    }

    private List<Method> getAllMethods(Class<?> resourceClass) {
        List<Method> methods = new ArrayList<>();
        Class<?> superclass = resourceClass;
        while (superclass != null && superclass != Object.class) {
            Collections.addAll(methods, superclass.getDeclaredMethods());
            superclass = superclass.getSuperclass();
        }
        return methods;
    }

    private void addResourceMethod(Method method, HttpMethod httpMethod, List<Parameter> params, Annotation[] additional, List<MediaType> produces, List<MediaType> consumes) {
        ResourceMethodDescriptor resourceMethod = new ResourceMethodDescriptorImpl(method, httpMethod.value(), params, this, consumes, produces, additional);
        validateResourceMethod(resourceMethod);
        ResourceMethodDescriptor existedResourceMethod = findMethodResourceMediaType(getResourceMethods(httpMethod.value()), resourceMethod.consumes(), resourceMethod.produces());
        if (existedResourceMethod != null) {
            throw new RuntimeException(String.format("Two resource method %s and %s with the same HTTP method, consumes and produces found", resourceMethod, existedResourceMethod));
        }
        resourceMethods.add(httpMethod.value(), resourceMethod);
    }

    private void addSubResourceMethod(Method method, Path subPath, HttpMethod httpMethod, List<Parameter> params, Annotation[] additional, List<MediaType> produces, List<MediaType> consumes) {
        SubResourceMethodDescriptor subResourceMethod = new SubResourceMethodDescriptorImpl(new PathValue(subPath.value()), method, httpMethod.value(), params, this, consumes, produces, additional);
        validateResourceMethod(subResourceMethod);

        Map<String, List<SubResourceMethodDescriptor>> subResourceMethods = getSubResourceMethods(subResourceMethod.getUriPattern());

        SubResourceMethodDescriptor existedSubResourceMethod = (SubResourceMethodDescriptor)findMethodResourceMediaType(subResourceMethods.get(httpMethod.value()), subResourceMethod.consumes(), subResourceMethod.produces());
        if (existedSubResourceMethod != null) {
            throw new RuntimeException(String.format("Two sub-resource method %s and %s with the same HTTP method, path, consumes and produces found", subResourceMethod, existedSubResourceMethod));
        }
        List<SubResourceMethodDescriptor> methodList = subResourceMethods.get(httpMethod.value());
        if (methodList == null) {
            methodList = new ArrayList<>();
            subResourceMethods.put(httpMethod.value(), methodList);
        }
        methodList.add(subResourceMethod);
    }

    private void validateResourceMethod(ResourceMethodDescriptor resourceMethod) {
        List<Parameter> methodParameters = resourceMethod.getMethodParameters();
        int numberOfEntityParameters = (int)methodParameters.stream().filter(parameter -> parameter.getAnnotation() == null).count();
        if (numberOfEntityParameters > 1) {
            throw new RuntimeException(String.format("Method %s has %d parameters that are not annotated with JAX-RS parameter annotations, but must not have more than one",
                                                     resourceMethod.getMethod().getName(), numberOfEntityParameters));
        }
        boolean isAnyParameterAnnotatedWithFormParam = methodParameters.stream().anyMatch(parameter -> parameter.getAnnotation() != null &&
                                                                                          parameter.getAnnotation().annotationType() == FormParam.class);
        if (isAnyParameterAnnotatedWithFormParam && numberOfEntityParameters == 1) {
            boolean entityParameterIsMultivaluedMap = false;
            Parameter entityParameter = methodParameters.stream().filter(parameter -> parameter.getAnnotation() == null).findFirst().get();
            if (entityParameter.getParameterClass() == MultivaluedMap.class && entityParameter.getGenericType() instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType)entityParameter.getGenericType()).getActualTypeArguments();
                if (actualTypeArguments.length == 2 && String.class == actualTypeArguments[0] && String.class == actualTypeArguments[1]) {
                    entityParameterIsMultivaluedMap = true;
                }
            }
            if (!entityParameterIsMultivaluedMap) {
                throw new RuntimeException("At least one method's parameter is annotated with FormParam, entity parameter might not be other than MultivaluedMap<String, String>");
            }
        }
    }

    private void addSubResourceLocator(Method method, Path subPath, List<Parameter> params, Annotation[] additional) {
        SubResourceLocatorDescriptor resourceLocator = new SubResourceLocatorDescriptorImpl(new PathValue(subPath.value()), method, params, this, additional);
        validateSubResourceLocator(resourceLocator);
        if (subResourceLocators.containsKey(resourceLocator.getUriPattern())) {
            throw new RuntimeException(String.format("Two sub-resource locators %s and %s with the same path found", resourceLocator, subResourceLocators.get(resourceLocator.getUriPattern())));
        }
        subResourceLocators.put(resourceLocator.getUriPattern(), resourceLocator);
    }

    private void validateSubResourceLocator(SubResourceLocatorDescriptor resourceLocator) {
        List<Parameter> methodParameters = resourceLocator.getMethodParameters();
        boolean hasEntityParameter = methodParameters.stream().anyMatch(parameter -> parameter.getAnnotation() == null);
        if (hasEntityParameter) {
            throw new RuntimeException(String.format("Method %s is resource locator, it must not have not JAX-RS annotated (entity) parameters",
                                                     resourceLocator.getMethod().getName()));
        }
    }

    private void sortResourceMethods() {
        for (List<ResourceMethodDescriptor> resourceMethods : this.resourceMethods.values()) {
            Collections.sort(resourceMethods, resourceMethodComparator);
        }
    }

    private List<ResourceMethodDescriptor> getResourceMethods(String httpMethod) {
        List<ResourceMethodDescriptor> methodDescriptors = resourceMethods.get(httpMethod);
        if (methodDescriptors == null) {
            methodDescriptors = new ArrayList<>();
            resourceMethods.put(httpMethod, methodDescriptors);
        }
        return methodDescriptors;
    }

    private Map<String, List<SubResourceMethodDescriptor>> getSubResourceMethods(UriPattern subResourceUriPattern) {
        Map<String, List<SubResourceMethodDescriptor>> map = subResourceMethods.get(subResourceUriPattern);
        if (map == null) {
            map = new MultivaluedHashMap<>();
            subResourceMethods.put(subResourceUriPattern, map);
        }
        return map;
    }

    private void sortSubResourceMethods() {
        for (Map<String, List<SubResourceMethodDescriptor>> subResourceMethods : this.subResourceMethods.values()) {
            for (List<SubResourceMethodDescriptor> resourceMethods : subResourceMethods.values()) {
                Collections.sort(resourceMethods, resourceMethodComparator);
            }
        }
    }

    /**
     * Create list of {@link Parameter} .
     *
     * @param resourceClass
     *         class
     * @param method
     *         See {@link java.lang.reflect.Method}
     * @return list of {@link Parameter}
     */
    private List<Parameter> createMethodParameters(Class<?> resourceClass, Method method) {
        Class<?>[] parameterClasses = method.getParameterTypes();
        if (parameterClasses.length > 0) {
            Type[] parameterGenTypes = method.getGenericParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();

            List<Parameter> methodParameters = new ArrayList<>(parameterClasses.length);
            boolean classEncoded = getClassAnnotation(resourceClass, Encoded.class) != null;
            boolean methodEncoded = getMethodAnnotation(method, resourceClass, Encoded.class, false) != null;
            for (int i = 0; i < parameterClasses.length; i++) {
                String defaultValue = null;
                Annotation parameterAnnotation = null;
                boolean encoded = false;

                for (int j = 0; j < annotations[i].length; j++) {
                    Annotation annotation = annotations[i][j];
                    Class<?> annotationType = annotation.annotationType();
                    if (RESOURCE_METHOD_PARAMETER_ANNOTATIONS.contains(annotationType.getName())) {
                        if (parameterAnnotation != null) {
                            String msg = String.format(
                                    "JAX-RS annotations on one of method parameters of resource %s, method %s are equivocality. Annotations: %s and %s can't be applied to one parameter",
                                    toString(), method.getName(), parameterAnnotation, annotation);
                            throw new RuntimeException(msg);
                        }
                        parameterAnnotation = annotation;
                    } else if (annotationType == Encoded.class) {
                        encoded = true;
                    } else if (annotationType == DefaultValue.class) {
                        defaultValue = ((DefaultValue)annotation).value();
                    }
                }

                Parameter methodParameter = new MethodParameter(
                        parameterAnnotation,
                        annotations[i],
                        parameterClasses[i],
                        parameterGenTypes[i],
                        defaultValue,
                        encoded || methodEncoded || classEncoded);
                methodParameters.add(methodParameter);
            }

            return methodParameters;
        }

        return Collections.emptyList();
    }

    /**
     * According to JSR-311:
     * <p>
     * On receipt of a HEAD request an implementation MUST either: 1. Call method annotated with request method
     * designation for HEAD or, if none present, 2. Call method annotated with a request method designation GET and
     * discard any returned entity.
     * </p>
     */
    private void resolveHeadRequest() {
        List<ResourceMethodDescriptor> getResources = resourceMethods.get(HttpMethod.GET);
        if (getResources != null && getResources.size() > 0) {
            List<ResourceMethodDescriptor> headResources = getResourceMethods(HttpMethod.HEAD);
            for (ResourceMethodDescriptor resourceMethod : getResources) {
                if (findMethodResourceMediaType(headResources, resourceMethod.consumes(), resourceMethod.produces()) == null) {
                    headResources.add(
                            new ResourceMethodDescriptorImpl(resourceMethod.getMethod(), HttpMethod.HEAD,
                                                             resourceMethod.getMethodParameters(), this, resourceMethod.consumes(),
                                                             resourceMethod.produces(),
                                                             resourceMethod.getAnnotations()));
                }
            }
        }

        for (Map<String, List<SubResourceMethodDescriptor>> allSubResourceMethods : subResourceMethods.values()) {
            List<SubResourceMethodDescriptor> getSubResources = allSubResourceMethods.get(HttpMethod.GET);
            if (getSubResources != null && getSubResources.size() > 0) {
                List<SubResourceMethodDescriptor> headSubResources = allSubResourceMethods.get(HttpMethod.HEAD);
                if (headSubResources == null) {
                    headSubResources = new ArrayList<>();
                    allSubResourceMethods.put(HttpMethod.HEAD, headSubResources);
                }
                for (SubResourceMethodDescriptor subResourceMethod : getSubResources) {
                    if (findMethodResourceMediaType(headSubResources, subResourceMethod.consumes(), subResourceMethod.produces()) == null) {
                        headSubResources.add(
                                new SubResourceMethodDescriptorImpl(subResourceMethod.getPathValue(), subResourceMethod.getMethod(),
                                                                    HttpMethod.HEAD, subResourceMethod.getMethodParameters(), this,
                                                                    subResourceMethod.consumes(),
                                                                    subResourceMethod.produces(), subResourceMethod.getAnnotations()));
                    }
                }
            }
        }
    }

    /**
     * According to JSR-311:
     * <p>
     * On receipt of a OPTIONS request an implementation MUST either: 1. Call method annotated with request method
     * designation for OPTIONS or, if none present, 2. Generate an automatic response using the metadata provided by the
     * JAX-RS annotations on the matching class and its methods.
     * </p>
     */
    private void resolveOptionsRequest() {
        List<ResourceMethodDescriptor> optionResources = getResourceMethods(HttpMethod.OPTIONS);
        if (optionResources.isEmpty()) {
            List<Parameter> methodParameters = Collections.emptyList();
            List<MediaType> consumes = Collections.singletonList(WILDCARD_TYPE);
            List<MediaType> produces = Collections.singletonList(WADL_TYPE);
            optionResources.add(new OptionsRequestResourceMethodDescriptorImpl("OPTIONS", methodParameters, this, consumes, produces, new Annotation[0]));
        }
    }

    /**
     * Get all method with at least one annotation which has annotation <i>annotation</i>. It is useful for annotation
     * {@link javax.ws.rs.GET}, etc. All HTTP method annotations has annotation {@link javax.ws.rs.HttpMethod}.
     *
     * @param <T>
     *         annotation type
     * @param method
     *         method
     * @param annotationClass
     *         annotation class
     * @return list of annotation
     */
    private <T extends Annotation> T getMetaAnnotation(Method method, Class<T> annotationClass) {
        for (Annotation annotation : method.getAnnotations()) {
            T result;
            if ((result = annotation.annotationType().getAnnotation(annotationClass)) != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Tries to get JAX-RS annotation on method from the resource class's superclasses or implemented interfaces.
     *
     * @param <T>
     *         annotation type
     * @param method
     *         method for discovering
     * @param resourceClass
     *         class that contains discovered method
     * @param annotationClass
     *         annotation type what we are looking for
     * @param metaAnnotation
     *         false if annotation should be on method and true in method should contain annotations that
     *         has supplied annotation
     * @return annotation from class or its ancestor or null if nothing found
     */
    private <T extends Annotation> T getMethodAnnotation(Method method,
                                                         Class<?> resourceClass,
                                                         Class<T> annotationClass,
                                                         boolean metaAnnotation) {
        T annotation = metaAnnotation ? getMetaAnnotation(method, annotationClass) : method.getAnnotation(annotationClass);

        if (annotation == null) {
            Method myMethod;
            Class<?> myClass = resourceClass;
            while (annotation == null && myClass != null && myClass != Object.class) {
                for (Class<?> anInterface : myClass.getInterfaces()) {
                    try {
                        myMethod = anInterface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        T newAnnotation = metaAnnotation ? getMetaAnnotation(myMethod, annotationClass) : myMethod.getAnnotation(annotationClass);
                        if (annotation == null) {
                            annotation = newAnnotation;
                        } else {
                            throw new RuntimeException(String.format("Conflicts of JAX-RS annotations on method %s of resource %s. " +
                                                                     "Method is declared in more than one interface and different interfaces contains JAX-RS annotations.",
                                                                     myMethod.getName(), resourceClass.getName()));
                        }
                    } catch (NoSuchMethodException ignored) {
                    }
                }
                if (annotation == null) {
                    myClass = myClass.getSuperclass();
                    if (myClass != null && myClass != Object.class) {
                        try {
                            myMethod = myClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                            annotation = metaAnnotation ? getMetaAnnotation(myMethod, annotationClass) : myMethod.getAnnotation(annotationClass);
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            }
        }

        return annotation;
    }

    /** Tries to get JAX-RS annotation on class, superclasses or implemented interfaces. */
    private <T extends Annotation> T getClassAnnotation(Class<?> resourceClass, Class<T> annotationClass) {
        T annotation = resourceClass.getAnnotation(annotationClass);
        if (annotation == null) {
            Class<?> myClass = resourceClass;
            while (annotation == null && myClass != null && myClass != Object.class) {
                for (Class<?> anInterface : myClass.getInterfaces()) {
                    T newAnnotation = anInterface.getAnnotation(annotationClass);
                    if (annotation == null) {
                        annotation = newAnnotation;
                    } else {
                        throw new RuntimeException(String.format("Conflict of JAX-RS annotation on class %s. " +
                                                                 "Class implements more that one interface and few interfaces have JAX-RS annotations.", resourceClass.getName()));
                    }
                }
                if (annotation == null) {
                    myClass = myClass.getSuperclass();
                    if (myClass != null && myClass != Object.class) {
                        annotation = myClass.getAnnotation(annotationClass);
                    }
                }
            }
        }
        return annotation;
    }

    /**
     * Check is collection of {@link org.everrest.core.resource.ResourceMethodDescriptor} already contains ResourceMethodDescriptor with
     * the same media types.
     *
     * @param resourceMethods
     *         {@link java.util.Set} of {@link org.everrest.core.resource.ResourceMethodDescriptor}
     * @param consumes
     *         resource method consumed media type
     * @param produces
     *         resource method produced media type
     * @return ResourceMethodDescriptor or null if nothing found
     */
    private <T extends ResourceMethodDescriptor> ResourceMethodDescriptor findMethodResourceMediaType(List<T> resourceMethods,
                                                                                                      List<MediaType> consumes,
                                                                                                      List<MediaType> produces) {
        if (resourceMethods == null || resourceMethods.isEmpty()) {
            return null;
        }
        ResourceMethodDescriptor matched = null;
        for (Iterator<T> iterator = resourceMethods.iterator(); matched == null && iterator.hasNext(); ) {
            T method = iterator.next();

            if (method.consumes().size() != consumes.size() || method.produces().size() != produces.size()) {
                continue;
            }

            if (method.consumes().containsAll(consumes) && method.produces().containsAll(produces)) {
                matched = method;
            }
        }
        return matched;
    }

    /**
     * Get security annotation (DenyAll, RolesAllowed, PermitAll) from <code>method</code> or class
     * <code>clazz</class> which contains method.
     * Supper class or implemented interfaces will be also checked. Annotation
     * on method has the advantage on annotation on class or interface.
     *
     * @param method
     *         method to be checked for security annotation
     * @param clazz
     *         class which contains <code>method</code>
     * @return one of security annotation or <code>null</code> is no such annotation found
     * @see javax.annotation.security.DenyAll
     * @see javax.annotation.security.RolesAllowed
     * @see javax.annotation.security.PermitAll
     */
    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getSecurityAnnotation(Method method, Class<?> clazz) {
        Class<T>[] securityAnnotationClassesClasses = new Class[]{DenyAll.class, RolesAllowed.class, PermitAll.class};
        T annotation = getAnnotation(method, securityAnnotationClassesClasses);
        if (annotation == null) {
            annotation = getAnnotation(clazz, securityAnnotationClassesClasses);
            if (annotation == null) {
                Method myMethod;
                Class<?> myClass = clazz;
                while (annotation == null && myClass != null && myClass != Object.class) {
                    Class<?>[] interfaces = myClass.getInterfaces();
                    for (int i = 0; annotation == null && i < interfaces.length; i++) {
                        try {
                            myMethod = interfaces[i].getDeclaredMethod(method.getName(), method.getParameterTypes());
                            annotation = getAnnotation(myMethod, securityAnnotationClassesClasses);
                        } catch (NoSuchMethodException ignored) {
                        }
                        if (annotation == null) {
                            annotation = getAnnotation(interfaces[i], securityAnnotationClassesClasses);
                        }
                    }
                    if (annotation == null) {
                        myClass = myClass.getSuperclass();
                        if (myClass != null && myClass != Object.class) {
                            try {
                                myMethod = myClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                                annotation = getAnnotation(myMethod, securityAnnotationClassesClasses);
                            } catch (NoSuchMethodException ignored) {
                            }
                            if (annotation == null) {
                                annotation = getAnnotation(myClass, securityAnnotationClassesClasses);
                            }
                        }
                    }
                }
            }
        }
        return annotation;
    }

    private <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T>[] annotationClasses) {
        T annotation = null;
        for (int i = 0; annotation == null && i < annotationClasses.length; i++) {
            annotation = clazz.getAnnotation(annotationClasses[i]);
        }
        return annotation;
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T>[] annotationClasses) {
        T annotation = null;
        for (int i = 0; annotation == null && i < annotationClasses.length; i++) {
            annotation = method.getAnnotation(annotationClasses[i]);
        }
        return annotation;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                          .add("path", path)
                          .add("isRootResource", isRootResource())
                          .add("class", clazz)
                          .omitNullValues()
                          .toString();
    }

}
