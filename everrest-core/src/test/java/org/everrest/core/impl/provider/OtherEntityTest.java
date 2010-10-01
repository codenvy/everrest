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

import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: OtherEntityTest.java 497 2009-11-08 13:19:25Z aparfonov $
 */
public class OtherEntityTest extends BaseTest
{

   public void setUp() throws Exception
   {
      super.setUp();
   }

   @Path("/")
   public static class Resource1
   {
      @POST
      @Path("bytes")
      public void m1(byte[] b)
      {
         assertEquals("to be or not to be", new String(b));
      }

      @POST
      @Path("string")
      public void m2(String s)
      {
         assertEquals("to be or not to be", s);
      }

      @POST
      @Path("stream")
      public void m3(InputStream in) throws IOException
      {
         byte[] b = new byte[1024];
         int r = in.read(b);
         assertEquals("to be or not to be", new String(b, 0, r));
      }

      @POST
      @Path("reader")
      public void m4(Reader rd) throws IOException
      {
         char[] c = new char[1024];
         int r = rd.read(c);
         assertEquals("to be or not to be", new String(c, 0, r));
      }

      @POST
      @Path("dom")
      @Consumes("application/xml")
      public void m5(DOMSource dom) throws Exception
      {
         assertEquals("root", dom.getNode().getFirstChild().getNodeName());
         assertEquals("hello world", dom.getNode().getFirstChild().getFirstChild().getTextContent());
      }

      @POST
      @Path("sax")
      @Consumes("application/xml")
      public void m6(SAXSource sax) throws Exception
      {
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sax.getInputSource());
         assertEquals("root", doc.getDocumentElement().getNodeName());
         assertEquals("data", doc.getDocumentElement().getFirstChild().getNodeName());
         assertEquals("hello world", doc.getDocumentElement().getFirstChild().getTextContent());
      }

      @POST
      @Path("ss")
      @Consumes("application/xml")
      public void m7(StreamSource ss) throws Exception
      {
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ss.getInputStream());
         assertEquals("root", doc.getDocumentElement().getNodeName());
         assertEquals("data", doc.getDocumentElement().getFirstChild().getNodeName());
         assertEquals("hello world", doc.getDocumentElement().getFirstChild().getTextContent());
      }
   }

   private static final String XML_DATA =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<root><data>hello world</data></root>";

   public void testEntityPatameter() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();

      byte[] data = "to be or not to be".getBytes("UTF-8");
      h.putSingle("content-length", "" + data.length);

      // next types allowed for any content-type
      //    h.putSingle("content-type", "application/octet-stream");
      assertEquals(204, launcher.service("POST", "/bytes", "", h, data, null).getStatus());

      assertEquals(204, launcher.service("POST", "/string", "", h, data, null).getStatus());

      assertEquals(204, launcher.service("POST", "/stream", "", h, data, null).getStatus());

      assertEquals(204, launcher.service("POST", "/reader", "", h, data, null).getStatus());

      // next types required application/xml, text/xml or
      // application/xhtml+xml content-type
      h.putSingle("content-type", "application/xml");
      data = XML_DATA.getBytes("UTF-8");
      h.putSingle("content-length", "" + data.length);
      assertEquals(204, launcher.service("POST", "/dom", "", h, data, null).getStatus());

      assertEquals(204, launcher.service("POST", "/sax", "", h, data, null).getStatus());

      assertEquals(204, launcher.service("POST", "/ss", "", h, data, null).getStatus());

      unregistry(r1);
   }

   @Path("/")
   public static class Resource2
   {
      @GET
      @Path("bytes")
      public byte[] m1() throws Exception
      {
         return "to be or not to be".getBytes("UTF-8");
      }

      @GET
      @Path("string")
      @Produces("text/plain")
      public String m2()
      {
         return "to be or not to be";
      }

      @GET
      @Path("stream")
      public InputStream m3(InputStream in) throws IOException
      {
         return new ByteArrayInputStream("to be or not to be".getBytes("UTF-8"));
      }

      @GET
      @Path("reader")
      @Produces("text/plain")
      public Reader m4() throws IOException
      {
         return new InputStreamReader(new ByteArrayInputStream("to be or not to be".getBytes("UTF-8")));
      }

      @GET
      @Path("dom")
      @Produces("application/xml")
      public DOMSource m5() throws Exception
      {
         return new DOMSource(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new ByteArrayInputStream(XML_DATA.getBytes("UTF-8"))));
      }

      @GET
      @Path("sax")
      @Produces("application/xml")
      public SAXSource m6() throws Exception
      {
         return new SAXSource(new InputSource(new ByteArrayInputStream(XML_DATA.getBytes("UTF-8"))));
      }

      @GET
      @Path("ss")
      @Produces("application/xml")
      public StreamSource m7() throws Exception
      {
         return new StreamSource(new ByteArrayInputStream(XML_DATA.getBytes("UTF-8")));
      }

      @GET
      @Path("so")
      public StreamingOutput m8() throws Exception
      {
         return new StreamingOutput()
         {
            private String data = "to be or not to be";

            public void write(OutputStream out) throws IOException, WebApplicationException
            {
               out.write(data.getBytes("UTF-8"));
            }
         };
      }

      @GET
      @Path("response")
      public Response m9() throws Exception
      {
         String data = "to be or not to be";
         return Response.ok(data, "text/plain").header(HttpHeaders.CONTENT_LENGTH, data.getBytes("UTF-8").length)
            .build();
      }
   }

   public void testReturn() throws Exception
   {
      Resource2 r2 = new Resource2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

      h.putSingle("accept", "text/plain");
      ContainerResponse response = launcher.service("GET", "/bytes", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/octet-stream", response.getContentType().toString());
      assertEquals("to be or not to be".getBytes("UTF-8").length + "", response.getHttpHeaders().getFirst(
         HttpHeaders.CONTENT_LENGTH).toString());
      assertEquals("to be or not to be", new String(writer.getBody()));

      response = launcher.service("GET", "/string", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("text/plain", response.getContentType().toString());
      assertEquals("to be or not to be", new String(writer.getBody()));

      response = launcher.service("GET", "/stream", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/octet-stream", response.getContentType().toString());
      assertEquals("to be or not to be", new String(writer.getBody()));

      response = launcher.service("GET", "/reader", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("text/plain", response.getContentType().toString());
      assertEquals("to be or not to be", new String(writer.getBody()));

      Pattern pattern = Pattern.compile("(<\\?xml .*\\?>)");
      String xml = pattern.matcher(XML_DATA).replaceFirst("");

      h.putSingle("accept", "application/xml");
      response = launcher.service("GET", "/dom", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/xml", response.getContentType().toString());
      String result = new String(writer.getBody());
      result = pattern.matcher(result).replaceFirst("");
      assertEquals(xml, result);

      response = launcher.service("GET", "/sax", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/xml", response.getContentType().toString());
      result = new String(writer.getBody());
      result = pattern.matcher(result).replaceFirst("");
      assertEquals(xml, result);

      response = launcher.service("GET", "/ss", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/xml", response.getContentType().toString());
      result = new String(writer.getBody());
      result = pattern.matcher(result).replaceFirst("");
      assertEquals(xml, result);

      response = launcher.service("GET", "/so", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("application/octet-stream", response.getContentType().toString());
      assertEquals("to be or not to be", new String(writer.getBody()));

      response = launcher.service("GET", "/response", "", h, null, writer, null);
      assertEquals(200, response.getStatus());
      assertEquals("text/plain", response.getContentType().toString());
      assertEquals("to be or not to be".getBytes("UTF-8").length + "", response.getHttpHeaders().getFirst(
         HttpHeaders.CONTENT_LENGTH).toString());
      assertEquals("to be or not to be", new String(writer.getBody()));

      unregistry(r2);
   }
}
