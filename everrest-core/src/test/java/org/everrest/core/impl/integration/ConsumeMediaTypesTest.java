/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.integration;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Katayev
 */
@RunWith(DataProviderRunner.class)
public class ConsumeMediaTypesTest extends BaseTest {

    @Path("/a")
    @Consumes(TEXT_PLAIN)
    public static class Resource {
        @POST
        @Path("/1")
        public void m1(@HeaderParam(CONTENT_TYPE) String type) {
            assertEquals(TEXT_PLAIN, type);
        }

        @POST
        @Path("/1")
        @Consumes(TEXT_XML)
        public void m11(@HeaderParam(CONTENT_TYPE) String type) {
            assertEquals(TEXT_XML, type);
        }

        @POST
        @Path("/2")
        @Consumes(TEXT_XML)
        public void m2(@HeaderParam(CONTENT_TYPE) String type) {
            assertEquals(TEXT_XML, type);
        }

        @POST
        @Path("/3")
        @Consumes(APPLICATION_JSON)
        public void m3(@HeaderParam(CONTENT_TYPE) String type) {
            assertEquals(APPLICATION_JSON, type);
        }
    }

    @DataProvider
    public static Object[][] data() {
        return new Object[][]{
                {new Resource(), "/a/1", "text/plain",       "text",                204},
                {new Resource(), "/a/1", "text/xml",         "<xml/>",              204},
                {new Resource(), "/a/2", "text/xml",         "<xml/>",              204},
                {new Resource(), "/a/3", "application/json", "{\"json\":\"text\"}", 204},
                {new Resource(), "/a/2", "text/html",        "<html></html>",       415}
        };
    }

    @UseDataProvider("data")
    @Test
    public void testConsumesMediaTypes(Object resource,
                                       String path,
                                       String contentType,
                                       String entity,
                                       int expectedStatus) throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return newHashSet(resource);
            }
        });
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (contentType != null) {
            headers.add(CONTENT_TYPE, contentType);
        }

        ContainerResponse response = launcher.service("POST", path, "", headers, entity.getBytes(), null);

        assertEquals(expectedStatus, response.getStatus());
    }
}
