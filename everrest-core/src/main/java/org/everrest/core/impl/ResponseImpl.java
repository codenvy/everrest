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
package org.everrest.core.impl;

import org.everrest.core.util.CaselessStringWrapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ResponseImpl.java -1 $
 */
public final class ResponseImpl extends Response
{

   /** HTTP status. */
   private final int status;

   /** Entity. Entity will be written as response message body. */
   private final Object entity;

   /** HTTP headers. */
   private final MultivaluedMap<String, Object> headers;

   /**
    * Construct Response with supplied status, entity and headers.
    *
    * @param status HTTP status
    * @param entity an entity
    * @param headers HTTP headers
    */
   ResponseImpl(int status, Object entity, MultivaluedMap<String, Object> headers)
   {
      this.status = status;
      this.entity = entity;
      this.headers = headers;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getEntity()
   {
      return entity;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public MultivaluedMap<String, Object> getMetadata()
   {
      return headers;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getStatus()
   {
      return status;
   }

   // ResponseBuilder

   /**
    * @see ResponseBuilder
    */
   public static final class ResponseBuilderImpl extends ResponseBuilder
   {

      /** HTTP headers which can't be multivalued. */
      private enum HEADERS {
         /** Cache control. */
         CACHE_CONTROL,
         /** Content-Language. */
         CONTENT_LANGUAGE,
         /** Content-Location. */
         CONTENT_LOCATION,
         /** Content-Type. */
         CONTENT_TYPE,
         /** Content-length. */
         CONTENT_LENGTH,
         /** ETag. */
         ETAG,
         /** Expires. */
         EXPIRES,
         /** Last-Modified. */
         LAST_MODIFIED,
         /** Location. */
         LOCATION
      }

      private static final Map<HEADERS, CaselessStringWrapper> HEADER_TO_ENUM =
         new HashMap<HEADERS, CaselessStringWrapper>();

      private static final Map<CaselessStringWrapper, HEADERS> ENUM_TO_HEADER =
         new HashMap<CaselessStringWrapper, HEADERS>();

      static
      {
         HEADER_TO_ENUM.put(HEADERS.CACHE_CONTROL, new CaselessStringWrapper(HttpHeaders.CACHE_CONTROL));
         HEADER_TO_ENUM.put(HEADERS.CONTENT_LANGUAGE, new CaselessStringWrapper(HttpHeaders.CONTENT_LANGUAGE));
         HEADER_TO_ENUM.put(HEADERS.CONTENT_LOCATION, new CaselessStringWrapper(HttpHeaders.CONTENT_LOCATION));
         HEADER_TO_ENUM.put(HEADERS.CONTENT_TYPE, new CaselessStringWrapper(HttpHeaders.CONTENT_TYPE));
         HEADER_TO_ENUM.put(HEADERS.CONTENT_LENGTH, new CaselessStringWrapper(HttpHeaders.CONTENT_LENGTH));
         HEADER_TO_ENUM.put(HEADERS.ETAG, new CaselessStringWrapper(HttpHeaders.ETAG));
         HEADER_TO_ENUM.put(HEADERS.LAST_MODIFIED, new CaselessStringWrapper(HttpHeaders.LAST_MODIFIED));
         HEADER_TO_ENUM.put(HEADERS.LOCATION, new CaselessStringWrapper(HttpHeaders.LOCATION));
         HEADER_TO_ENUM.put(HEADERS.EXPIRES, new CaselessStringWrapper(HttpHeaders.EXPIRES));

         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.CACHE_CONTROL), HEADERS.CACHE_CONTROL);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.CONTENT_LANGUAGE), HEADERS.CONTENT_LANGUAGE);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.CONTENT_LOCATION), HEADERS.CONTENT_LOCATION);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.CONTENT_TYPE), HEADERS.CONTENT_TYPE);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.CONTENT_LENGTH), HEADERS.CONTENT_LENGTH);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.ETAG), HEADERS.ETAG);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.LAST_MODIFIED), HEADERS.LAST_MODIFIED);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.LOCATION), HEADERS.LOCATION);
         ENUM_TO_HEADER.put(new CaselessStringWrapper(HttpHeaders.EXPIRES), HEADERS.EXPIRES);
      }

      /** Default HTTP status, No-content, 204. */
      private static final int DEFAULT_HTTP_STATUS = Response.Status.NO_CONTENT.getStatusCode();

      /** Default HTTP status. */
      private int status = DEFAULT_HTTP_STATUS;

      /** Entity. Entity will be written as response message body. */
      private Object entity;

      /** Not multivalued HTTP headers. */
      //private Map<String, Object> headers = new HashMap<String, Object>();
      private Map<CaselessStringWrapper, Object> headers = new HashMap<CaselessStringWrapper, Object>();

      /** Multivalued HTTP headers. */
      private List<Object> headerValues;

      /** HTTP cookies, Set-Cookie header. */
      private Map<String, NewCookie> cookies;

      /** See {@link ResponseBuilder}. */
      ResponseBuilderImpl()
      {
      }

      /**
       * Useful for clone method.
       *
       * @param other other ResponseBuilderImpl
       * @see #clone()
       */
      private ResponseBuilderImpl(ResponseBuilderImpl other)
      {
         this.status = other.status;
         this.entity = other.entity;
         this.headers.putAll(other.headers);
         this.cookies.putAll(other.cookies);
         this.headerValues.addAll(other.headerValues);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Response build()
      {
         MultivaluedMap<String, Object> m = new OutputHeadersMap();
         // following headers can't be multivalued
         for (Entry<CaselessStringWrapper, Object> e : headers.entrySet())
         {
            m.putSingle(e.getKey().getString(), e.getValue());
         }

         // following headers can be multivalued
         if (headerValues != null)
         {
            Iterator<Object> i = headerValues.iterator();
            while (i.hasNext())
            {
               m.add(((CaselessStringWrapper)i.next()).getString(), i.next());
            }
         }

         // add cookies
         if (cookies != null)
         {
            for (NewCookie c : cookies.values())
            {
               m.add(HttpHeaders.SET_COOKIE, c);
            }
         }

         Response response = new ResponseImpl(status, entity, m);
         reset();

         return response;
      }

      /**
       * Set ResponseBuilder to default state.
       */
      private void reset()
      {
         status = DEFAULT_HTTP_STATUS;
         entity = null;
         headers.clear();
         headerValues = null;
         cookies = null;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder cacheControl(CacheControl cacheControl)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.CACHE_CONTROL), cacheControl);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder clone()
      {
         return new ResponseBuilderImpl(this);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder contentLocation(URI location)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.CONTENT_LOCATION), location);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder cookie(NewCookie... cookies)
      {
         if (cookies == null && this.cookies != null)
         {
            this.cookies.clear();
         }
         else if (cookies != null)
         {
            if (this.cookies == null)
            {
               this.cookies = new HashMap<String, NewCookie>();
            }
            // new cookie overwrite old ones with the same name
            for (NewCookie c : cookies)
            {
               this.cookies.put(c.getName(), c);
            }
         }
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder entity(Object entity)
      {
         this.entity = entity;
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder expires(Date expires)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.EXPIRES), expires);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder header(String name, Object value)
      {
         CaselessStringWrapper caselessname = new CaselessStringWrapper(name);
         if (ENUM_TO_HEADER.get(caselessname) != null)
         {
            if (value == null)
            {
               headers.remove(caselessname);
            }
            else
            {
               headers.put(caselessname, value);
            }
            return this;
         }
         if (value != null)
         {
            if (headerValues == null)
            {
               headerValues = new ArrayList<Object>();
            }
            headerValues.add(caselessname);
            headerValues.add(value);
         }
         else
         {
            if (headerValues == null || headerValues.isEmpty())
            {
               return this;
            }
            Iterator<Object> i = headerValues.iterator();
            while (i.hasNext())
            {
               CaselessStringWrapper next = (CaselessStringWrapper)i.next();
               if (next.equals(caselessname))
               {
                  i.remove(); // remove name
                  i.next();
                  i.remove(); // remove value
               }
               else
               {
                  i.next(); // skip next Object in iterator
               }
            }
         }
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder language(String language)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.CONTENT_LANGUAGE), language);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder language(Locale language)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.CONTENT_LANGUAGE), language);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder lastModified(Date lastModified)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.LAST_MODIFIED), lastModified);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder location(URI location)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.LOCATION), location);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder status(int status)
      {
         this.status = status;
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder tag(EntityTag tag)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.ETAG), tag);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder tag(String tag)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.ETAG), tag);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder type(MediaType type)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.CONTENT_TYPE), type);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder type(String type)
      {
         headers.put(HEADER_TO_ENUM.get(HEADERS.CONTENT_TYPE), type);
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder variant(Variant variant)
      {
         type(variant.getMediaType());
         language(variant.getLanguage());
         if (variant.getEncoding() != null)
         {
            header(HttpHeaders.CONTENT_ENCODING, variant.getEncoding());
         }
         return this;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public ResponseBuilder variants(List<Variant> variants)
      {
         if (variants.isEmpty())
         {
            return this;
         }

         boolean acceptMediaType = variants.get(0).getMediaType() != null;
         boolean acceptLanguage = variants.get(0).getLanguage() != null;
         boolean acceptEncoding = variants.get(0).getEncoding() != null;

         for (Variant v : variants)
         {
            acceptMediaType |= v.getMediaType() != null;
            acceptLanguage |= v.getLanguage() != null;
            acceptEncoding |= v.getEncoding() != null;
         }

         StringBuilder sb = new StringBuilder();
         if (acceptMediaType)
         {
            sb.append(HttpHeaders.ACCEPT);
         }
         if (acceptLanguage)
         {
            if (sb.length() > 0)
            {
               sb.append(',');
            }
            sb.append(HttpHeaders.ACCEPT_LANGUAGE);
         }
         if (acceptEncoding)
         {
            if (sb.length() > 0)
            {
               sb.append(',');
            }
            sb.append(HttpHeaders.ACCEPT_ENCODING);
         }
         if (sb.length() > 0)
         {
            header(HttpHeaders.VARY, sb.toString());
         }
         return this;
      }

   }

}
