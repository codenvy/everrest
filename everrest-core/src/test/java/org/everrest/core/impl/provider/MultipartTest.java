/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.everrest.core.impl.provider;

import org.apache.commons.fileupload.FileItem;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.provider.multipart.InputItem;
import org.everrest.core.impl.provider.multipart.OutputItem;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public class MultipartTest extends BaseTest {
    /** Pattern for comparison with parsed {@link org.apache.commons.fileupload.FileItem}. */
    private static class FileItemTester {
        private boolean isFormField;
        private String  contentType;
        private String  name;
        private String  fieldName;
        private String  string;

        public FileItemTester(String contentType, boolean isFormField, String fieldName, String name, String string) {
            this.contentType = contentType;
            this.isFormField = isFormField;
            this.fieldName = fieldName;
            this.name = name;
            this.string = string;
        }

        public String getContentType() {
            return contentType;
        }

        public boolean isFormField() {
            return isFormField;
        }

        public String getName() {
            return fieldName;
        }

        public String getFilename() {
            return name;
        }

        public String getString() {
            return string;
        }
    }

    @Path("/1")
    public static class Resource1 {
        private Iterator<FileItemTester> pattern;

        /** Initialize <tt>pattern</tt>. */
        public Resource1() {
            List<FileItemTester> l = new ArrayList<>(3);
            l.add(new FileItemTester("text/xml", false, "xml-file", "foo.xml", XML_DATA));
            l.add(new FileItemTester("application/json", false, "json-file", "foo.json", JSON_DATA));
            l.add(new FileItemTester(null, true, "field", null, TEXT_DATA));
            pattern = l.iterator();
        }

        @POST
        @Consumes("multipart/*")
        public void m(Iterator<FileItem> iter) throws Exception {
            while (iter.hasNext()) {
                if (!pattern.hasNext())
                    fail("Wrong number of parsed items");
                FileItem fi = iter.next();
                FileItemTester fit = pattern.next();
                assertEquals(fit.getContentType(), fi.getContentType());
                assertEquals(fit.isFormField(), fi.isFormField());
                assertEquals(fit.getName(), fi.getFieldName());
                assertEquals(fit.getFilename(), fi.getName());
                assertEquals(fit.getString(), fi.getString());
            }
        }
    }

    private static class InputItemTester implements InputItem {
        final String    name;
        final String    filename;
        final MediaType mediaType;
        final String    body;
        final MultivaluedMap<String, String> headers = new MultivaluedMapImpl();

        InputItemTester(String name, String filename, MediaType mediaType, String body) {
            this.name = name;
            this.filename = filename;
            this.mediaType = mediaType;
            this.body = body;
        }

        InputItemTester(String name, MediaType mediaType, String body) {
            this(name, null, mediaType, body);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public MediaType getMediaType() {
            return mediaType;
        }

        @Override
        public MultivaluedMap<String, String> getHeaders() {
            return headers;
        }

        @Override
        public String getBodyAsString() throws IOException {
            return body;
        }

        /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

        @Override
        public <T> T getBody(Class<T> type, Type genericType) throws IOException {
            return null;
        }

        @Override
        public InputStream getBody() throws IOException {
            return null;
        }
    }

    @Path("/2")
    public static class Resource2 {
        private List<InputItemTester> pattern;

        public Resource2() {
            pattern = new ArrayList<>(3);
            pattern.add(new InputItemTester("xml-file", "foo.xml", MediaType.TEXT_XML_TYPE, XML_DATA));
            pattern.add(new InputItemTester("json-file", "foo.json", MediaType.APPLICATION_JSON_TYPE, JSON_DATA));
            pattern.add(new InputItemTester("field", null, null, TEXT_DATA));
        }

        @POST
        @Consumes("multipart/*")
        public void m(List<InputItem> items) throws Exception {
            assertEquals(pattern.size(), items.size());
            for (int i = 0; i < items.size(); i++) {
                InputItem item = items.get(i);
                InputItemTester tester = pattern.get(i);
                assertEquals(tester.getName(), item.getName());
                assertEquals(tester.getFilename(), item.getFilename());
                assertEquals(tester.getMediaType(), item.getMediaType());
                assertEquals(tester.getBodyAsString(), item.getBodyAsString());
            }
        }
    }

    @Path("/3")
    public static class Resource3 {
        private Map<String, InputItemTester> pattern;

        public Resource3() {
            pattern = new HashMap<>(3);
            pattern.put("xml-file", new InputItemTester("xml-file", "foo.xml", MediaType.TEXT_XML_TYPE, XML_DATA));
            pattern.put("json-file", new InputItemTester("json-file", "foo.json", MediaType.APPLICATION_JSON_TYPE, JSON_DATA));
            pattern.put("field", new InputItemTester("field", null, null, TEXT_DATA));
        }

        @POST
        @Consumes("multipart/*")
        public void m(Map<String, InputItem> map) throws Exception {
            assertEquals(pattern.size(), map.size());
            for (InputItem item : map.values()) {
                InputItemTester tester = pattern.get(item.getName());
                assertNotNull(tester);
                assertEquals(tester.getName(), item.getName());
                assertEquals(tester.getFilename(), item.getFilename());
                assertEquals(tester.getMediaType(), item.getMediaType());
                assertEquals(tester.getBodyAsString(), item.getBodyAsString());
            }
        }
    }

    @Path("/4")
    public static class Resource4 {
        @GET
        @Produces("multipart/form-data;boundary=" + BOUNDARY)
        public GenericEntity<List<OutputItem>> m1() throws Exception {
            List<OutputItem> list = new ArrayList<>(3);
            list.add(OutputItem.create("xml-file", XML_DATA, MediaType.TEXT_XML_TYPE, "foo.xml"));
            list.add(OutputItem.create("json-file", new JsonData("hello world"), MediaType.APPLICATION_JSON_TYPE, "foo.json"));
            list.add(OutputItem.create("field", TEXT_DATA, null));
            return new GenericEntity<List<OutputItem>>(list) {};
        }
    }

    public static class JsonData {
        private String data;

        public JsonData(String data) {
            this.data = data;
        }

        public JsonData() {
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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setContext();
    }

    public void testInputMultipartFormApache() throws Exception {
        Resource1 r1 = new Resource1();
        registry(r1);
        doPost("/1");
        unregistry(r1);
    }

    public void testInputMultipartFormList() throws Exception {
        Resource2 r2 = new Resource2();
        registry(r2);
        doPost("/2");
        unregistry(r2);
    }

    public void testInputMultipartFormMap() throws Exception {
        Resource3 r3 = new Resource3();
        registry(r3);
        doPost("/3");
        unregistry(r3);
    }

    public void testOutputMultipartFormList() throws Exception {
        Resource4 r4 = new Resource4();
        registry(r4);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/4", "", null, null, writer, null);
        assertEquals(200, response.getStatus());
        assertEquals(FORM_DATA, new String(writer.getBody()));
        unregistry(r4);
    }

    private void doPost(String path) throws Exception {
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(out);
        w.write(FORM_DATA);
        w.flush();
        h.putSingle("content-type", "multipart/form-data;boundary=" + BOUNDARY);
        byte[] data = out.toByteArray();
        // NOTE In this test data will be red from HttpServletRequest, not from
        // byte array. See MultipartFormDataEntityProvider.
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(data), data.length, "POST", h));
        ContainerResponse response = launcher.service("POST", path, "", h, data, env);
        assertEquals(204, response.getStatus());
    }
}
