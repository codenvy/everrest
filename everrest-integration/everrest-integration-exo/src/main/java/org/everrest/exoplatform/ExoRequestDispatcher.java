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
package org.everrest.exoplatform;

import org.everrest.core.ApplicationContext;
import org.everrest.core.FilterDescriptor;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.GenericContainerResponse;
import org.everrest.core.ObjectFactory;
import org.everrest.core.RequestFilter;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.exoplatform.container.RestfulComponentAdapter;
import org.everrest.exoplatform.container.RestfulContainer;
import org.exoplatform.container.ExoContainerContext;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class ExoRequestDispatcher extends RequestDispatcher {
    private final ProvidersRegistry providersRegistry;

    /**
     * @param resourceBinder
     *         ResourceBinder
     * @param providersRegistry
     *         ProvidersRegistry
     */
    public ExoRequestDispatcher(ResourceBinder resourceBinder, ProvidersRegistry providersRegistry) {
        super(resourceBinder);
        this.providersRegistry = providersRegistry;
    }

    /**
     * @see org.everrest.core.impl.RequestDispatcher#dispatch(org.everrest.core.GenericContainerRequest,
     *      org.everrest.core.GenericContainerResponse)
     */
    @Override
    public void dispatch(GenericContainerRequest request, GenericContainerResponse response) {
        ApplicationContext context = ApplicationContextImpl.getCurrent();
        String requestPath = context.getPath(false);
        List<String> parameterValues = context.getParameterValues();
        ObjectFactory<AbstractResourceDescriptor> resourceFactory = getRootResource(parameterValues, requestPath);
        AbstractResourceDescriptor resourceDescriptor = resourceFactory.getObjectModel();
        if (resourceDescriptor instanceof ApplicationResource) {
            String appName = ((ApplicationResource)resourceDescriptor).getApplicationName();
            ProviderBinder appProviders = providersRegistry.getProviders(appName);
            if (appProviders != null) {
                context.setProviders(appProviders);
                // Apply application specific request filters if any.
                for (ObjectFactory<FilterDescriptor> factory : context.getProviders().getRequestFilters(context.getPath())) {
                    ((RequestFilter)factory.getInstance(context)).doFilter(request);
                }
            }
        }
        String newRequestPath = getPathTail(parameterValues);
        context.addMatchedURI(requestPath.substring(0, requestPath.lastIndexOf(newRequestPath)));
        context.setParameterNames(resourceFactory.getObjectModel().getUriPattern().getParameterNames());
        Object resource = resourceFactory.getInstance(context);
        dispatch(request, response, context, resourceFactory, resource, newRequestPath);
    }

    @Override
    protected ObjectFactory<AbstractResourceDescriptor> getRootResource(List<String> parameterValues, String requestPath) {
        ObjectFactory<AbstractResourceDescriptor> resource;
        try {
            resource = super.getRootResource(parameterValues, requestPath);
        } catch (WebApplicationException wae) {
            if (404 == wae.getResponse().getStatus()) {
                Provider provider = (Provider)ExoContainerContext.getCurrentContainer().getComponentInstance("RestfulContainerProvider");
                if (null != provider) {
                    RestfulContainer container = (RestfulContainer)provider.get();
                    if (null != container) {
                        RestfulComponentAdapter resourceAdapter =
                                (RestfulComponentAdapter)container.getMatchedResource(requestPath, parameterValues);
                        if (null != resourceAdapter) {
                            return (ObjectFactory)resourceAdapter.getFactory();
                        }
                    }
                }
            }
            throw wae;
        }
        return resource;
    }
}
