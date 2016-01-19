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

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.Ranges;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

/**
 * @author andrew00x
 */
public class PartialContentTest extends BaseTest {

    @Path("a")
    public static class Resource1 {
        @GET
        public Response m(@HeaderParam("Range") Ranges ranges) {
            int start = (int)ranges.getRanges().get(0).getStart();
            int end = (int)ranges.getRanges().get(0).getEnd();
            byte[] b = new byte[(end - start + 1)];
            System.arraycopy(contentBytes, 2, b, 0, b.length);
            return Response.status(206) //
                    .header(HttpHeaders.CONTENT_LENGTH, Long.toString(b.length)) //
                    .header(ExtHttpHeaders.ACCEPT_RANGES, "bytes") //
                    .header(ExtHttpHeaders.CONTENTRANGE, "bytes " + start + "-" + end + "/" + contentBytes.length) //
                    .entity(b) //
                    .build();
        }
    }

    private static final String contentString = "to be or not to be";

    private static final byte[] contentBytes = contentString.getBytes();

    @Test
    public void testPartialContent() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.<Class<?>>singleton(Resource1.class);
            }
        });
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle("range", "bytes=2-5");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/a", "", headers, null, writer, null);
        MultivaluedMap<String, Object> responseHeaders = writer.getHeaders();
        Assert.assertEquals("4", responseHeaders.getFirst("content-length"));
        Assert.assertEquals("bytes 2-5/18", responseHeaders.getFirst("content-range"));
        Assert.assertEquals("bytes", responseHeaders.getFirst("accept-ranges"));
        Assert.assertEquals(" be ", new String(writer.getBody()));
    }

}
