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
package org.everrest.core.wadl;

import org.everrest.core.impl.resource.AbstractResourceDescriptorImpl;
import org.everrest.core.method.MethodParameter;
import org.everrest.core.resource.AbstractResourceDescriptor;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.resource.ResourceMethodMap;
import org.everrest.core.resource.SubResourceLocatorDescriptor;
import org.everrest.core.resource.SubResourceMethodDescriptor;
import org.everrest.core.resource.SubResourceMethodMap;
import org.everrest.core.wadl.research.Application;
import org.everrest.core.wadl.research.Param;
import org.everrest.core.wadl.research.ParamStyle;
import org.everrest.core.wadl.research.RepresentationType;
import org.everrest.core.wadl.research.Resources;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages process of creation WADL document which describe
 * {@link AbstractResourceDescriptor}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public final class WadlProcessor {

    /** See {@link WadlGenerator}. */
    private final WadlGenerator wadlGenerator;

    /** Constructs new instance of WadlProcessor which use default WadlGenerator. */
    public WadlProcessor() {
        this.wadlGenerator = new BaseWadlGeneratorImpl();
    }

    /**
     * Constructs new instance of WadlProcessor with specified WadlGenerator.
     *
     * @param wadlGenerator
     *         {@link WadlGenerator}
     */
    public WadlProcessor(WadlGenerator wadlGenerator) {
        this.wadlGenerator = wadlGenerator;
    }

    /**
     * Process {@link AbstractResourceDescriptor} for build its WADL
     * representation.
     *
     * @param resourceDescriptor
     *         see {@link AbstractResourceDescriptor}
     * @param baseURI
     *         base URI of resource, e. g. servlet context
     * @return {@link Application}
     */
    public Application process(AbstractResourceDescriptor resourceDescriptor, URI baseURI) {
        // Root component of WADL representation
        Application wadlApp = wadlGenerator.createApplication();
        // Container for resources
        Resources wadlResources = wadlGenerator.createResources();
        if (baseURI != null) {
            wadlResources.setBase(baseURI.toString());
        }

        org.everrest.core.wadl.research.Resource wadlResource = processResource(resourceDescriptor);
        wadlResources.getResource().add(wadlResource);

        wadlApp.setResources(wadlResources);
        return wadlApp;
    }

    /**
     * @param resourceDescriptor
     *         see {@link AbstractResourceDescriptor}
     * @return see {@link WadlGenerator#createResponse()}
     */
    private org.everrest.core.wadl.research.Resource processResource(AbstractResourceDescriptor resourceDescriptor) {
        org.everrest.core.wadl.research.Resource wadlResource = wadlGenerator.createResource(resourceDescriptor);

        // Keeps common parameters for resource.
        Map<String, Param> wadlResourceParams = new HashMap<String, Param>();

        ResourceMethodMap<ResourceMethodDescriptor> resourceMethods = resourceDescriptor.getResourceMethods();
        for (List<ResourceMethodDescriptor> l : resourceMethods.values()) {
            for (ResourceMethodDescriptor rmd : l) {
                org.everrest.core.wadl.research.Method wadlMethod = processMethod(rmd, wadlResourceParams);
                if (wadlMethod == null) {
                    continue;
                }
                wadlResource.getMethodOrResource().add(wadlMethod);
            }
        }

        // Add parameters to a resource
        for (Param p : wadlResourceParams.values()) {
            wadlResource.getParam().add(p);
        }

        processSubResourceMethods(wadlResource, resourceDescriptor);

        processSubResourceLocators(wadlResource, resourceDescriptor);

        return wadlResource;
    }

    /**
     * Process sub-resource methods.
     *
     * @param wadlResource
     *         see {@link org.everrest.core.wadl.research.Resource}
     * @param resourceDescriptor
     *         see {@link AbstractResourceDescriptor}
     */
    private void processSubResourceMethods(org.everrest.core.wadl.research.Resource wadlResource,
                                           AbstractResourceDescriptor resourceDescriptor) {

        // Keeps common parameter for sub-resource.
        Map<String, Map<String, Param>> wadlCommonSubResourceParams = new HashMap<String, Map<String, Param>>();
        // Mapping resource path to resource.
        Map<String, org.everrest.core.wadl.research.Resource> wadlSubResources =
                new HashMap<String, org.everrest.core.wadl.research.Resource>();

        SubResourceMethodMap subresourceMethods = resourceDescriptor.getSubResourceMethods();
        for (ResourceMethodMap<SubResourceMethodDescriptor> rmm : subresourceMethods.values()) {
            for (List<SubResourceMethodDescriptor> l : rmm.values()) {
                for (SubResourceMethodDescriptor srmd : l) {
                    String path = srmd.getPathValue().getPath();
                    org.everrest.core.wadl.research.Resource wadlSubResource = wadlSubResources.get(path);
                    // There is no any resource for 'path' yet.
                    if (wadlSubResource == null) {
                        wadlSubResource = wadlGenerator.createResource(path);
                        Map<String, Param> wadlResourceParams = new HashMap<String, Param>();
                        org.everrest.core.wadl.research.Method wadlMethod = processMethod(srmd, wadlResourceParams);
                        if (wadlMethod == null) {
                            continue;
                        }
                        wadlSubResource.getMethodOrResource().add(wadlMethod);
                        // Remember sub-resource and parameters.
                        wadlSubResources.put(path, wadlSubResource);
                        wadlCommonSubResourceParams.put(path, wadlResourceParams);
                    } else {
                        // Get parameters for sub-resource that was created by one of previous
                        // iteration.
                        Map<String, Param> wadlResourceParams = wadlCommonSubResourceParams.get(path);
                        // Add new method.
                        org.everrest.core.wadl.research.Method wadlMethod = processMethod(srmd, wadlResourceParams);
                        if (wadlMethod == null) {
                            continue;
                        }
                        wadlSubResource.getMethodOrResource().add(wadlMethod);
                    }
                }

            }
        }
        // Add sub-resources to the root resource.
        for (Map.Entry<String, org.everrest.core.wadl.research.Resource> entry : wadlSubResources.entrySet()) {
            String path = entry.getKey();
            org.everrest.core.wadl.research.Resource wadlSubResource = entry.getValue();

            for (Param wadlSubParam : wadlCommonSubResourceParams.get(path).values()) {
                wadlSubResource.getParam().add(wadlSubParam);
            }

            wadlResource.getMethodOrResource().add(wadlSubResource);
        }
    }

    /**
     * Process sub-resource locators.
     *
     * @param wadlResource
     *         see {@link org.everrest.core.wadl.research.Resource}
     * @param resourceDescriptor
     *         see {@link AbstractResourceDescriptor}
     */
    private void processSubResourceLocators(org.everrest.core.wadl.research.Resource wadlResource,
                                            AbstractResourceDescriptor resourceDescriptor) {
        for (SubResourceLocatorDescriptor srld : resourceDescriptor.getSubResourceLocators().values()) {
            AbstractResourceDescriptor subResourceDescriptor = new AbstractResourceDescriptorImpl(srld.getMethod().getReturnType()) {
                @Override
                protected void processConstructors() {
                }

                @Override
                protected void processFields() {
                }
            };
            org.everrest.core.wadl.research.Resource wadlSubResource = processResource(subResourceDescriptor);
            wadlSubResource.setPath(srld.getPathValue().getPath());
            wadlResource.getMethodOrResource().add(wadlSubResource);
        }
    }

    /**
     * @param rmd
     *         see {@link ResourceMethodDescriptor}
     * @param wadlResourceParams
     *         for adding parameters which must be in parent
     * @return {@link org.everrest.core.wadl.research.Method}
     */
    private org.everrest.core.wadl.research.Method processMethod(ResourceMethodDescriptor rmd,
                                                                 Map<String, Param> wadlResourceParams) {
        org.everrest.core.wadl.research.Method wadlMethod = wadlGenerator.createMethod(rmd);
        // See description of this in
        // BaseWadlGeneratorImpl.createMethod(ResourceMethodDescriptor)
        if (wadlMethod == null) {
            return null;
        }

        org.everrest.core.wadl.research.Request wadlRequest = processRequest(rmd, wadlResourceParams);
        if (wadlRequest != null) {
            wadlMethod.setRequest(wadlRequest);
        }

        org.everrest.core.wadl.research.Response wadlResponse = processResponse(rmd);
        if (wadlResponse != null) {
            wadlMethod.setResponse(wadlResponse);
        }

        return wadlMethod;
    }

    /**
     * @param rmd
     *         see {@link ResourceMethodDescriptor}
     * @param wadlResourceParams
     *         for adding parameters which must be in parent
     * @return {@link org.everrest.core.wadl.research.Request}
     */
    private org.everrest.core.wadl.research.Request processRequest(ResourceMethodDescriptor rmd,
                                                                   Map<String, Param> wadlResourceParams) {
        org.everrest.core.wadl.research.Request wadlRequest = wadlGenerator.createRequest();
        for (MethodParameter methodParameter : rmd.getMethodParameters()) {
            if (methodParameter.getAnnotation() == null) {
                for (MediaType mediaType : rmd.consumes()) {
                    RepresentationType wadlRepresentation = wadlGenerator.createRequestRepresentation(mediaType);
                    wadlRequest.getRepresentation().add(wadlRepresentation);
                }
            }
            Param wadlParam = processParam(methodParameter);
            if (wadlParam != null) {
                if (wadlParam.getStyle() == ParamStyle.QUERY || wadlParam.getStyle() == ParamStyle.HEADER
               /* || wadlParam.getStyle() == ParamStyle.MATRIX */) {
                    wadlRequest.getParam().add(wadlParam);
                } else {
                    // If matrix or path template parameter then add in map for add in
                    // parent element
                    wadlResourceParams.put(wadlParam.getName(), wadlParam);
                }
            }
        }

        // NOTE If there are no any representation and parameters then request is
        // null.
        return wadlRequest.getRepresentation().isEmpty() && wadlRequest.getParam().isEmpty() ? null : wadlRequest;
    }

    /**
     * @param rmd
     *         see {@link ResourceMethodDescriptor}
     * @return {@link org.everrest.core.wadl.research.Response}
     */
    private org.everrest.core.wadl.research.Response processResponse(ResourceMethodDescriptor rmd) {
        org.everrest.core.wadl.research.Response wadlResponse = null;
        if (rmd.getResponseType() != void.class) {
            wadlResponse = wadlGenerator.createResponse();
            for (MediaType mediaType : rmd.produces()) {
                RepresentationType wadlRepresentation = wadlGenerator.createResponseRepresentation(mediaType);
                // Element can represent normal response or fault response
                JAXBElement<RepresentationType> wadlRepresentationElement =
                        new JAXBElement<RepresentationType>(new QName("http://research.sun.com/wadl/2006/10", "representation"),
                                                            RepresentationType.class, wadlRepresentation);

                wadlResponse.getRepresentationOrFault().add(wadlRepresentationElement);
            }
        }

        return wadlResponse;
    }

    /**
     * @param methodParameter
     *         see {@link MethodParameter}
     * @return {@link Param}
     */
    private Param processParam(MethodParameter methodParameter) {
        Param wadlParam = null;
        // Skip parameters without annotation (entity) and parameters with javax.ws.rs.core.Context.
        // Context parameter dependent of environment and not used in WADL representation
        if (methodParameter.getAnnotation() != null
            && methodParameter.getAnnotation().annotationType() != javax.ws.rs.core.Context.class) {
            wadlParam = wadlGenerator.createParam(methodParameter);
        }

        return wadlParam;
    }

}
