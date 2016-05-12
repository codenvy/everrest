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
package org.everrest.core.tools;

import org.everrest.core.ContainerResponseWriter;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.InputHeadersMap;
import org.everrest.core.impl.MultivaluedMapImpl;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class may be useful for running test and should not be used for launching services in real environment, Servlet Container for
 * example.
 *
 * @author andrew00x
 */
public class ResourceLauncher {

    private final EverrestProcessor processor;

    public ResourceLauncher(EverrestProcessor processor) {
        this.processor = processor;
    }

    public ContainerResponse service(String httpMethod,
                                     String requestURI,
                                     String baseURI,
                                     Map<String, List<String>> headers,
                                     byte[] data,
                                     ContainerResponseWriter writer,
                                     EnvironmentContext env) throws Exception {

        if (baseURI == null) {
            baseURI = "";
        }

        if (requestURI == null) {
            requestURI = "/";
        }

        if (baseURI.isEmpty() && !requestURI.startsWith("/")) {
            requestURI = '/' + requestURI;
        }

        Map<String, List<String>> headersCopy = new MultivaluedMapImpl();
        if (headers != null) {
            headersCopy.putAll(headers);
        }

        InputStream in;
        if (data != null) {
            in = new ByteArrayInputStream(data);
            headersCopy.put(HttpHeaders.CONTENT_LENGTH, Arrays.asList(Integer.toString(data.length)));
        } else {
            in = new EmptyInputStream();
            headersCopy.put(HttpHeaders.CONTENT_LENGTH, Arrays.asList("0"));
        }

        if (env == null) {
            env = new EnvironmentContext();
        }

        if (writer == null) {
            writer = new DummyContainerResponseWriter();
        }

        SecurityContext securityContext = (SecurityContext)env.get(SecurityContext.class);

        if (securityContext == null) {
            securityContext = new SimpleSecurityContext(false);
        }

        ContainerRequest request = new ContainerRequest(httpMethod, URI.create(requestURI), URI.create(baseURI), in,
                                                        new InputHeadersMap(headersCopy), securityContext);
        ContainerResponse response = new ContainerResponse(writer);
        processor.process(request, response, env);
        return response;
    }

    public ContainerResponse service(String httpMethod,
                                     String requestURI,
                                     String baseURI,
                                     Map<String, List<String>> headers,
                                     byte[] data,
                                     EnvironmentContext env) throws Exception {
        return service(httpMethod, requestURI, baseURI, headers, data, new DummyContainerResponseWriter(), env);
    }
}
