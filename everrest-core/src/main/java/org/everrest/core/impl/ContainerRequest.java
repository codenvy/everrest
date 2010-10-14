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

import org.everrest.core.GenericContainerRequest;
import org.everrest.core.impl.header.AcceptLanguage;
import org.everrest.core.impl.header.AcceptMediaType;
import org.everrest.core.impl.header.HeaderHelper;
import org.everrest.core.impl.header.Language;

import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: ContainerRequest.java -1 $
 */
public class ContainerRequest implements GenericContainerRequest
{

   /**
    * HTTP method.
    */
   private String method;

   /**
    * HTTP request message body as stream.
    */
   private InputStream entityStream;

   /**
    * HTTP headers.
    */
   private MultivaluedMap<String, String> httpHeaders;

   /**
    * Parsed HTTP cookies.
    */
   private Map<String, Cookie> cookies;

   /**
    * Source strings of HTTP cookies.
    */
   private List<String> cookieHeaders;

   /**
    * HTTP header Content-Type.
    */
   private MediaType contentType;

   /**
    * HTTP header Content-Language.
    */
   private Locale contentLanguage;

   /**
    * List of accepted media type, HTTP header Accept. List is sorted by quality
    * value factor.
    */
   private List<MediaType> acceptMediaType;

   /**
    * List of accepted language, HTTP header Accept-Language. List is sorted by
    * quality value factor.
    */
   private List<Locale> acceptLanguage;

   /**
    * Full request URI, includes query string and fragment.
    */
   private URI requestUri;

   /**
    * Base URI, e.g. servlet path.
    */
   private URI baseUri;

   /**
    * Constructs new instance of ContainerRequest.
    *
    * @param method HTTP method
    * @param requestUri full request URI
    * @param baseUri base request URI
    * @param entityStream request message body as stream
    * @param httpHeaders HTTP headers
    */
   public ContainerRequest(String method, URI requestUri, URI baseUri, InputStream entityStream,
      MultivaluedMap<String, String> httpHeaders)
   {
      this.method = method;
      this.requestUri = requestUri;
      this.baseUri = baseUri;
      this.entityStream = entityStream;
      this.httpHeaders = httpHeaders;
   }

   // GenericContainerRequest

   /**
    * {@inheritDoc}
    */
   public MediaType getAcceptableMediaType(List<MediaType> mediaTypes)
   {
      if (mediaTypes.isEmpty())
         // getAcceptableMediaTypes() return list which contains at least one
         // element even HTTP header 'accept' is absent
         return getAcceptableMediaTypes().get(0);

      List<MediaType> l = getAcceptableMediaTypes();

      for (MediaType at : l)
      {
         if (at.isWildcardType())
            // any media type from given list is acceptable the take first
            return mediaTypes.get(0);

         for (MediaType rt : mediaTypes)
         {
            // skip all media types if it has wildcard at type or sub-type
            if (rt.isCompatible(at) && !rt.isWildcardType() && !rt.isWildcardSubtype())
               return rt;
         }
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public List<String> getCookieHeaders()
   {
      if (cookieHeaders == null)
      {
         List<String> c = getRequestHeader(COOKIE);
         if (c != null && c.size() > 0)
            cookieHeaders = Collections.unmodifiableList(getRequestHeader(COOKIE));
         else
            cookieHeaders = Collections.emptyList();
      }
      return cookieHeaders;
   }

   /**
    * {@inheritDoc}
    */
   public InputStream getEntityStream()
   {
      return entityStream;
   }

   /**
    * {@inheritDoc}
    */
   public URI getRequestUri()
   {
      return requestUri;
   }

   /**
    * {@inheritDoc}
    */
   public URI getBaseUri()
   {
      return baseUri;
   }

   /**
    * {@inheritDoc}
    */
   public void setMethod(String method)
   {
      this.method = method;
   }

   /**
    * {@inheritDoc}
    */
   public void setEntityStream(InputStream entityStream)
   {
      this.entityStream = entityStream;

      // reset form data, it should be recreated
      ApplicationContextImpl.getCurrent().getAttributes().remove("org.everrest.provider.entity.form");
   }

   /**
    * {@inheritDoc}
    */
   public void setUris(URI requestUri, URI baseUri)
   {
      this.requestUri = requestUri;
      this.baseUri = baseUri;
   }

   /**
    * {@inheritDoc}
    */
   public void setCookieHeaders(List<String> cookieHeaders)
   {
      this.cookieHeaders = cookieHeaders;

      // reset parsed cookies
      this.cookies = null;
   }

   /**
    * {@inheritDoc}
    */
   public void setRequestHeaders(MultivaluedMap<String, String> httpHeaders)
   {
      this.httpHeaders = httpHeaders;

      // reset dependent fields
      this.cookieHeaders = null;
      this.cookies = null;
      this.contentType = null;
      this.contentLanguage = null;
      this.acceptMediaType = null;
      this.acceptLanguage = null;
   }

   // javax.ws.rs.core.SecurityContext

   // Methods from SecurityContext will have different implementation for
   // different container. Currently thinking about servlet container only but
   // for flexible don't implement it here, it must be implemented in super
   // classes.
   /**
    * {@inheritDoc}
    */
   public String getAuthenticationScheme()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * {@inheritDoc}
    */
   public Principal getUserPrincipal()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSecure()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isUserInRole(String role)
   {
      throw new UnsupportedOperationException();
   }

   // javax.ws.rs.core.Request

   /**
    * {@inheritDoc}
    */
   public ResponseBuilder evaluatePreconditions(EntityTag etag)
   {
      ResponseBuilder rb = evaluateIfMatch(etag);
      if (rb != null)
         return rb;

      return evaluateIfNoneMatch(etag);
   }

   /**
    * {@inheritDoc}
    */
   public ResponseBuilder evaluatePreconditions(Date lastModified)
   {
      long lastModifiedTime = lastModified.getTime();
      ResponseBuilder rb = evaluateIfModified(lastModifiedTime);
      if (rb != null)
         return rb;

      return evaluateIfUnmodified(lastModifiedTime);

   }

   /**
    * {@inheritDoc}
    */
   public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag etag)
   {
      ResponseBuilder rb = evaluateIfMatch(etag);
      if (rb != null)
         return rb;

      long lastModifiedTime = lastModified.getTime();
      rb = evaluateIfModified(lastModifiedTime);
      if (rb != null)
         return rb;

      rb = evaluateIfNoneMatch(etag);
      if (rb != null)
         return rb;

      return evaluateIfUnmodified(lastModifiedTime);

   }

   /**
    * {@inheritDoc}
    */
   public String getMethod()
   {
      return method;
   }

   /**
    * {@inheritDoc}
    */
   public Variant selectVariant(List<Variant> variants)
   {
      if (variants == null || variants.isEmpty())
         throw new IllegalArgumentException("The list of variants is null or empty");
      // TODO constructs and set 'Vary' header in response
      // Response will be set in RequestDispatcher if set Response
      // now then it will be any way rewrite in RequestDispatcher.
      return VariantsHandler.handleVariants(this, variants);
   }

   // javax.ws.rs.core.HttpHeaders

   /**
    * If accept-language header does not present or its length is null then
    * default language list will be returned. This list contains only one
    * element Locale with language '*', and it minds any language accepted.
    * {@inheritDoc}
    */
   public List<Locale> getAcceptableLanguages()
   {
      if (acceptLanguage == null)
      {
         List<AcceptLanguage> l =
            HeaderHelper.createAcceptedLanguageList(HeaderHelper.convertToString(getRequestHeader(ACCEPT_LANGUAGE)));
         List<Locale> t = new ArrayList<Locale>(l.size());
         // extract Locales from AcceptLanguage
         for (AcceptLanguage al : l)
            t.add(al.getLocale());

         acceptLanguage = Collections.unmodifiableList(t);
      }

      return acceptLanguage;
   }

   /**
    * If accept header does not presents or its length is null then list with
    * one element will be returned. That one element is default media type, see
    * {@link AcceptMediaType#DEFAULT} . {@inheritDoc}
    */
   public List<MediaType> getAcceptableMediaTypes()
   {
      if (acceptMediaType == null)
      {
         // 'extract' MediaType from AcceptMediaType
         List<MediaType> t =
            new ArrayList<MediaType>(HeaderHelper.createAcceptedMediaTypeList(HeaderHelper
               .convertToString(getRequestHeader(ACCEPT))));
         acceptMediaType = Collections.unmodifiableList(t);
      }

      return acceptMediaType;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, Cookie> getCookies()
   {
      if (cookies == null)
      {
         Map<String, Cookie> t = new HashMap<String, Cookie>();

         for (String ch : getCookieHeaders())
         {
            List<Cookie> l = HeaderHelper.parseCookies(ch);
            for (Cookie c : l)
               t.put(c.getName(), c);
         }

         cookies = Collections.unmodifiableMap(t);
      }

      return cookies;
   }

   /**
    * {@inheritDoc}
    */
   public Locale getLanguage()
   {
      // TODO Not efficient implementation, header map can be checked few times
      if (contentLanguage == null && httpHeaders.getFirst(CONTENT_LANGUAGE) != null)
         contentLanguage = Language.getLocale(httpHeaders.getFirst(CONTENT_LANGUAGE));

      return contentLanguage;
   }

   /**
    * {@inheritDoc}
    */
   public MediaType getMediaType()
   {
      // TODO Not efficient implementation, if header map can be checked few times
      if (contentType == null && httpHeaders.getFirst(CONTENT_TYPE) != null)
         contentType = MediaType.valueOf(httpHeaders.getFirst(CONTENT_TYPE));

      return contentType;
   }

   /**
    * {@inheritDoc}
    */
   public List<String> getRequestHeader(String name)
   {
      return httpHeaders.get(name);
   }

   /**
    * {@inheritDoc}
    */
   public MultivaluedMap<String, String> getRequestHeaders()
   {
      return httpHeaders;
   }

   /**
    * Comparison for If-Match header and ETag.
    *
    * @param etag the ETag
    * @return ResponseBuilder with status 412 (precondition failed) if If-Match
    *         header is NOT MATCH to ETag or null otherwise
    */
   private ResponseBuilder evaluateIfMatch(EntityTag etag)
   {
      String ifMatch = getRequestHeaders().getFirst(IF_MATCH);
      // Strong comparison is required.
      // From specification:
      // The strong comparison function: in order to be considered equal,
      // both validators MUST be identical in every way, and both MUST
      // NOT be weak.

      if (ifMatch == null)
         return null;

      EntityTag otherEtag = EntityTag.valueOf(ifMatch);

      // TODO check is status 412 valid if one of tag is weak
      if ((etag.isWeak() || otherEtag.isWeak()) // one of tag is weak
         || (!"*".equals(otherEtag.getValue()) && !etag.getValue().equals(otherEtag.getValue())))
         return Response.status(Response.Status.PRECONDITION_FAILED);

      // if tags are not matched then do as tag 'if-match' is absent
      return null;

   }

   /**
    * Comparison for If-None-Match header and ETag.
    *
    * @param etag the ETag
    * @return ResponseBuilder with status 412 (precondition failed) if
    *         If-None-Match header is MATCH to ETag and HTTP method is not GET
    *         or HEAD. If method is GET or HEAD and If-None-Match is MATCH to
    *         ETag then ResponseBuilder with status 304 (not modified) will be
    *         returned.
    */
   private ResponseBuilder evaluateIfNoneMatch(EntityTag etag)
   {
      String ifNoneMatch = getRequestHeaders().getFirst(IF_NONE_MATCH);

      if (ifNoneMatch == null)
         return null;

      EntityTag otherEtag = EntityTag.valueOf(ifNoneMatch);
      String httpMethod = getMethod();
      // The weak comparison function can only be used with GET or HEAD requests.
      if (httpMethod.equals(HttpMethod.GET) || httpMethod.equals(HttpMethod.HEAD))
      {

         if ("*".equals(otherEtag.getValue()) || etag.getValue().equals(otherEtag.getValue()))
            return Response.notModified(etag);

      }
      else
      {
         // Use strong comparison (ignore weak tags) because HTTP method is not GET
         // or HEAD. If one of tag is weak then tags are not identical.
         if (!etag.isWeak() && !otherEtag.isWeak()
            && ("*".equals(otherEtag.getValue()) || etag.getValue().equals(otherEtag.getValue())))
            return Response.status(Response.Status.PRECONDITION_FAILED);

      }

      // if tags are matched then do as tag 'if-none-match' is absent
      return null;

   }

   /**
    * Comparison for lastModified and unmodifiedSince times.
    *
    * @param lastModified the last modified time
    * @return ResponseBuilder with status 412 (precondition failed) if
    *         lastModified time is greater then unmodifiedSince otherwise return
    *         null. If date format in header If-Unmodified-Since is wrong also
    *         null returned
    */
   private ResponseBuilder evaluateIfModified(long lastModified)
   {
      String ifUnmodified = getRequestHeaders().getFirst(IF_UNMODIFIED_SINCE);

      if (ifUnmodified == null)
         return null;
      try
      {
         long unmodifiedSince = HeaderHelper.parseDateHeader(ifUnmodified).getTime();
         if (lastModified > unmodifiedSince)
            return Response.status(Response.Status.PRECONDITION_FAILED);

      }
      catch (IllegalArgumentException e)
      {
         // If the specified date is invalid, the header is ignored.
      }

      return null;
   }

   /**
    * Comparison for lastModified and modifiedSince times.
    *
    * @param lastModified the last modified time
    * @return ResponseBuilder with status 304 (not modified) if lastModified
    *         time is greater then modifiedSince otherwise return null. If date
    *         format in header If-Modified-Since is wrong also null returned
    */
   private ResponseBuilder evaluateIfUnmodified(long lastModified)
   {
      String ifModified = getRequestHeaders().getFirst(IF_MODIFIED_SINCE);

      if (ifModified == null)
         return null;
      try
      {
         long modifiedSince = HeaderHelper.parseDateHeader(ifModified).getTime();
         if (lastModified < modifiedSince)
            return Response.notModified();

      }
      catch (IllegalArgumentException e)
      {
         // If the specified date is invalid, the header is ignored.
      }

      return null;
   }

}
