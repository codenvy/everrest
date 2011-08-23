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

import org.everrest.core.GenericContainerRequest;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
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
   public Object get(@PathParam("job") String jobId, @Context UriInfo uriInfo, @Context Request request)
   {
      AsynchronousJobPool pool = getJobPool();
      AsynchronousJob async = pool.getJob(jobId);
      if (async == null)
         throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
            .entity("Job " + jobId + " not found. ").type(MediaType.TEXT_PLAIN).build());
      if (async.isDone())
      {
         Object result;
         try
         {
            result = async.get();
         }
         catch (InterruptedException e)
         {
            throw new WebApplicationException(e);
         }
         catch (ExecutionException e)
         {
            throw new WebApplicationException(e);
         }
         finally
         {
            pool.removeJob(jobId, false);
         }

         if (result == null || result.getClass() == void.class || result.getClass() == Void.class)
         {
            return Response.noContent().build();
         }
         else
         {
            if (Response.class.isAssignableFrom(result.getClass()))
            {
               Response response = (Response)result;
               if (response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE) == null && response.getEntity() != null)
                  response.getMetadata().putSingle(HttpHeaders.CONTENT_TYPE,
                     ((GenericContainerRequest)request).getAcceptableMediaType(async.getResourceMethod().produces()));
               return response;
            }
            else
            {
               return Response.ok(result,
                  ((GenericContainerRequest)request).getAcceptableMediaType(async.getResourceMethod().produces()))
                  .build();
            }
         }
      }
      else
      {
         final String jobUri = uriInfo.getRequestUri().toString();
         return Response.status(Response.Status.ACCEPTED).header(HttpHeaders.LOCATION, jobUri).entity(jobUri)
            .type(MediaType.TEXT_PLAIN).build();
      }
   }

   @DELETE
   public void remove(@PathParam("job") String jobId)
   {
      AsynchronousJobPool jobPool = getJobPool();
      if (!jobPool.removeJob(jobId, true))
         throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
            .entity("Job " + jobId + " not found. ").type(MediaType.TEXT_PLAIN).build());
   }

   private AsynchronousJobPool getJobPool()
   {
      if (providers != null)
      {
         ContextResolver<AsynchronousJobPool> asynchJobsResolver =
            providers.getContextResolver(AsynchronousJobPool.class, null);
         if (asynchJobsResolver != null)
            return asynchJobsResolver.getContext(null);
      }
      throw new RuntimeException("Asynchronous jobs feature is not configured properly. ");
   }
}
