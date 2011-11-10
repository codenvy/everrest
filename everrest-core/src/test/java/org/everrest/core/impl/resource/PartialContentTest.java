/**
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.everrest.core.impl.resource;

import org.everrest.core.ExtHttpHeaders;
import org.everrest.core.impl.BaseTest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.MultivaluedMapImpl;
import org.everrest.core.impl.header.Ranges;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class PartialContentTest extends BaseTest
{

   @Path("a")
   public static class Resource1
   {
      @GET
      public Response m(@HeaderParam("Range") Ranges ranges)
      {
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

   public void testPartialContent() throws Exception
   {
      registry(Resource1.class);
      MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
      headers.putSingle("range", "bytes=2-5");
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      ContainerResponse response = launcher.service("GET", "/a", "", headers, null, writer, null);
      MultivaluedMap<String, Object> responseHeaders = writer.getHeaders();
      assertEquals("4", responseHeaders.getFirst("content-length"));
      assertEquals("bytes 2-5/18", responseHeaders.getFirst("content-range"));
      assertEquals("bytes", responseHeaders.getFirst("accept-ranges"));
      assertEquals(" be ", new String(writer.getBody()));
      unregistry(Resource1.class);
   }

}
