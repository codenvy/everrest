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
package org.everrest.core.impl.provider;

import org.everrest.core.generated.Book;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.provider.json.BooleanValue;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.everrest.core.impl.provider.json.StringValue;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
public class JsonEntityTest extends BaseTest {

    @Path("/")
    public static class ResourceBook {
        @POST
        @Consumes("application/json")
        public void m1(Book book) {
            Assert.assertEquals("Hamlet", book.getTitle());
            Assert.assertEquals("William Shakespeare", book.getAuthor());
            Assert.assertTrue(book.isSendByPost());
        }
    }

    @Path("/")
    public static class ResourceBookRaw {
        @POST
        @Consumes("application/json")
        public void m1(JsonValue book) {
            Assert.assertEquals("Hamlet", book.getElement("title").getStringValue());
            Assert.assertEquals("William Shakespeare", book.getElement("author").getStringValue());
            Assert.assertTrue(book.getElement("sendByPost").getBooleanValue());
        }
    }

    @Path("/")
    public static class ResourceBookArray {
        @POST
        @Consumes("application/json")
        public void m1(Book[] b) {
            Assert.assertEquals("Hamlet", b[0].getTitle());
            Assert.assertEquals("William Shakespeare", b[0].getAuthor());
            Assert.assertTrue(b[0].isSendByPost());
            Assert.assertEquals("Collected Stories", b[1].getTitle());
            Assert.assertEquals("Gabriel Garcia Marquez", b[1].getAuthor());
            Assert.assertTrue(b[1].isSendByPost());
        }
    }

    @Path("/")
    public static class ResourceBookCollection {
        @POST
        @Consumes("application/json")
        public void m1(List<Book> b) {
            Assert.assertEquals("Hamlet", b.get(0).getTitle());
            Assert.assertEquals("William Shakespeare", b.get(0).getAuthor());
            Assert.assertTrue(b.get(0).isSendByPost());
            Assert.assertEquals("Collected Stories", b.get(1).getTitle());
            Assert.assertEquals("Gabriel Garcia Marquez", b.get(1).getAuthor());
            Assert.assertTrue(b.get(1).isSendByPost());
        }
    }

    @Path("/")
    public static class ResourceBookMap {
        @POST
        @Consumes("application/json")
        public void m1(Map<String, Book> b) {
            Assert.assertEquals("Hamlet", b.get("12345").getTitle());
            Assert.assertEquals("William Shakespeare", b.get("12345").getAuthor());
            Assert.assertTrue(b.get("12345").isSendByPost());
            Assert.assertEquals("Collected Stories", b.get("54321").getTitle());
            Assert.assertEquals("Gabriel Garcia Marquez", b.get("54321").getAuthor());
            Assert.assertTrue(b.get("54321").isSendByPost());
        }
    }

    @Path("/")
    public static class ResourceString {
        @POST
        @Consumes("application/json")
        public void m1(String b) {
            Assert.assertEquals(jsonBook, b);
        }
    }

    @Path("/")
    public static class ResourceBook2 {
        @GET
        @Produces("application/json")
        public Book m1() {
            Book book = new Book();
            book.setTitle("Hamlet");
            book.setAuthor("William Shakespeare");
            book.setSendByPost(true);
            return book;
        }

        // Without @Produces annotation also should work.
        @POST
        public Book m2() {
            return m1();
        }
    }

    @Path("/")
    public static class ResourceBookRaw2 {
        @GET
        @Produces("application/json")
        public JsonValue m1() {
            ObjectValue book = new ObjectValue();
            book.addElement("title", new StringValue("Hamlet"));
            book.addElement("author", new StringValue("William Shakespeare"));
            book.addElement("sendByPost", new BooleanValue(true));
            return book;
        }

        // Without @Produces annotation also should work.
        @POST
        public JsonValue m2() {
            return m1();
        }
    }

    @Path("/")
    public static class ResourceBookArray2 {
        @GET
        @Produces("application/json")
        public Book[] m1() {
            return createArray();
        }

        // Without @Produces annotation also should work.
        @POST
        public Book[] m2() {
            return createArray();
        }

        private Book[] createArray() {
            Book book1 = new Book();
            book1.setTitle("Hamlet");
            book1.setAuthor("William Shakespeare");
            book1.setSendByPost(true);
            Book book2 = new Book();
            book2.setTitle("Collected Stories");
            book2.setAuthor("Gabriel Garcia Marquez");
            book2.setSendByPost(true);
            return new Book[]{book1, book2};
        }
    }

    @Path("/")
    public static class ResourceBookCollection2 {
        @GET
        @Produces("application/json")
        public List<Book> m1() {
            return createCollection();
        }

        // Without @Produces annotation also should work.
        @POST
        public List<Book> m2() {
            return createCollection();
        }

        private List<Book> createCollection() {
            Book book1 = new Book();
            book1.setTitle("Hamlet");
            book1.setAuthor("William Shakespeare");
            book1.setSendByPost(true);
            Book book2 = new Book();
            book2.setTitle("Collected Stories");
            book2.setAuthor("Gabriel Garcia Marquez");
            book2.setSendByPost(true);
            return Arrays.asList(book1, book2);
        }
    }

    @Path("/")
    public static class ResourceBookMap2 {
        @GET
        @Produces("application/json")
        public Map<String, Book> m1() {
            return createMap();
        }

        // Without @Produces annotation also should work.
        @POST
        public Map<String, Book> m2() {
            return createMap();
        }

        private Map<String, Book> createMap() {
            Book book1 = new Book();
            book1.setTitle("Hamlet");
            book1.setAuthor("William Shakespeare");
            book1.setSendByPost(true);
            Book book2 = new Book();
            book2.setTitle("Collected Stories");
            book2.setAuthor("Gabriel Garcia Marquez");
            book2.setSendByPost(true);
            Map<String, Book> m = new HashMap<>();
            m.put("12345", book1);
            m.put("54321", book2);
            return m;
        }
    }

    @Path("/")
    public static class ResourceString2 {
        @GET
        @Produces("application/json")
        public String m1() {
            return jsonBook;
        }

        @POST
        public Response m2() {
            return Response.ok(jsonBook).type(MediaType.APPLICATION_JSON).build();
        }
    }

    private static String jsonBook = "{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true}";

    private static String jsonArray =
            "[{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true},"
            + "{\"title\":\"Collected Stories\", \"author\":\"Gabriel Garcia Marquez\", \"sendByPost\":true}]";

    private static String jsonMap =
            "{\"12345\":{\"title\":\"Hamlet\", \"author\":\"William Shakespeare\", \"sendByPost\":true},"
            + "\"54321\":{\"title\":\"Collected Stories\", \"author\":\"Gabriel Garcia Marquez\", \"sendByPost\":true}}";

    private byte[] jsonBookData;

    private byte[] jsonArrayData;

    private byte[] jsonMapData;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        jsonBookData = jsonBook.getBytes("UTF-8");
        jsonArrayData = jsonArray.getBytes("UTF-8");
        jsonMapData = jsonMap.getBytes("UTF-8");
    }

    @Test
    public void testJsonEntityBean() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBook());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        // Object is transfered via JSON
        h.putSingle("content-type", "application/json");
        // with JSON transformation for Book have restriction can't pass BigDecimal
        // (has not simple constructor and it is not in JSON known types)
        h.putSingle("content-length", "" + jsonBookData.length);
        Assert.assertEquals(204, launcher.service("POST", "/", "", h, jsonBookData, null).getStatus());
    }

    @Test
    public void testJsonRawEntity() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookRaw());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        // Object is transfered via JSON
        h.putSingle("content-type", "application/json");
        // with JSON transformation for Book have restriction can't pass BigDecimal
        // (has not simple constructor and it is not in JSON known types)
        h.putSingle("content-length", "" + jsonBookData.length);
        Assert.assertEquals(204, launcher.service("POST", "/", "", h, jsonBookData, null).getStatus());
    }

    @Test
    public void testJsonEntityArray() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookArray());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        // Object is transfered via JSON
        h.putSingle("content-type", "application/json");
        h.putSingle("content-length", "" + jsonArrayData.length);
        Assert.assertEquals(204, launcher.service("POST", "/", "", h, jsonArrayData, null).getStatus());
    }

    @Test
    public void testJsonEntityCollection() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookCollection());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        // Object is transfered via JSON
        h.putSingle("content-type", "application/json");
        h.putSingle("content-length", "" + jsonArrayData.length);
        Assert.assertEquals(204, launcher.service("POST", "/", "", h, jsonArrayData, null).getStatus());
    }

    @Test
    public void testJsonEntityMap() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookMap());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        // Object is transfered via JSON
        h.putSingle("content-type", "application/json");
        h.putSingle("content-length", "" + jsonMapData.length);
        Assert.assertEquals(204, launcher.service("POST", "/", "", h, jsonMapData, null).getStatus());
    }

    @Test
    public void testJsonEntityString() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceString());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", "application/json");
        h.putSingle("content-length", "" + jsonBookData.length);
        Assert.assertEquals(204, launcher.service("POST", "/", "", h, jsonBookData, null).getStatus());
    }

    @Test
    public void testJsonReturnBean() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBook2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/json");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        // ResourceBook2#m1()
        ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        JsonParser parser = new JsonParser();
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        Book book = ObjectBuilder.createObject(Book.class, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book.getTitle());
        Assert.assertEquals("William Shakespeare", book.getAuthor());
        Assert.assertTrue(book.isSendByPost());

        // ResourceBook2#m2()
        writer.reset();
        response = launcher.service("POST", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        book = ObjectBuilder.createObject(Book.class, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book.getTitle());
        Assert.assertEquals("William Shakespeare", book.getAuthor());
        Assert.assertTrue(book.isSendByPost());
    }

    @Test
    public void testJsonReturnRaw() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookRaw2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/json");

        // ResourceBook2#m1()
        ContainerResponse response = launcher.service("GET", "/", "", h, null, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        JsonValue book = (JsonValue)response.getEntity();
        Assert.assertEquals("Hamlet", book.getElement("title").getStringValue());
        Assert.assertEquals("William Shakespeare", book.getElement("author").getStringValue());
        Assert.assertTrue(book.getElement("sendByPost").getBooleanValue());

        // ResourceBook2#m2()
        response = launcher.service("POST", "/", "", h, null, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        book = (JsonValue)response.getEntity();
        Assert.assertEquals("Hamlet", book.getElement("title").getStringValue());
        Assert.assertEquals("William Shakespeare", book.getElement("author").getStringValue());
        Assert.assertTrue(book.getElement("sendByPost").getBooleanValue());
    }

    @Test
    public void testJsonReturnBeanArray() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookArray2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/json");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        // ResourceBookArray2#m1()
        ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        JsonParser parser = new JsonParser();
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        Book[] book = (Book[])ObjectBuilder.createArray(Book[].class, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book[0].getTitle());
        Assert.assertEquals("William Shakespeare", book[0].getAuthor());
        Assert.assertTrue(book[0].isSendByPost());
        Assert.assertEquals("Collected Stories", book[1].getTitle());
        Assert.assertEquals("Gabriel Garcia Marquez", book[1].getAuthor());
        Assert.assertTrue(book[1].isSendByPost());
        //System.out.println("array: " + new String(writer.getBody()));

        // ResourceBookArray2#m2()
        writer.reset();
        response = launcher.service("POST", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        book = (Book[])ObjectBuilder.createArray(Book[].class, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book[0].getTitle());
        Assert.assertEquals("William Shakespeare", book[0].getAuthor());
        Assert.assertTrue(book[0].isSendByPost());
        Assert.assertEquals("Collected Stories", book[1].getTitle());
        Assert.assertEquals("Gabriel Garcia Marquez", book[1].getAuthor());
        Assert.assertTrue(book[1].isSendByPost());
        //System.out.println("array: " + new String(writer.getBody()));
    }

    @SuppressWarnings({"unchecked", "serial"})
    @Test
    public void testJsonReturnBeanCollection() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookCollection2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/json");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        // ResourceBookCollection2#m1()
        ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        JsonParser parser = new JsonParser();
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        ParameterizedType genericType = (ParameterizedType)new ArrayList<Book>() {
        }.getClass().getGenericSuperclass();
        //System.out.println(">>>>>"+genericType);
        List<Book> book = ObjectBuilder.createCollection(List.class, genericType, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book.get(0).getTitle());
        Assert.assertEquals("William Shakespeare", book.get(0).getAuthor());
        Assert.assertTrue(book.get(0).isSendByPost());
        Assert.assertEquals("Collected Stories", book.get(1).getTitle());
        Assert.assertEquals("Gabriel Garcia Marquez", book.get(1).getAuthor());
        Assert.assertTrue(book.get(1).isSendByPost());
        //System.out.println("collection: " + new String(writer.getBody()));

        // ResourceBookCollection2#m2()
        writer.reset();
        response = launcher.service("POST", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        book = ObjectBuilder.createCollection(List.class, genericType, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book.get(0).getTitle());
        Assert.assertEquals("William Shakespeare", book.get(0).getAuthor());
        Assert.assertTrue(book.get(0).isSendByPost());
        Assert.assertEquals("Collected Stories", book.get(1).getTitle());
        Assert.assertEquals("Gabriel Garcia Marquez", book.get(1).getAuthor());
        Assert.assertTrue(book.get(1).isSendByPost());
        //System.out.println("collection: " + new String(writer.getBody()));
    }

    @SuppressWarnings({"unchecked", "serial"})
    @Test
    public void testJsonReturnBeanMap() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceBookMap2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/json");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        // ResourceBookMap2#m1()
        ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        JsonParser parser = new JsonParser();
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        ParameterizedType genericType = (ParameterizedType)new HashMap<String, Book>() {
        }.getClass().getGenericSuperclass();
        //System.out.println(">>>>>" + genericType);
        Map<String, Book> book = ObjectBuilder.createObject(Map.class, genericType, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book.get("12345").getTitle());
        Assert.assertEquals("William Shakespeare", book.get("12345").getAuthor());
        Assert.assertTrue(book.get("12345").isSendByPost());
        Assert.assertEquals("Collected Stories", book.get("54321").getTitle());
        Assert.assertEquals("Gabriel Garcia Marquez", book.get("54321").getAuthor());
        Assert.assertTrue(book.get("54321").isSendByPost());
        //System.out.println("map: " + new String(writer.getBody()));

        // ResourceBookMap2#m2()
        writer.reset();
        response = launcher.service("POST", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        parser.parse(new ByteArrayInputStream(writer.getBody()));
        book = ObjectBuilder.createObject(Map.class, genericType, parser.getJsonObject());
        Assert.assertEquals("Hamlet", book.get("12345").getTitle());
        Assert.assertEquals("William Shakespeare", book.get("12345").getAuthor());
        Assert.assertTrue(book.get("12345").isSendByPost());
        Assert.assertEquals("Collected Stories", book.get("54321").getTitle());
        Assert.assertEquals("Gabriel Garcia Marquez", book.get("54321").getAuthor());
        Assert.assertTrue(book.get("54321").isSendByPost());
        //System.out.println("map: " + new String(writer.getBody()));
    }

    @Test
    public void testJsonReturnString() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new ResourceString2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("accept", "application/json");
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        // ResourceString2#m1()
        ContainerResponse response = launcher.service("GET", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        Assert.assertEquals(jsonBook, response.getEntity());
        //System.out.println("string: " + new String(writer.getBody()));

        // ResourceString2#m2()
        writer.reset();
        response = launcher.service("POST", "/", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/json", response.getContentType().toString());
        Assert.assertEquals(jsonBook, response.getEntity());
        //System.out.println("string: " + new String(writer.getBody()));
    }
}
