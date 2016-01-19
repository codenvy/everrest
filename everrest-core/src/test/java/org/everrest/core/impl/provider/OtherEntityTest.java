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
package org.everrest.core.impl.provider;

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author andrew00x
 */
public class OtherEntityTest extends BaseTest {

    @Path("/")
    public static class Resource1 {
        @POST
        @Path("bytes")
        public void m1(byte[] b) {
            Assert.assertEquals("to be or not to be", new String(b));
        }

        @POST
        @Path("string")
        public void m2(String s) {
            Assert.assertEquals("to be or not to be", s);
        }

        @POST
        @Path("stream")
        public void m3(InputStream in) throws IOException {
            byte[] b = new byte[1024];
            int r = in.read(b);
            Assert.assertEquals("to be or not to be", new String(b, 0, r));
        }

        @POST
        @Path("reader")
        public void m4(Reader rd) throws IOException {
            char[] c = new char[1024];
            int r = rd.read(c);
            Assert.assertEquals("to be or not to be", new String(c, 0, r));
        }

        @POST
        @Path("dom")
        @Consumes("application/xml")
        public void m5(DOMSource dom) throws Exception {
            Assert.assertEquals("root", dom.getNode().getFirstChild().getNodeName());
            Assert.assertEquals("hello world", dom.getNode().getFirstChild().getFirstChild().getTextContent());
        }

        @POST
        @Path("sax")
        @Consumes("application/xml")
        public void m6(SAXSource sax) throws Exception {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sax.getInputSource());
            Assert.assertEquals("root", doc.getDocumentElement().getNodeName());
            Assert.assertEquals("data", doc.getDocumentElement().getFirstChild().getNodeName());
            Assert.assertEquals("hello world", doc.getDocumentElement().getFirstChild().getTextContent());
        }

        @POST
        @Path("ss")
        @Consumes("application/xml")
        public void m7(StreamSource ss) throws Exception {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ss.getInputStream());
            Assert.assertEquals("root", doc.getDocumentElement().getNodeName());
            Assert.assertEquals("data", doc.getDocumentElement().getFirstChild().getNodeName());
            Assert.assertEquals("hello world", doc.getDocumentElement().getFirstChild().getTextContent());
        }
    }

    private static final String XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<root><data>hello world</data></root>";

    @Test
    public void testBytesEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        byte[] data = "to be or not to be".getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/bytes", "", h, data, null).getStatus());
    }

    @Test
    public void testStringEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        byte[] data = "to be or not to be".getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/string", "", h, data, null).getStatus());
    }

    @Test
    public void testStreamEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        byte[] data = "to be or not to be".getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/stream", "", h, data, null).getStatus());
    }

    @Test
    public void testReaderEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        byte[] data = "to be or not to be".getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/reader", "", h, data, null).getStatus());
    }

    @Test
    public void testDomEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();

        h.putSingle("content-type", "application/xml");
        byte[] data = XML_DATA.getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/dom", "", h, data, null).getStatus());
    }

    @Test
    public void testSaxSourceEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", "application/xml");
        byte[] data = XML_DATA.getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/sax", "", h, data, null).getStatus());
    }

    @Test
    public void testStreamSourceEntityParameter() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource1());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        h.putSingle("content-type", "application/xml");
        byte[] data = XML_DATA.getBytes("UTF-8");
        h.putSingle("content-length", "" + data.length);
        Assert.assertEquals(204, launcher.service("POST", "/ss", "", h, data, null).getStatus());
    }


    @Path("/")
    public static class Resource2 {
        @GET
        @Path("bytes")
        public byte[] m1() throws Exception {
            return "to be or not to be".getBytes("UTF-8");
        }

        @GET
        @Path("string")
        @Produces("text/plain")
        public String m2() {
            return "to be or not to be";
        }

        @GET
        @Path("stream")
        public InputStream m3(InputStream in) throws IOException {
            return new ByteArrayInputStream("to be or not to be".getBytes("UTF-8"));
        }

        @GET
        @Path("reader")
        @Produces("text/plain")
        public Reader m4() throws IOException {
            return new InputStreamReader(new ByteArrayInputStream("to be or not to be".getBytes("UTF-8")));
        }

        @GET
        @Path("dom")
        @Produces("application/xml")
        public DOMSource m5() throws Exception {
            return new DOMSource(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new ByteArrayInputStream(XML_DATA.getBytes("UTF-8"))));
        }

        @GET
        @Path("sax")
        @Produces("application/xml")
        public SAXSource m6() throws Exception {
            return new SAXSource(new InputSource(new ByteArrayInputStream(XML_DATA.getBytes("UTF-8"))));
        }

        @GET
        @Path("ss")
        @Produces("application/xml")
        public StreamSource m7() throws Exception {
            return new StreamSource(new ByteArrayInputStream(XML_DATA.getBytes("UTF-8")));
        }

        @GET
        @Path("so")
        public StreamingOutput m8() throws Exception {
            return new StreamingOutput() {
                private String data = "to be or not to be";

                public void write(OutputStream out) throws IOException, WebApplicationException {
                    out.write(data.getBytes("UTF-8"));
                }
            };
        }

        @GET
        @Path("response")
        public Response m9() throws Exception {
            String data = "to be or not to be";
            return Response.ok(data, "text/plain").header(HttpHeaders.CONTENT_LENGTH, data.getBytes("UTF-8").length).build();
        }
    }

    @Test
    public void testReturnBytes() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        h.putSingle("accept", "text/plain");
        ContainerResponse response = launcher.service("GET", "/bytes", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("text/plain", response.getContentType().toString());
        Assert.assertEquals("to be or not to be".getBytes("UTF-8").length + "", writer.getHeaders().getFirst(
                HttpHeaders.CONTENT_LENGTH).toString());
        Assert.assertEquals("to be or not to be", new String(writer.getBody()));
    }

    @Test
    public void testReturnString() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        h.putSingle("accept", "text/plain");
        ContainerResponse response = launcher.service("GET", "/string", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("text/plain", response.getContentType().toString());
        Assert.assertEquals("to be or not to be", new String(writer.getBody()));
    }

    @Test
    public void testReturnStream() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        h.putSingle("accept", "text/plain");
        ContainerResponse response = launcher.service("GET", "/stream", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("text/plain", response.getContentType().toString());
        Assert.assertEquals("to be or not to be", new String(writer.getBody()));
    }

    @Test
    public void testReturnReader() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

        h.putSingle("accept", "text/plain");
        ContainerResponse response = launcher.service("GET", "/reader", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("text/plain", response.getContentType().toString());
        Assert.assertEquals("to be or not to be", new String(writer.getBody()));
    }

    @Test
    public void testReturnDom() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        h.putSingle("accept", "application/xml");
        Pattern pattern = Pattern.compile("(<\\?xml .*\\?>)");
        String xml = pattern.matcher(XML_DATA).replaceFirst("");
        ContainerResponse response = launcher.service("GET", "/dom", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/xml", response.getContentType().toString());
        String result = new String(writer.getBody());
        result = pattern.matcher(result).replaceFirst("");
        Assert.assertEquals(xml, result);
    }

    @Test
    public void testReturnSax() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        Pattern pattern = Pattern.compile("(<\\?xml .*\\?>)");
        String xml = pattern.matcher(XML_DATA).replaceFirst("");
        ContainerResponse response = launcher.service("GET", "/sax", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/xml", response.getContentType().toString());
        String result = new String(writer.getBody());
        result = pattern.matcher(result).replaceFirst("");
        Assert.assertEquals(xml, result);
    }

    @Test
    public void testReturnStreamingOutput() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service("GET", "/so", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/octet-stream", response.getContentType().toString());
        Assert.assertEquals("to be or not to be", new String(writer.getBody()));
    }

    @Test
    public void testReturnResponse() throws Exception {
        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.<Object>singleton(new Resource2());
            }
        });
        MultivaluedMap<String, String> h = new MultivaluedMapImpl();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        h.putSingle("accept", "text/plain");
        ContainerResponse response = launcher.service("GET", "/response", "", h, null, writer, null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("text/plain", response.getContentType().toString());
        Assert.assertEquals("to be or not to be".getBytes("UTF-8").length + "",
                            writer.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH).toString());
        Assert.assertEquals("to be or not to be", new String(writer.getBody()));
    }
}
