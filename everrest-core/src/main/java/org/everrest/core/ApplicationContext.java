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
package org.everrest.core;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.InternalException;
import org.everrest.core.impl.LifecycleComponent;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousMethodInvoker;
import org.everrest.core.impl.method.DefaultMethodInvoker;
import org.everrest.core.impl.method.MethodInvokerDecoratorFactory;
import org.everrest.core.impl.method.OptionsRequestMethodInvoker;
import org.everrest.core.impl.method.ParameterResolverFactory;
import org.everrest.core.impl.uri.UriComponent;
import org.everrest.core.method.MethodInvoker;
import org.everrest.core.resource.GenericResourceMethod;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.servlet.ServletContainerRequest;
import org.everrest.core.tools.SimplePrincipal;
import org.everrest.core.tools.SimpleSecurityContext;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.everrest.core.uri.UriPattern;
import org.everrest.core.wadl.WadlProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public class ApplicationContext implements UriInfo, InitialProperties, Lifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationContext.class);

    /** {@link ThreadLocal} ApplicationContext. */
    private static ThreadLocal<ApplicationContext> current = new ThreadLocal<>();

    /** @return current ApplicationContext. */
    public static ApplicationContext getCurrent() {
        return current.get();
    }

    /**
     * Set ApplicationContext for current thread.
     *
     * @param context
     *         the ApplicationContext.
     */
    public static void setCurrent(ApplicationContext context) {
        current.set(context);
    }

    /** See {@link GenericContainerRequest}. */
    private GenericContainerRequest        request;
    /** See {@link ContainerResponse}. */
    private GenericContainerResponse       response;
    /** Providers. */
    private ProviderBinder                 providers;
    private DependencySupplier             dependencySupplier;
    /** Values of template parameters. */
    private List<String>                   parameterValues;
    /** List of matched resources. */
    private List<Object>                   matchedResources;
    /** List of not decoded matched URIs. */
    private List<String>                   encodedMatchedURIs;
    /** List of decoded matched URIs. */
    private List<String>                   matchedURIs;
    /** Mutable runtime attributes. */
    private Map<String, Object>            attributes;
    /** Properties. */
    private Map<String, String>            properties;
    /** Absolute path, full requested URI without query string and fragment. */
    private URI                            absolutePath;
    /** Decoded relative path. */
    private String                         path;
    /** Not decoded relative path. */
    private String                         encodedPath;
    /** Not decoded path template parameters. */
    private MultivaluedMap<String, String> encodedPathParameters;
    /** Decoded path template parameters. */
    private MultivaluedMap<String, String> pathParameters;
    /** List of not decoded path segments. */
    private List<PathSegment>              encodedPathSegments;
    /** Decoded path segments. */
    private List<PathSegment>              pathSegments;
    /** Not decoded query parameters. */
    private MultivaluedMap<String, String> encodedQueryParameters;
    /** Decoded query parameters. */
    private MultivaluedMap<String, String> queryParameters;
    private SecurityContext                asynchronousSecurityContext;
    private Application                    application;
    private EverrestConfiguration          configuration;
    private MethodInvokerDecoratorFactory  methodInvokerDecoratorFactory;

    private ApplicationContext(ApplicationContextBuilder builder) {
        request = builder.request;
        response = builder.response;
        providers = builder.providers;
        properties = builder.properties;
        application = builder.application;
        configuration = builder.configuration;
        dependencySupplier = builder.dependencySupplier;
        methodInvokerDecoratorFactory = builder.methodInvokerDecoratorFactory;
        parameterValues = new ArrayList<>();
        matchedResources = new ArrayList<>();
        encodedMatchedURIs = new ArrayList<>();
        matchedURIs = new ArrayList<>();
    }

    /**
     * Add ancestor resource, according to JSR-311:
     * <p>
     * Entries are ordered according in reverse request URI matching order, with the root resource last.
     * </p>
     * So add each new resource at the begin of list.
     *
     * @param resource
     *         the resource e. g. resource class, sub-resource method or sub-resource locator.
     */
    public void addMatchedResource(Object resource) {
        matchedResources.add(0, resource);
    }

    /**
     * Add ancestor resource, according to JSR-311:
     * <p>
     * Entries are ordered in reverse request URI matching order, with the root resource URI last.
     * </p>
     * So add each new URI at the begin of list.
     *
     * @param uri
     *         the partial part of that matched to resource class, sub-resource method or sub-resource locator.
     */
    public void addMatchedURI(String uri) {
        encodedMatchedURIs.add(0, uri);
        matchedURIs.add(0, UriComponent.decode(uri, UriComponent.PATH_SEGMENT));
    }

    @Override
    public URI getAbsolutePath() {
        if (absolutePath == null) {
            absolutePath = getRequestUriBuilder().replaceQuery(null).fragment(null).build();
        }
        return absolutePath;
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    /** @return get mutable runtime attributes */
    public Map<String, Object> getAttributes() {
        return attributes == null ? attributes = new HashMap<>() : attributes;
    }

    @Override
    public URI getBaseUri() {
        return request.getBaseUri();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri(getBaseUri());
    }

    /** @return See {@link GenericContainerRequest} */
    public GenericContainerRequest getContainerRequest() {
        return request;
    }

    /** @return See {@link GenericContainerResponse} */
    public GenericContainerResponse getContainerResponse() {
        return response;
    }

    /** @return See {@link DependencySupplier} */
    public DependencySupplier getDependencySupplier() {
        return dependencySupplier;
    }

    /** @return See {@link HttpHeaders} */
    public HttpHeaders getHttpHeaders() {
        return request;
    }

    /** @return {@link InitialProperties} */
    public InitialProperties getInitialProperties() {
        return this;
    }

    @Override
    public List<Object> getMatchedResources() {
        return matchedResources;
    }

    @Override
    public URI resolve(URI uri) {
        return UriComponent.resolve(getBaseUri(), uri);
    }

    @Override
    public URI relativize(URI uri) {
        if (!uri.isAbsolute()) {
            uri = resolve(uri);
        }
        return getRequestUri().relativize(uri);
    }

    @Override
    public List<String> getMatchedURIs() {
        return getMatchedURIs(true);
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        return decode ? matchedURIs : encodedMatchedURIs;
    }

    /**
     * @param methodDescriptor
     *         method descriptor
     * @return invoker that must be used for processing methods
     */
    public MethodInvoker getMethodInvoker(GenericResourceMethod methodDescriptor) {
        String method = request.getMethod();
        if ("OPTIONS".equals(method) && methodDescriptor.getMethod() == null) {
            // GenericMethodResource.getMethod() always return null if method for
            // "OPTIONS" request was not described in source code of service. In
            // this case we provide mechanism for "fake" method invoking.
            return new OptionsRequestMethodInvoker(new WadlProcessor());
        }
        MethodInvoker invoker = null;
        // Never use AsynchronousMethodInvoker for process SubResourceLocatorDescriptor.
        // Locators can't be processed in asynchronous mode since it is not end point of request.
        if (isAsynchronous() && methodDescriptor instanceof ResourceMethodDescriptor) {
            ContextResolver<AsynchronousJobPool> asyncJobsResolver = getProviders().getContextResolver(AsynchronousJobPool.class, null);
            if (asyncJobsResolver == null) {
                throw new IllegalStateException("Asynchronous jobs feature is not configured properly. ");
            }
            invoker = new AsynchronousMethodInvoker(asyncJobsResolver.getContext(null), new ParameterResolverFactory());
        }
        if (invoker == null) {
            invoker = new DefaultMethodInvoker(new ParameterResolverFactory());
        }
        if (methodInvokerDecoratorFactory != null) {
            return methodInvokerDecoratorFactory.makeDecorator(invoker);
        }
        return invoker;
    }

    /**
     * Should be used to pass template values in context by using returned list in matching to @see
     * {@link UriPattern#match(String, List)}. List will be cleared during matching.
     *
     * @return the list for template values
     */
    public List<String> getParameterValues() {
        return parameterValues;
    }

    @Override
    public String getPath() {
        return getPath(true);
    }

    @Override
    public String getPath(boolean decode) {
        if (encodedPath == null) {
            encodedPath = getAbsolutePath().getRawPath().substring(getBaseUri().getRawPath().length());
        }
        if (decode) {
            if (path == null) {
                path = UriComponent.decode(encodedPath, UriComponent.PATH);
            }
            return path;
        }
        return encodedPath;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        if (encodedPathParameters == null) {
            throw new IllegalStateException("Path template variables not initialized yet.");
        }
        if (decode) {
            if (pathParameters == null) {
                pathParameters = new MultivaluedMapImpl();
            }
            if (pathParameters.size() != encodedPathParameters.size()) {
                for (String key : encodedPathParameters.keySet()) {
                    if (!pathParameters.containsKey(key)) {
                        pathParameters.putSingle(UriComponent.decode(key, UriComponent.PATH_SEGMENT),
                                                 UriComponent.decode(encodedPathParameters.getFirst(key), UriComponent.PATH));
                    }
                }
            }
            return pathParameters;
        }
        return encodedPathParameters;
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        if (decode) {
            return pathSegments == null ? (pathSegments = UriComponent.parsePathSegments(getPath(true), true)) : pathSegments;
        }
        return encodedPathSegments == null ? (encodedPathSegments = UriComponent.parsePathSegments(getPath(false), false)) : encodedPathSegments;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties == null ? properties = new HashMap<>() : properties;
    }

    @Override
    public String getProperty(String name) {
        return getProperties().get(name);
    }

    @Override
    public void setProperty(String name, String value) {
        getProperties().put(name, value);
    }

    /**
     * @return {@link ProviderBinder}
     * @see Providers
     */
    public ProviderBinder getProviders() {
        return providers;
    }

    /**
     * @param providers
     *         ProviderBinder
     */
    public void setProviders(ProviderBinder providers) {
        this.providers = providers;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (decode) {
            return queryParameters != null ? queryParameters : (queryParameters =
                    UriComponent.parseQueryString(getRequestUri().getRawQuery(), true));
        }
        return encodedQueryParameters != null ? encodedQueryParameters : (encodedQueryParameters =
                UriComponent.parseQueryString(getRequestUri().getRawQuery(), false));
    }

    /** @return See {@link Request} */
    public Request getRequest() {
        return request;
    }

    @Override
    public URI getRequestUri() {
        return request.getRequestUri();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(getRequestUri());
    }

    /** @return See {@link SecurityContext} */
    public SecurityContext getSecurityContext() {
        // We get security information from HttpServletRequest but we may be not able to do this is asynchronous mode.
        // In asynchronous mode resource method processed when HTTP request ended already and we cannot use it anymore.
        // Do some workaround to keep security info even after request ends.
        if (isAsynchronous() && (request instanceof ServletContainerRequest)) {
            if (asynchronousSecurityContext == null) {
                Principal requestPrincipal = request.getUserPrincipal();
                if (requestPrincipal == null) {
                    asynchronousSecurityContext = new SimpleSecurityContext(request.isSecure());
                } else {
                    // Info about roles declared for web application. We assume this is all roles that we can meet.
                    WebApplicationDeclaredRoles declaredRoles =
                            (WebApplicationDeclaredRoles)EnvironmentContext.getCurrent().get(WebApplicationDeclaredRoles.class);
                    if (declaredRoles == null) {
                        asynchronousSecurityContext = new SimpleSecurityContext(new SimplePrincipal(requestPrincipal.getName()),
                                                                                null, request.getAuthenticationScheme(),
                                                                                request.isSecure());
                    } else {
                        Set<String> userRoles = new LinkedHashSet<>();
                        for (String declaredRole : declaredRoles.getDeclaredRoles()) {
                            if (request.isUserInRole(declaredRole)) {
                                userRoles.add(declaredRole);
                            }
                        }
                        asynchronousSecurityContext = new SimpleSecurityContext(new SimplePrincipal(requestPrincipal.getName()),
                                                                                userRoles, request.getAuthenticationScheme(),
                                                                                request.isSecure());
                    }
                }
            }
            return asynchronousSecurityContext;
        }
        return request;
    }

    /** @return See {@link UriInfo} */
    public UriInfo getUriInfo() {
        return this;
    }

    /**
     * @param dependencySupplier
     *         DependencySupplier
     */
    public void setDependencySupplier(DependencySupplier dependencySupplier) {
        this.dependencySupplier = dependencySupplier;
    }

    /**
     * Pass in context list of path template parameters @see {@link UriPattern}.
     *
     * @param parameterNames
     *         list of templates parameters
     */
    public void setParameterNames(List<String> parameterNames) {
        if (encodedPathParameters == null) {
            encodedPathParameters = new MultivaluedMapImpl();
        }
        for (int i = 0; i < parameterNames.size(); i++) {
            encodedPathParameters.add(parameterNames.get(i), parameterValues.get(i));
        }
    }

    /** @return {@code true} if request is asynchronous and {@code false} otherwise, */
    public boolean isAsynchronous() {
        return Boolean.parseBoolean(getQueryParameters().getFirst("async"))
               || Boolean.parseBoolean(request.getRequestHeaders().getFirst("x-everrest-async"));
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public EverrestConfiguration getEverrestConfiguration() {
        return configuration == null ? configuration = new EverrestConfiguration() : configuration;
    }

    public void setEverrestConfiguration(EverrestConfiguration config) {
        this.configuration = config;
    }

    /** @see Lifecycle#start() */
    @Override
    public final void start() {
    }

    /** @see Lifecycle#stop() */
    @Override
    public final void stop() {
        @SuppressWarnings("unchecked")
        List<LifecycleComponent> perRequestComponents = (List<LifecycleComponent>)getAttributes().get("org.everrest.lifecycle.PerRequest");
        if (perRequestComponents != null && !perRequestComponents.isEmpty()) {
            for (LifecycleComponent component : perRequestComponents) {
                try {
                    component.destroy();
                } catch (InternalException e) {
                    LOG.error("Unable to destroy component", e);
                }
            }
            perRequestComponents.clear();
        }
    }

    public static ApplicationContextBuilder anApplicationContext() {
        return new ApplicationContextBuilder();
    }

    public static class ApplicationContextBuilder {
        private GenericContainerRequest       request;
        private GenericContainerResponse      response;
        private ProviderBinder                providers;
        private Map<String, String>           properties;
        private Application                   application;
        private MethodInvokerDecoratorFactory methodInvokerDecoratorFactory;
        private EverrestConfiguration         configuration;
        private DependencySupplier            dependencySupplier;

        private ApplicationContextBuilder() {
            properties = new HashMap<>();
        }

        public ApplicationContextBuilder withRequest(GenericContainerRequest request) {
            this.request = request;
            return this;
        }

        public ApplicationContextBuilder withResponse(GenericContainerResponse response) {
            this.response = response;
            return this;
        }

        public ApplicationContextBuilder withProviders(ProviderBinder providers) {
            this.providers = providers;
            return this;
        }

        public ApplicationContextBuilder withProperties(Map<String, String> properties) {
            if (properties != null) {
                this.properties.putAll(properties);
            }
            return this;
        }

        public ApplicationContextBuilder withApplication(Application application) {
            this.application = application;
            return this;
        }

        public ApplicationContextBuilder withMethodInvokerDecoratorFactory(MethodInvokerDecoratorFactory methodInvokerDecoratorFactory) {
            this.methodInvokerDecoratorFactory = methodInvokerDecoratorFactory;
            return this;
        }

        public ApplicationContextBuilder withConfiguration(EverrestConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public ApplicationContextBuilder withDependencySupplier(DependencySupplier dependencySupplier) {
            this.dependencySupplier = dependencySupplier;
            return this;
        }

        public ApplicationContext build() {
            return new ApplicationContext(this);
        }
    }
}
