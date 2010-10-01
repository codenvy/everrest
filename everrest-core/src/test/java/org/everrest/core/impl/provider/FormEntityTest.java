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
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.test.mock.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: FormEntityTest.java 497 2009-11-08 13:19:25Z aparfonov $
 */
public class FormEntityTest extends BaseTest
{

   public void setUp() throws Exception
   {
      super.setUp();
   }

   @Path("/")
   public static class Resource1
   {
      @POST
      @Path("a")
      @Consumes("application/x-www-form-urlencoded")
      public void m1(@FormParam("foo") String foo, @FormParam("bar") String bar, MultivaluedMap<String, String> form)
      {
         assertEquals(foo, form.getFirst("foo"));
         assertEquals(bar, form.getFirst("bar"));
      }

      @POST
      @Path("b")
      @Consumes("application/x-www-form-urlencoded")
      public void m2(MultivaluedMap<String, String> form)
      {
         assertEquals("to be or not to be", form.getFirst("foo"));
         assertEquals("hello world", form.getFirst("bar"));
      }

   }

   public void testFormEntity() throws Exception
   {
      Resource1 r1 = new Resource1();
      registry(r1);
      byte[] data = "foo=to%20be%20or%20not%20to%20be&bar=hello%20world".getBytes("UTF-8");
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("content-type", "application/x-www-form-urlencoded");
      h.putSingle("content-length", "" + data.length);
      assertEquals(204, launcher.service("POST", "/a", "", h, data, null).getStatus());
      assertEquals(204, launcher.service("POST", "/b", "", h, data, null).getStatus());
      unregistry(r1);
   }

   // Multipart form-data

   @Path("/")
   public static class Resource2
   {

      /**
       * Pattern for comparison with parsed {@link FileItem}.
       */
      private class FileItemTester
      {

         private boolean isFormField;

         private String contentType;

         private String name;

         private String fieldName;

         private String string;

         public FileItemTester(String contentType, boolean isFormField, String fieldName, String name, String string)
         {
            this.contentType = contentType;
            this.isFormField = isFormField;
            this.fieldName = fieldName;
            this.name = name;
            this.string = string;
         }

         public String getContentType()
         {
            return contentType;
         }

         public boolean isFormField()
         {
            return isFormField;
         }

         public String getFieldName()
         {
            return fieldName;
         }

         public String getName()
         {
            return name;
         }

         public String getString()
         {
            return string;
         }
      }

      private Iterator<FileItemTester> pattern;

      /**
       * Initialize <tt>pattern</tt>.
       */
      public Resource2()
      {
         List<FileItemTester> l = new ArrayList<FileItemTester>(3);
         l.add(new FileItemTester("text/xml", false, "xml-file", "foo.xml", XML_DATA));
         l.add(new FileItemTester("application/json", false, "json-file", "foo.json", JSON_DATA));
         l.add(new FileItemTester(null, true, "field", null, "to be or not to be"));
         pattern = l.iterator();
      }

      @POST
      @Consumes("multipart/*")
      public void m9(Iterator<FileItem> iter) throws Exception
      {
         while (iter.hasNext())
         {
            if (!pattern.hasNext())
               fail("Wrong number of parsed items");
            FileItem fi = iter.next();
            FileItemTester fit = pattern.next();
            assertEquals(fit.getContentType(), fi.getContentType());
            assertEquals(fit.isFormField(), fi.isFormField());
            assertEquals(fit.getFieldName(), fi.getFieldName());
            assertEquals(fit.getName(), fi.getName());
            assertEquals(fit.getString(), fi.getString());
         }
      }

   }

   private static final String XML_DATA =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<root><data>hello world</data></root>";

   private static final String JSON_DATA = "{\"data\":\"hello world\"}";

   public void testMultipartForm() throws Exception
   {
      Resource2 r2 = new Resource2();
      registry(r2);
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter w = new PrintWriter(out);
      w.write("--abcdef\r\n" + "Content-Disposition: form-data; name=\"xml-file\"; filename=\"foo.xml\"\r\n"
         + "Content-Type: text/xml\r\n" + "\r\n" + XML_DATA + "\r\n" + "--abcdef\r\n"
         + "Content-Disposition: form-data; name=\"json-file\"; filename=\"foo.json\"\r\n"
         + "Content-Type: application/json\r\n" + "\r\n" + JSON_DATA + "\r\n" + "--abcdef\r\n"
         + "Content-Disposition: form-data; name=\"field\"\r\n" + "\r\n" + "to be or not to be" + "\r\n"
         + "--abcdef--\r\n");
      w.flush();
      h.putSingle("content-type", "multipart/form-data; boundary=abcdef");

      byte[] data = out.toByteArray();
      // NOTE In this test data will be red from HttpServletRequest, not from
      // byte array. See MultipartFormDataEntityProvider.
      EnvironmentContext env = new EnvironmentContext();
      env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(data), data.length,
         "POST", h));
      assertEquals(204, launcher.service("POST", "/", "", h, data, env).getStatus());
      unregistry(r2);
   }

}
