/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.everrest.core.impl.async;

import org.everrest.core.impl.ApplicationContextImpl;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

/**
 * Service to get result of invocation asynchronous job from {@link AsynchronousJobPool}.
 * Instance of AsynchronousJobPool obtained in instance of this class via mechanism of injections.
 * This resource must always be deployed as per-request resource.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("async/{job}")
public class AsynchronousJobService
{
   @Context
   private Providers providers;

   @GET
   public Object get(@PathParam("job") String jobId, @Context UriInfo uriInfo)
   {
      AsynchronousJobPool pool = getJobPool();
      final AsynchronousJob async = pool.getJob(jobId);
      if (async == null)
      {
         throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
            .entity("Job " + jobId + " not found. ").type(MediaType.TEXT_PLAIN).build());
      }
      if (async.isDone())
      {
         Object result;
         try
         {
            result = async.getResult();
         }
         finally
         {
            pool.removeJob(jobId);
         }

         // This response will be sent to client side.
         Response response;
         if (result == null || result.getClass() == void.class || result.getClass() == Void.class)
         {
            response = Response.noContent().build();
         }
         else if (Response.class.isAssignableFrom(result.getClass()))
         {
            response = (Response)result;

            if (response.getEntity() != null && response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE) == null)
            {
               MediaType contentType = async.getRequest().getAcceptableMediaType(async.getResourceMethod().produces());
               response.getMetadata().putSingle(HttpHeaders.CONTENT_TYPE, contentType);
            }
         }
         else
         {
            MediaType contentType = async.getRequest().getAcceptableMediaType(async.getResourceMethod().produces());
            response = Response.ok(result, contentType).build();
         }

         // Result of job.
         ApplicationContextImpl.getCurrent().getContainerResponse().setResponse(response);

         // This response (204) never sent to client side.
         return null;
      }
      else
      {
         final String jobUri = uriInfo.getRequestUri().toString();
         return Response
            .status(Response.Status.ACCEPTED)
            .header(HttpHeaders.LOCATION, jobUri)
            .entity(jobUri)
            .type(MediaType.TEXT_PLAIN).build();
      }
   }

   @DELETE
   public void remove(@PathParam("job") String jobId)
   {
      AsynchronousJobPool jobPool = getJobPool();
      if (null == jobPool.removeJob(jobId))
      {
         throw new WebApplicationException(Response
            .status(Response.Status.NOT_FOUND)
            .entity("Job " + jobId + " not found. ")
            .type(MediaType.TEXT_PLAIN)
            .build()
         );
      }
   }

   private AsynchronousJobPool getJobPool()
   {
      if (providers != null)
      {
         ContextResolver<AsynchronousJobPool> asyncJobsResolver =
            providers.getContextResolver(AsynchronousJobPool.class, null);
         if (asyncJobsResolver != null)
         {
            return asyncJobsResolver.getContext(null);
         }
      }
      throw new RuntimeException("Asynchronous jobs feature is not configured properly. ");
   }
}
