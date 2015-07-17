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
package org.everrest.core.impl.resource;

import org.everrest.core.BaseObjectModel;
import org.everrest.core.impl.header.MediaTypeHelper;
import org.everrest.core.impl.method.MethodParameterImpl;
import org.everrest.core.impl.method.ParameterHelper;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceDescriptorVisitor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceLocatorMap;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceMethodMap;
import org.everrest.core.uri.UriPattern;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author andrew00x
 */
public class AbstractResourceDescriptorImpl extends BaseObjectModel implements AbstractResourceDescriptor {
    /** Logger. */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractResourceDescriptorImpl.class);

    /** PathValue. */
    private final PathValue path;

    /** UriPattern. */
    private final UriPattern uriPattern;

    /** Sub-resource methods. Sub-resource method has path annotation. */
    private final SubResourceMethodMap subResourceMethods;

    /** Sub-resource locators. Sub-resource locator has path annotation. */
    private final SubResourceLocatorMap subResourceLocators;

    /** Resource methods. Resource method has not own path annotation. */
    private final ResourceMethodMap<ResourceMethodDescriptor> resourceMethods;

    /**
     * Constructs new instance of AbstractResourceDescriptor.
     *
     * @param resourceClass
     *         resource class
     */
    public AbstractResourceDescriptorImpl(Class<?> resourceClass) {
        this(PathValue.getPath(resourceClass.getAnnotation(Path.class)), resourceClass);
    }

    public AbstractResourceDescriptorImpl(String path, Class<?> resourceClass) {
        super(resourceClass);
        if (path == null) {
            this.path = null;
            this.uriPattern = null;
        } else {
            this.path = new PathValue(path);
            this.uriPattern = new UriPattern(path);
        }
        this.resourceMethods = new ResourceMethodMap<>();
        this.subResourceMethods = new SubResourceMethodMap();
        this.subResourceLocators = new SubResourceLocatorMap();
        processMethods();
    }

    /**
     * Constructs new instance of AbstractResourceDescriptor.
     *
     * @param resource
     *         resource
     */
    public AbstractResourceDescriptorImpl(Object resource) {
        this(PathValue.getPath(resource.getClass().getAnnotation(Path.class)), resource);
    }

    public AbstractResourceDescriptorImpl(String path, Object resource) {
        super(resource);
        if (path == null) {
            this.path = null;
            this.uriPattern = null;
        } else {
            this.path = new PathValue(path);
            this.uriPattern = new UriPattern(path);
        }
        this.resourceMethods = new ResourceMethodMap<>();
        this.subResourceMethods = new SubResourceMethodMap();
        this.subResourceLocators = new SubResourceLocatorMap();
        processMethods();
    }


    @Override
    public void accept(ResourceDescriptorVisitor visitor) {
        visitor.visitAbstractResourceDescriptor(this);
    }


    @Override
    public PathValue getPathValue() {
        return path;
    }


    @Override
    public ResourceMethodMap<ResourceMethodDescriptor> getResourceMethods() {
        return resourceMethods;
    }


    @Override
    public SubResourceLocatorMap getSubResourceLocators() {
        return subResourceLocators;
    }


    @Override
    public SubResourceMethodMap getSubResourceMethods() {
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
    protected void processMethods() {
        Class<?> resourceClass = getObjectClass();

        for (Method method : resourceClass.getMethods()) {
            Path subPath = getMethodAnnotation(method, resourceClass, Path.class, false);
            HttpMethod httpMethod = getMethodAnnotation(method, resourceClass, HttpMethod.class, true);

            if (subPath != null || httpMethod != null) {
                List<MethodParameter> params = createMethodParametersList(resourceClass, method);

                // Need only one type annotation at the moment
                Annotation security = getSecurityAnnotation(method, resourceClass);
                Annotation[] additional = security != null ? new Annotation[]{security} : new Annotation[0];

                if (httpMethod != null) {
                    Produces p = getMethodAnnotation(method, resourceClass, Produces.class, false);
                    if (p == null) {
                        p = getClassAnnotation(resourceClass, Produces.class); // from resource class
                    }
                    List<MediaType> produces = MediaTypeHelper.createProducesList(p);

                    Consumes c = getMethodAnnotation(method, resourceClass, Consumes.class, false);
                    if (c == null) {
                        c = getClassAnnotation(resourceClass, Consumes.class); // from resource class
                    }
                    List<MediaType> consumes = MediaTypeHelper.createConsumesList(c);

                    if (subPath == null) {
                        // Resource method.

                        ResourceMethodDescriptor res = new ResourceMethodDescriptorImpl(method, httpMethod.value(), params,
                                                                                        this, consumes, produces, additional);
                        ResourceMethodDescriptor exist = findMethodResourceMediaType(
                                resourceMethods.getList(httpMethod.value()), res.consumes(), res.produces());
                        if (exist == null) {
                            resourceMethods.add(httpMethod.value(), res);
                        } else {
                            throw new RuntimeException("Two resource method " + res + " and " + exist
                                                       + " with the same HTTP method, consumes and produces found. ");
                        }
                    } else {
                        // Sub-resource method.

                        SubResourceMethodDescriptor subRes = new SubResourceMethodDescriptorImpl(new PathValue(subPath.value()),
                                                                                                 method, httpMethod.value(), params, this,
                                                                                                 consumes, produces, additional);

                        ResourceMethodMap<SubResourceMethodDescriptor> map = subResourceMethods.getMethodMap(subRes.getUriPattern());

                        SubResourceMethodDescriptor exist = (SubResourceMethodDescriptor)findMethodResourceMediaType(
                                map.getList(httpMethod.value()), subRes.consumes(), subRes.produces());
                        if (exist == null) {
                            map.add(httpMethod.value(), subRes);
                        } else {
                            throw new RuntimeException("Two sub-resource method " + subRes + " and " + exist
                                                       + " with the same HTTP method, path, consumes and produces found. ");
                        }
                    }
                } else {
                    if (subPath != null) {
                        // Sub-resource locator.

                        SubResourceLocatorDescriptor locator = new SubResourceLocatorDescriptorImpl(new PathValue(subPath.value()),
                                                                                                    method, params, this, additional);
                        if (!subResourceLocators.containsKey(locator.getUriPattern())) {
                            subResourceLocators.put(locator.getUriPattern(), locator);
                        } else {
                            throw new RuntimeException("Two sub-resource locators " + locator + " and "
                                                       + subResourceLocators.get(locator.getUriPattern()) + " with the same path found. ");
                        }
                    }
                }
            }
        }
        if (resourceMethods.size() + subResourceMethods.size() + subResourceLocators.size() == 0) {
            // Warn instead throw exception. Lets user resolve such situation.
            String msg = "Not found any resource methods, sub-resource methods or sub-resource locators in "
                         + resourceClass.getName();
            LOG.warn(msg);
        }

        // End method processing.
        // Start HEAD and OPTIONS resolving, see JAX-RS (JSR-311) specification section 3.3.5
        resolveHeadRequest();
        resolveOptionsRequest();

        resourceMethods.sort();
        subResourceMethods.sort();
        // sub-resource locators already sorted
    }

    /**
     * Create list of {@link org.everrest.core.method.MethodParameter} .
     *
     * @param resourceClass
     *         class
     * @param method
     *         See {@link java.lang.reflect.Method}
     * @return list of {@link org.everrest.core.method.MethodParameter}
     */
    protected List<MethodParameter> createMethodParametersList(Class<?> resourceClass, Method method) {
        Class<?>[] parameterClasses = method.getParameterTypes();
        if (parameterClasses.length > 0) {
            Type[] parameterGenTypes = method.getGenericParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();

            List<MethodParameter> params = new ArrayList<>(parameterClasses.length);
            boolean classEncoded = getClassAnnotation(resourceClass, Encoded.class) != null;
            boolean methodEncoded = getMethodAnnotation(method, resourceClass, Encoded.class, false) != null;
            for (int i = 0; i < parameterClasses.length; i++) {
                String defaultValue = null;
                Annotation annotation = null;
                boolean encoded = false;

                for (int j = 0; j < annotations[i].length; j++) {
                    Annotation a = annotations[i][j];
                    Class<?> aClass = a.annotationType();
                    if (ParameterHelper.RESOURCE_METHOD_PARAMETER_ANNOTATIONS.contains(aClass.getName())) {
                        if (annotation != null) {
                            String msg = "JAX-RS annotations on one of method parameters of resource " + toString() + ", method "
                                         + method.getName() + " are equivocality. " + "Annotations: " + annotation + " and " + a
                                         + " can't be applied to one parameter. ";
                            throw new RuntimeException(msg);
                        }
                        annotation = a;
                    } else if (aClass == Encoded.class) {
                        encoded = true;
                    } else if (aClass == DefaultValue.class) {
                        defaultValue = ((DefaultValue)a).value();
                    } else {
                        LOG.debug("Method parameter of resource " + toString() + ", method " + method.getName()
                                  + " contains unknown or not valid JAX-RS annotation " + a.toString() + ". It will be ignored.");
                    }
                }

                MethodParameter mp = new MethodParameterImpl(
                        annotation,
                        annotations[i],
                        parameterClasses[i],
                        parameterGenTypes[i],
                        defaultValue,
                        encoded || methodEncoded || classEncoded);
                params.add(mp);
            }

            return params;
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
    protected void resolveHeadRequest() {
        List<ResourceMethodDescriptor> getResources = resourceMethods.get(HttpMethod.GET);
        if (getResources != null && getResources.size() > 0) {
            List<ResourceMethodDescriptor> headResources = resourceMethods.getList(HttpMethod.HEAD);
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

        for (ResourceMethodMap<SubResourceMethodDescriptor> map : subResourceMethods.values()) {
            List<SubResourceMethodDescriptor> getSubResources = map.get(HttpMethod.GET);
            if (getSubResources != null && getSubResources.size() > 0) {
                List<SubResourceMethodDescriptor> headSubResources = map.getList(HttpMethod.HEAD);
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
    protected void resolveOptionsRequest() {
        List<ResourceMethodDescriptor> optionResources = resourceMethods.getList("OPTIONS");
        if (optionResources.size() == 0) {
            List<MethodParameter> mps = Collections.emptyList();
            List<MediaType> consumes = MediaTypeHelper.DEFAULT_TYPE_LIST;
            List<MediaType> produces = Collections.singletonList(MediaTypeHelper.WADL_TYPE);
            optionResources.add(new OptionsRequestResourceMethodDescriptorImpl(null, "OPTIONS", mps, this, consumes,
                                                                               produces, new Annotation[0]));
        }
    }

    /**
     * Get all method with at least one annotation which has annotation <i>annotation</i>. It is useful for annotation
     * {@link javax.ws.rs.GET}, etc. All HTTP method annotations has annotation {@link javax.ws.rs.HttpMethod}.
     *
     * @param <T>
     *         annotation type
     * @param m
     *         method
     * @param annotationClass
     *         annotation class
     * @return list of annotation
     */
    protected <T extends Annotation> T getMetaAnnotation(Method m, Class<T> annotationClass) {
        for (Annotation a : m.getAnnotations()) {
            T endPoint;
            if ((endPoint = a.annotationType().getAnnotation(annotationClass)) != null) {
                return endPoint;
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
    protected <T extends Annotation> T getMethodAnnotation(Method method,
                                                           Class<?> resourceClass,
                                                           Class<T> annotationClass,
                                                           boolean metaAnnotation) {
        T annotation = metaAnnotation ? getMetaAnnotation(method, annotationClass) : method.getAnnotation(annotationClass);

        if (annotation == null) {
            Method myMethod;
            Class<?> myClass = resourceClass;
            while (annotation == null && myClass != null && myClass != Object.class) {
                Class<?>[] interfaces = myClass.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    try {
                        myMethod = interfaces[i].getDeclaredMethod(method.getName(), method.getParameterTypes());
                        T tmp = metaAnnotation ? getMetaAnnotation(myMethod, annotationClass) : myMethod.getAnnotation(annotationClass);
                        if (annotation == null) {
                            annotation = tmp;
                        } else {
                            throw new RuntimeException("JAX-RS annotation on method " + myMethod.getName() + " of resource "
                                                       + toString() + " is equivocating.");
                        }
                    } catch (NoSuchMethodException ignored) {
                    }
                }
                if (annotation == null) {
                    myClass = myClass.getSuperclass();
                    if (myClass != null && myClass != Object.class) {
                        try {
                            myMethod = myClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                            annotation =
                                    metaAnnotation ? getMetaAnnotation(myMethod, annotationClass) : myMethod.getAnnotation(annotationClass);
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            }
        }

        return annotation;
    }

    /** Tries to get JAX-RS annotation on class, superclasses or implemented interfaces. */
    protected <T extends Annotation> T getClassAnnotation(Class<?> resourceClass, Class<T> annotationClass) {
        T annotation = resourceClass.getAnnotation(annotationClass);
        if (annotation == null) {
            Class<?> myClass = resourceClass;
            while (annotation == null && myClass != null && myClass != Object.class) {
                Class<?>[] interfaces = myClass.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    T tmp = interfaces[i].getAnnotation(annotationClass);
                    if (annotation == null) {
                        annotation = tmp;
                    } else {
                        throw new RuntimeException("JAX-RS annotation on class " + resourceClass.getName() + " of resource "
                                                   + toString() + " is equivocating.");
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
     * the
     * same
     * media types.
     *
     * @param resourceMethods
     *         {@link java.util.Set} of {@link org.everrest.core.resource.ResourceMethodDescriptor}
     * @param consumes
     *         resource method consumed media type
     * @param produces
     *         resource method produced media type
     * @return ResourceMethodDescriptor or null if nothing found
     */
    protected <T extends ResourceMethodDescriptor> ResourceMethodDescriptor findMethodResourceMediaType(List<T> resourceMethods,
                                                                                                        List<MediaType> consumes,
                                                                                                        List<MediaType> produces) {
        ResourceMethodDescriptor matched = null;
        for (Iterator<T> iterator = resourceMethods.iterator(); matched == null && iterator.hasNext(); ) {
            T method = iterator.next();

            if (method.consumes().size() != consumes.size() || method.produces().size() != produces.size()) {
                continue;
            }

            if (method.consumes().containsAll(consumes) && method.produces().containsAll(produces)) {
                matched = method; // matched resource method
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
        Class<T>[] aClasses = new Class[]{DenyAll.class, RolesAllowed.class, PermitAll.class};
        T annotation = getAnnotation(method, aClasses);
        if (annotation == null) {
            annotation = getAnnotation(clazz, aClasses);
            if (annotation == null) {
                Method myMethod;
                Class<?> myClass = clazz;
                while (annotation == null && myClass != null && myClass != Object.class) {
                    Class<?>[] interfaces = myClass.getInterfaces();
                    for (int i = 0; annotation == null && i < interfaces.length; i++) {
                        try {
                            myMethod = interfaces[i].getDeclaredMethod(method.getName(), method.getParameterTypes());
                            annotation = getAnnotation(myMethod, aClasses);
                        } catch (NoSuchMethodException ignored) {
                        }
                        if (annotation == null) {
                            annotation = getAnnotation(interfaces[i], aClasses);
                        }
                    }
                    if (annotation == null) {
                        myClass = myClass.getSuperclass();
                        if (myClass != null && myClass != Object.class) {
                            try {
                                myMethod = myClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                                annotation = getAnnotation(myMethod, aClasses);
                            } catch (NoSuchMethodException ignored) {
                            }
                            if (annotation == null) {
                                annotation = getAnnotation(myClass, aClasses);
                            }
                        }
                    }
                }
            }
        }
        return annotation;
    }

    private <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T>[] annotationClasses) {
        T a = null;
        for (int i = 0; a == null && i < annotationClasses.length; i++) {
            a = clazz.getAnnotation(annotationClasses[i]);
        }
        return a;
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T>[] annotationClasses) {
        T a = null;
        for (int i = 0; a == null && i < annotationClasses.length; i++) {
            a = method.getAnnotation(annotationClasses[i]);
        }
        return a;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ AbstractResourceDescriptorImpl: ");
        sb.append("path: ");
        sb.append(getPathValue());
        sb.append("; isRootResource: ");
        sb.append(isRootResource());
        sb.append("; class: ");
        sb.append(getObjectClass());
        sb.append(" ]");
        return sb.toString();
    }

}
