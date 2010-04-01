/*
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
package org.everrest.ext.proxy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.everrest.common.http.client.HTTPResponse;
import org.everrest.common.http.client.ModuleException;
import org.everrest.common.http.client.ParseException;
import org.everrest.common.http.client.ProtocolNotSuppException;
import org.everrest.core.resource.ResourceContainer;

/**
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: ProxyService.java 1886 2010-02-18 10:34:09Z max_shaposhnik $
 */
@Path("proxy")
public class ProxyService implements ResourceContainer
{
   /**
    * Handles GET proxy request.
    * 
    * @param httpRequestHttpServletRequest
    * @param url the url to request
    * @return  response Response
    */
   @GET
   public Response doProxyGet(@Context HttpServletRequest httpRequest, @QueryParam("url") String url)
   {
      BaseConnector conn = new BaseConnector();
      if (url == null)
      {
         Throwable e = new Throwable("Necessary URL parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }
      try
      {
         HTTPResponse resp = conn.fetchGet(httpRequest, url);
         return createResponse(resp);
      }
      catch (MalformedURLException mue)
      {
         throw new WebApplicationException(mue, createErrorResponse(mue, 400));
      }
      catch (ProtocolNotSuppException pnse)
      {
         throw new WebApplicationException(pnse, createErrorResponse(pnse, 400));
      }
      catch (IOException ioe)
      {
         throw new WebApplicationException(ioe, createErrorResponse(ioe, 500));
      }
      catch (ModuleException me)
      {
         throw new WebApplicationException(me, createErrorResponse(me, 500));
      }
      catch (ParseException pe)
      {
         throw new WebApplicationException(pe, createErrorResponse(pe, 400));
      }

   }

   /**
    * Handles POST proxy request.
    * 
    * @param httpRequestHttpServletRequest
    * @param url the url to request
    * @return  response Response
    */
   @POST
   public Response doProxyPost(@Context HttpServletRequest httpRequest, @QueryParam("url") String url)
   {
      BaseConnector conn = new BaseConnector();
      if (url == null)
      {
         Throwable e = new Throwable("Necessary URL parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }
      try
      {
         HTTPResponse resp = conn.fetchPost(httpRequest, url);
         return createResponse(resp);
      }
      catch (MalformedURLException mue)
      {
         throw new WebApplicationException(mue, createErrorResponse(mue, 400));
      }
      catch (ProtocolNotSuppException pnse)
      {
         throw new WebApplicationException(pnse, createErrorResponse(pnse, 400));
      }
      catch (IOException ioe)
      {
         throw new WebApplicationException(ioe, createErrorResponse(ioe, 500));
      }
      catch (ModuleException me)
      {
         throw new WebApplicationException(me, createErrorResponse(me, 500));
      }
      catch (ParseException pe)
      {
         throw new WebApplicationException(pe, createErrorResponse(pe, 400));
      }
   }

   /**
    * Handles PUT proxy request.
    * 
    * @param httpRequestHttpServletRequest
    * @param url the url to request
    * @return  response Response
    */
   @PUT
   public Response doProxyPut(@Context HttpServletRequest httpRequest, @QueryParam("url") String url)
   {
      BaseConnector conn = new BaseConnector();
      if (url == null)
      {
         Throwable e = new Throwable("Necessary URL parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }
      try
      {
         HTTPResponse resp = conn.doPut(httpRequest, url);
         return createResponse(resp);
      }
      catch (MalformedURLException mue)
      {
         throw new WebApplicationException(mue, createErrorResponse(mue, 400));
      }
      catch (ProtocolNotSuppException pnse)
      {
         throw new WebApplicationException(pnse, createErrorResponse(pnse, 400));
      }
      catch (IOException ioe)
      {
         throw new WebApplicationException(ioe, createErrorResponse(ioe, 500));
      }
      catch (ModuleException me)
      {
         throw new WebApplicationException(me, createErrorResponse(me, 500));
      }
      catch (ParseException pe)
      {
         throw new WebApplicationException(pe, createErrorResponse(pe, 400));
      }

   }

   /**
    * Handles DELETE proxy request.
    * 
    * @param httpRequestHttpServletRequest
    * @param url the url to request
    * @return  response Response
    */
   @DELETE
   public Response doProxyDelete(@Context HttpServletRequest httpRequest, @QueryParam("url") String url)
   {
      BaseConnector conn = new BaseConnector();
      if (url == null)
      {
         Throwable e = new Throwable("Necessary URL parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }
      try
      {
         HTTPResponse resp = conn.doDelete(httpRequest, url);
         return createResponse(resp);
      }
      catch (MalformedURLException mue)
      {
         throw new WebApplicationException(mue, createErrorResponse(mue, 400));
      }
      catch (ProtocolNotSuppException pnse)
      {
         throw new WebApplicationException(pnse, createErrorResponse(pnse, 400));
      }
      catch (IOException ioe)
      {
         throw new WebApplicationException(ioe, createErrorResponse(ioe, 500));
      }
      catch (ModuleException me)
      {
         throw new WebApplicationException(me, createErrorResponse(me, 500));
      }
      catch (ParseException pe)
      {
         throw new WebApplicationException(pe, createErrorResponse(pe, 400));
      }

   }

   /**
    * Creates the response from HTTP response.
    * 
    * @param httpResponse the http response
    * @return response Response
    */
   private Response createResponse(HTTPResponse httpResponse)
   {
      ResponseBuilder responseBuilder;
      try
      {
         responseBuilder = Response.status(httpResponse.getStatusCode());
         for (Enumeration<String> en = httpResponse.listHeaders(); en.hasMoreElements();)
         {
            String headerName = (String)en.nextElement();
            responseBuilder.header(headerName, httpResponse.getHeader(headerName));
         }
         return responseBuilder.entity(httpResponse.getInputStream()).build();
      }
      catch (IOException e)
      {
         throw new WebApplicationException(e, createErrorResponse(e, 500));
      }
      catch (ModuleException me)
      {
         throw new WebApplicationException(me, createErrorResponse(me, 400));
      }
   }

   /**
    * Creates the error response.
    * 
    * @param t Throwable
    * @param status integer response status
    * @return response Response
    */
   private Response createErrorResponse(Throwable t, int status)
   {
      return Response.status(status).entity(t.getMessage()).type("text/plain").build();
   }
}
