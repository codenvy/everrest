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

import org.everrest.common.http.client.HTTPConnection;
import org.everrest.common.http.client.HTTPResponse;
import org.everrest.common.http.client.HttpOutputStream;
import org.everrest.common.http.client.ModuleException;
import org.everrest.common.http.client.NVPair;
import org.everrest.common.http.client.ProtocolNotSuppException;
import org.everrest.common.util.CaselessStringWrapper;
import org.everrest.common.util.Logger;
import org.everrest.core.resource.ResourceContainer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
 * @version $Id: ProxyService.java 2720 2010-06-29 16:09:50Z aparfonov $
 */
@Path("proxy")
public class ProxyService implements ResourceContainer
{
   protected static final int DEFAULT_CONNECT_TIMEOUT_MS = 10000;

   private static final Logger LOG = Logger.getLogger(ProxyService.class);

   @DELETE
   public Response doProxyDelete(@Context HttpHeaders headers, @Context UriInfo uriInfo,
      @QueryParam("url") String urlParam)
   {
      if (urlParam == null)
      {
         IllegalArgumentException e = new IllegalArgumentException("'url' parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }
      try
      {
         URL url = new URL(urlParam);
         HTTPConnection conn = new HTTPConnection(url);
         conn.setTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
         NVPair[] headerPairs = toNVPair(headers.getRequestHeaders(), //
            Collections.singleton(new CaselessStringWrapper(HttpHeaders.HOST)));
         conn.setAllowUserInteraction(false);
         NVPair credentials = getCredentials(url);
         if (credentials != null)
         {
            conn.addBasicAuthorization(null, credentials.getName(), credentials.getValue());
         }
         HTTPResponse resp = conn.Delete(url.getFile(), headerPairs);
         if (resp.getStatusCode() >= 300)
         {
            if (LOG.isDebugEnabled())
            {
               // Do not read data if debug is off.
               // Client may get empty response, may not read stream twice.
               LOG.debug("DELETE. received status " + resp.getStatusCode() + ", " + resp.getReasonLine());
               byte[] data = resp.getData();
               if (data != null)
               {
                  LOG.debug("DELETE. Text : " + new String(data));
               }
            }
         }
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
   }

   @GET
   public Response doProxyGet(@Context HttpHeaders headers, @Context UriInfo uriInfo, @QueryParam("url") String urlParam)
   {
      if (urlParam == null)
      {
         IllegalArgumentException e = new IllegalArgumentException("'url' parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }
      try
      {
         URL url = new URL(urlParam);
         HTTPConnection conn = new HTTPConnection(url);
         conn.setTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
         NVPair[] headerPairs = toNVPair(headers.getRequestHeaders(), //
            Collections.singleton(new CaselessStringWrapper(HttpHeaders.HOST)));
         conn.setAllowUserInteraction(false);
         NVPair credentials = getCredentials(url);
         if (credentials != null)
         {
            conn.addBasicAuthorization(null, credentials.getName(), credentials.getValue());
         }
         HTTPResponse resp = conn.Get(url.getFile(), (NVPair[])null, headerPairs);
         if (resp.getStatusCode() >= 300)
         {
            if (LOG.isDebugEnabled())
            {
               // Do not read data if debug is off.
               // Client may get empty response, may not read stream twice.
               LOG.debug("GET. received status " + resp.getStatusCode() + ", " + resp.getReasonLine());
               byte[] data = resp.getData();
               if (data != null)
               {
                  LOG.debug("GET. Text : " + new String(data));
               }
            }
         }
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
   }

   @POST
   public Response doProxyPost(@Context HttpHeaders headers, @Context UriInfo uriInfo,
      @QueryParam("url") String urlParam, InputStream entity)
   {
      if (urlParam == null)
      {
         IllegalArgumentException e = new IllegalArgumentException("'url' parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }

      try
      {
         URL url = new URL(urlParam);
         HTTPConnection conn = new HTTPConnection(url);
         conn.setTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
         NVPair[] headerPairs = toNVPair(headers.getRequestHeaders(), //
            Collections.singleton(new CaselessStringWrapper(HttpHeaders.HOST)));
         conn.setAllowUserInteraction(false);
         NVPair credentials = getCredentials(url);
         if (credentials != null)
         {
            conn.addBasicAuthorization(null, credentials.getName(), credentials.getValue());
         }
         HTTPResponse resp = null;
         if (entity != null)
         {
            HttpOutputStream stream = new HttpOutputStream();
            resp = conn.Post(url.getFile(), stream, headerPairs);
            byte[] buf = new byte[1024];
            int r = -1;
            while ((r = entity.read(buf)) != -1)
            {
               stream.write(buf, 0, r);
            }
            stream.close();
         }
         else
         {
            resp = conn.Post(url.getFile(), (NVPair[])null, headerPairs);
         }

         if (resp.getStatusCode() >= 300)
         {
            if (LOG.isDebugEnabled())
            {
               // Do not read data if debug is off.
               // Client may get empty response, may not read stream twice.
               LOG.debug("POST. received status " + resp.getStatusCode() + ", " + resp.getReasonLine());
               byte[] data = resp.getData();
               if (data != null)
               {
                  LOG.debug("POST. Text : " + new String(data));
               }
            }
         }
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
   }

   @PUT
   public Response doProxyPut(@Context HttpHeaders headers, @Context UriInfo uriInfo,
      @QueryParam("url") String urlParam, InputStream entity)
   {
      if (urlParam == null)
      {
         IllegalArgumentException e = new IllegalArgumentException("'url' parameter not found in proxy request");
         throw new WebApplicationException(e, createErrorResponse(e, 404));
      }

      try
      {
         URL url = new URL(urlParam);
         HTTPConnection conn = new HTTPConnection(url);
         conn.setTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
         NVPair[] headerPairs = toNVPair(headers.getRequestHeaders(), //
            Collections.singleton(new CaselessStringWrapper(HttpHeaders.HOST)));
         conn.setAllowUserInteraction(false);
         NVPair credentials = getCredentials(url);
         if (credentials != null)
         {
            conn.addBasicAuthorization(null, credentials.getName(), credentials.getValue());
         }
         HTTPResponse resp = null;
         if (entity != null)
         {
            HttpOutputStream stream = new HttpOutputStream();
            resp = conn.Put(url.getFile(), stream, headerPairs);
            byte[] buf = new byte[1024];
            int r = -1;
            while ((r = entity.read(buf)) != -1)
            {
               stream.write(buf, 0, r);
            }
            stream.close();
         }
         else
         {
            resp = conn.Put(url.getFile(), new byte[0], headerPairs);
         }

         if (resp.getStatusCode() >= 300)
         {
            if (LOG.isDebugEnabled())
            {
               // Do not read data if debug is off.
               // Client may get empty response, may not read stream twice.
               LOG.debug("PUT. received status " + resp.getStatusCode() + ", " + resp.getReasonLine());
               byte[] data = resp.getData();
               if (data != null)
               {
                  LOG.debug("PUT Received : " + new String(data));
               }
            }
         }
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
            String headerName = en.nextElement();
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

   private NVPair getCredentials(URL url)
   {
      String userInfo = url.getUserInfo();
      NVPair credentials = null;
      if (userInfo != null)
      {
         int col = userInfo.indexOf(':');
         if (col == -1)
         {
            credentials = new NVPair(userInfo, "");
         }
         else if (col == userInfo.length() - 1)
         {
            credentials = new NVPair(userInfo.substring(0, userInfo.length() - 1), "");
         }
         else
         {
            credentials = new NVPair(userInfo.substring(0, col), userInfo.substring(col + 1));
         }
      }
      return credentials;
   }

   private NVPair[] toNVPair(MultivaluedMap<String, String> map, Set<CaselessStringWrapper> skip)
   {
      List<NVPair> hds = new ArrayList<NVPair>();
      for (Entry<String, List<String>> e : map.entrySet())
      {
         if (!skip.contains(new CaselessStringWrapper(e.getKey())))
         {
            for (String v : e.getValue())
            {
               hds.add(new NVPair(e.getKey(), v));
            }
         }
      }
      return hds.toArray(new NVPair[hds.size()]);
   }

}
