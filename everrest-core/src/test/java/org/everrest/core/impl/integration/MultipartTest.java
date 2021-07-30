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
package org.everrest.core.impl.integration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

import org.apache.commons.fileupload.FileItem;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.provider.multipart.InputItem;
import org.everrest.core.impl.provider.multipart.OutputItem;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static org.everrest.core.impl.provider.multipart.OutputItem.anOutputItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultipartTest extends BaseTest {
    @Path("/1")
    public static class Resource1 {
        private Iterator<FileItem> expected;

        public Resource1(Iterator<FileItem> expected) {
            this.expected = expected;
        }

        @POST
        @Consumes("multipart/*")
        public void m(Iterator<FileItem> fileItemIterator) throws Exception {
            while (fileItemIterator.hasNext()) {
                if (!expected.hasNext()) {
                    fail("Wrong number of parsed items");
                }
                FileItem inputFileItem = fileItemIterator.next();
                FileItem expectedFileItem = expected.next();
                assertEquals(expectedFileItem.getContentType(), inputFileItem.getContentType());
                assertEquals(expectedFileItem.isFormField(), inputFileItem.isFormField());
                assertEquals(expectedFileItem.getFieldName(), inputFileItem.getFieldName());
                assertEquals(expectedFileItem.getName(), inputFileItem.getName());
                assertEquals(expectedFileItem.getString(), inputFileItem.getString());
            }
            if (expected.hasNext()) {
                fail("Wrong number of parsed items");
            }
        }
    }

    @Path("/2")
    public static class Resource2 {
        private List<InputItem> expected;

        public Resource2(List<InputItem> expected) {
            this.expected = expected;
        }

        @POST
        @Consumes("multipart/*")
        public void m(List<InputItem> items) throws Exception {
            assertEquals(expected.size(), items.size());
            for (int i = 0; i < items.size(); i++) {
                InputItem item = items.get(i);
                InputItem expectedItem = expected.get(i);
                assertEquals(expectedItem.getName(), item.getName());
                assertEquals(expectedItem.getFilename(), item.getFilename());
                assertEquals(expectedItem.getMediaType(), item.getMediaType());
                assertEquals(expectedItem.getBodyAsString(), item.getBodyAsString());
            }
        }
    }

    @Path("/3")
    public static class Resource3 {
        private Map<String, InputItem> expected;

        public Resource3(Map<String, InputItem> expected) {
            this.expected = expected;
        }

        @POST
        @Consumes("multipart/*")
        public void m(Map<String, InputItem> map) throws Exception {
            assertEquals(expected.size(), map.size());
            for (InputItem inputItem : map.values()) {
                InputItem expectedInputItem = expected.get(inputItem.getName());
                assertNotNull(expectedInputItem);
                assertEquals(expectedInputItem.getName(), inputItem.getName());
                assertEquals(expectedInputItem.getFilename(), inputItem.getFilename());
                assertEquals(expectedInputItem.getMediaType(), inputItem.getMediaType());
                assertEquals(expectedInputItem.getBodyAsString(), inputItem.getBodyAsString());
            }
        }
    }


    @Path("/4")
    public static class Resource4 {
        @GET
        @Produces("multipart/form-data;boundary=" + BOUNDARY)
        public GenericEntity<List<OutputItem>> m1() throws Exception {
            List<OutputItem> list = new ArrayList<>(3);
            list.add(anOutputItem().withName("xml-file").withEntity(XML_DATA).withMediaType(TEXT_XML_TYPE).withFilename("foo.xml").build());
            list.add(anOutputItem().withName("json-file").withEntity(new JsonData("hello world")).withMediaType(APPLICATION_JSON_TYPE).withFilename("foo.json").build());
            list.add(anOutputItem().withName("field").withEntity(TEXT_DATA).build());
            return new GenericEntity<List<OutputItem>>(list) {
            };
        }
    }

    public static class JsonData {
        private String data;

        public JsonData(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    private static final String XML_DATA  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><data>hello world</data></root>";
    private static final String JSON_DATA = "{\"data\":\"hello world\"}";
    private static final String TEXT_DATA = "to be or not to be";
    private static final String BOUNDARY  = "abcdef";
    private static final String FORM_DATA =
            "--" + BOUNDARY + "\r\n" + "Content-Disposition: form-data; name=\"xml-file\"; filename=\"foo.xml\"\r\n"
            + "Content-Type: text/xml\r\n" + "\r\n" + XML_DATA + "\r\n" + "--" + BOUNDARY + "\r\n"
            + "Content-Disposition: form-data; name=\"json-file\"; filename=\"foo.json\"\r\n"
            + "Content-Type: application/json\r\n" + "\r\n" + JSON_DATA + "\r\n" + "--" + BOUNDARY + "\r\n"
            + "Content-Disposition: form-data; name=\"field\"\r\n" + "\r\n" + TEXT_DATA + "\r\n"
            + "--" + BOUNDARY + "--\r\n";

    @Test
    public void testInputMultipartFormApache() throws Exception {
        Iterator<FileItem> expected = Iterators.forArray(createFileItem("text/xml", false, "xml-file", "foo.xml", XML_DATA),
                                                         createFileItem("application/json", false, "json-file", "foo.json", JSON_DATA),
                                                         createFileItem(null, true, "field", null, TEXT_DATA));
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1(expected));
            }
        });

        doPost("/1");
    }

    @Test
    public void testInputMultipartFormList() throws Exception {
        List<InputItem> expected = newArrayList(createInputItem("xml-file", "foo.xml", TEXT_XML_TYPE, XML_DATA),
                                                createInputItem("json-file", "foo.json", APPLICATION_JSON_TYPE, JSON_DATA),
                                                createInputItem("field", null, null, TEXT_DATA));
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                    return Collections.<Object>singleton(new Resource2(expected));
            }
        });

        doPost("/2");
    }

    @Test
    public void testInputMultipartFormMap() throws Exception {
        Map<String, InputItem> expected =
                ImmutableMap.of("xml-file", createInputItem("xml-file", "foo.xml", TEXT_XML_TYPE, XML_DATA),
                                "json-file", createInputItem("json-file", "foo.json", APPLICATION_JSON_TYPE, JSON_DATA),
                                "field", createInputItem("field", null, null, TEXT_DATA));

        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource3(expected));
            }
        });
        doPost("/3");
    }

    @Test
    public void testOutputMultipartFormList() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource4());
            }
        });
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/4", "", null, null, writer, null);
        assertEquals(200, response.getStatus());
        assertEquals(FORM_DATA, new String(writer.getBody()));
    }

    private static InputItem createInputItem(String name, String fileName, MediaType mediaType, String body) throws IOException {
        InputItem inputItem = mock(InputItem.class);
        when(inputItem.getName()).thenReturn(name);
        when(inputItem.getFilename()).thenReturn(fileName);
        when(inputItem.getMediaType()).thenReturn(mediaType);
        when(inputItem.getBodyAsString()).thenReturn(body);
        return inputItem;
    }

    private static FileItem createFileItem(String contentType, boolean isFormField, String fieldName, String name, String body) {
        FileItem fileItem = mock(FileItem.class);
        when(fileItem.getContentType()).thenReturn(contentType);
        when(fileItem.isFormField()).thenReturn(isFormField);
        when(fileItem.getFieldName()).thenReturn(fieldName);
        when(fileItem.getName()).thenReturn(name);
        when(fileItem.getString()).thenReturn(body);
        return fileItem;
    }

    private void doPost(String path) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(out);
        w.write(FORM_DATA);
        w.flush();
        String contentType = "multipart/form-data;boundary=" + BOUNDARY;
        byte[] content = out.toByteArray();

        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, mockHttpServletRequest(content, contentType));

        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.putSingle("content-type", contentType);

        ContainerResponse response = launcher.service("POST", path, "", headers, content, env);
        assertEquals(204, response.getStatus());
    }

    private HttpServletRequest mockHttpServletRequest(byte[] content, String contentType) throws Exception {
        ByteArrayInputStream contentAsStream = new ByteArrayInputStream(content);
        ServletInputStream servletInputStream = createServletInputStream(contentAsStream);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(httpServletRequest.getContentType()).thenReturn(contentType);
        return httpServletRequest;
    }

    private ServletInputStream createServletInputStream(ByteArrayInputStream in) {
        return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }

                @Override
                public int read() throws IOException {
                    return in.read();
                }
            };
    }
}
